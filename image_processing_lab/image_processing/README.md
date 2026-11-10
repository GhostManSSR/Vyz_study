# Многопоточная пакетная обработка изображений (C++17)

Проект реализован на основе выданной заготовки `image_processing_starter.zip`.
Классы `Image` в `src/Image.h` и `src/Image.cpp` и файлы `third_party/stb_*` **не изменены**.
Основная реализация лабораторной находится в `src/main.cpp`.

> **Важно об условиях:** в исходном ZIP отсутствует полный текст лабораторной, хотя
> `main.cpp` ссылается на «раздел 2» и «раздел 3» задания. Поэтому здесь реализован
> типовой вариант: рекурсивная пакетная обработка изображений, очередь задач,
> фиксированное число рабочих потоков, защита общих данных, фильтры `grayscale` /
> `invert`, сохранение PNG, измерение времени и обработка ошибок. Если в настоящем
> задании указан иной CLI или схема producer-consumer, их нужно будет согласовать.

## Возможности

- Поиск входных изображений в каталоге и подкаталогах.
- Поддержка чтения PNG, JPEG, BMP, TGA, GIF, PSD, PIC, PNM, PPM, PGM, HDR
  через предоставленную библиотеку stb (форматы, не поддерживаемые конкретным
  декодером, будут отмечены ошибкой).
- Обработка каждого изображения независимо отдельным объектом `Image`.
- Фильтр `grayscale`: перевод в градации серого.
- Фильтр `invert`: инверсия RGB (альфа-канал сохраняется).
- Фильтр `both`: сначала `grayscale()`, затем `invert()`.
- Сохранение результата в PNG; исходные изображения не изменяются.
- Сохранение структуры подкаталогов: `input/nested/a.jpg` превращается
  в `output/nested/a.jpg_grayscale.png`.
- Количество рабочих потоков задаётся пользователем или выбирается автоматически.
- Сводка по успешным / неуспешным изображениям и времени выполнения.
- Если выходной каталог расположен внутри входного, результаты не попадают в очередь.
- Ошибка отдельного изображения не прерывает обработку остальных. При наличии
  ошибок программа возвращает код 1.

## Быстрый запуск одной командой

На Ubuntu / WSL из папки проекта:

```bash
bash run_demo.sh
```

Скрипт выполнит CMake, соберёт приложение, запустит интеграционный тест и
обработает примеры четырьмя потоками. Результаты появятся в `examples/output/`.

## Сборка на Ubuntu / WSL (Windows)

В Ubuntu:

```bash
sudo apt update
sudo apt install -y build-essential cmake python3 unzip
cd image_processing
cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build -j2
```

Бинарный файл появится как `build/image_processor`. База данных, FLTK, ODB не нужны.

## Запуск

Из каталога проекта:

```bash
# Четыре потока, оттенки серого
./build/image_processor examples/input examples/output grayscale 4

# Три потока, инверсия RGB
./build/image_processor examples/input examples/output_invert invert 3

# Оба фильтра подряд; параметры с названиями
./build/image_processor --input examples/input --output examples/output_both --filter both --threads 2

# Автоматическое определение числа потоков, минимум сообщений
./build/image_processor --input examples/input --output examples/auto --filter grayscale --quiet

# Справка
./build/image_processor --help
```

Можно запускать на собственных фотографиях:

```bash
./build/image_processor "/mnt/c/Users/YourName/Pictures" "/mnt/c/Users/YourName/PicturesProcessed" grayscale 4
```

Команды выполняются из папки `image_processing`, а каталог результатов должен
отличаться от входного каталога. Файлы формата PNG имеют имена вида
`example_001.png_grayscale.png`: исходное расширение оставлено в имени, чтобы
не перезаписать выход при наличии файлов `photo.jpg` и `photo.png` с одинаковой
базовой частью.

## Как устроена многопоточность

```text
  main()
     |
     v
  filesystem -> поиск файлов -> std::queue<Task>
                                    | (queueMutex_)
                  +-----------------+-----------------+
                  |                 |                 |
                thread 1         thread 2          thread N
                  |                 |                 |
             Image::load()     Image::load()     Image::load()
             grayscale()       grayscale()       grayscale()
             Image::save()     Image::save()     Image::save()
                  |                 |                 |
             atomic counters + synchronized console log
                  |                 |                 |
                  +-----------------+-----------------+
                                    |
                                join() all
                                    |
                         print elapsed ms / exit code
```

- **`std::queue<Task> tasks_`** — общая очередь входных/выходных путей.
- **`std::mutex queueMutex_`** — защищает проверку `empty()`, извлечение `front()`
  и удаление `pop()` от гонки данных.
- **`std::lock_guard<std::mutex>`** — автоматически отпускает мьютекс при
  выходе из блока, включая исключения.
- **`std::thread`** — независимые рабочие потоки, один и тот же `workerLoop()`.
- **`std::mutex logMutex_`** — защищает сообщения в `std::cout`/`std::cerr`,
  чтобы они не смешивались между потоками.
- **`std::atomic<std::size_t>`** — потокобезопасные счётчики успехов и ошибок.
- **`std::vector<std::thread>`** — хранит объекты потоков, для которых вызывается
  `join()` перед завершением программы.
- **`std::chrono::steady_clock`** — монотонные часы для измерения времени.

**Ключевой момент:** мьютекс *не удерживается во время обработки пикселей*.
Он держится только в короткой критической секции извлечения задания. Каждый поток
создаёт свой локальный `Image image;`, поэтому пиксели разных файлов не являются
общей изменяемой памятью.

## Тестирование

Тесты выполняются через `CTest`, используется только стандартная библиотека
Python 3 для генерации входных PNG и проверки результата. Python не нужен для
самой программы, только для тестов и генератора демонстрационных изображений.

```bash
ctest --test-dir build --output-on-failure
```

Тесты проверяют правильные RGB и alpha-пиксели, работу с 1 и 4 потоками,
вложенные папки, режимы `invert` и `both`, невалидные аргументы, повреждённые
файлы и защиту от повторной обработки собственных результатов.

## Сравнение времени: 1 поток и 4 потока

Для достоверного эксперимента генерируем достаточно большие изображения:

```bash
python3 scripts/generate_samples.py --output examples/benchmark_input --count 24 --width 1500 --height 1000
./build/image_processor --input examples/benchmark_input --output examples/benchmark_1 --filter grayscale --threads 1 --quiet
./build/image_processor --input examples/benchmark_input --output examples/benchmark_4 --filter grayscale --threads 4 --quiet
```

Сравни строки `Finished: ... elapsed: ... ms`. Ускорение можно оценить как
`T(1 поток) / T(4 потока)`; оно **не гарантировано**, поскольку на результат
влияют скорость диска, размер изображений, количество ядер и накладные расходы.

## Что показывать на защите

Открой `src/main.cpp` и найди: `collectTasks`, `queueMutex_`, `workerLoop`,
`std::lock_guard`, `std::thread`, `join`, `atomic`, `chrono`.
Подробнее в файле [`DEFENSE.md`](DEFENSE.md).
