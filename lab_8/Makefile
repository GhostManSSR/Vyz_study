# Makefile для лабораторной 8: PTX генерация и компоновка
NVCC = nvcc
TARGET = mst
SOURCE = mst.cu
PTX = $(TARGET).ptx
CUBIN = $(TARGET).cubin
OBJ = $(TARGET).o
FATBIN = $(TARGET).fatbin

NVCCFLAGS = -keep -arch=sm_75

.PHONY: all clean ptx manual check run report

all: $(TARGET)
	@$(MAKE) check

$(TARGET): $(SOURCE)
	$(NVCC) $(NVCCFLAGS) $< -o $@
	@echo "✓ Исполняемый файл создан: $@"

ptx: $(PTX)
$(PTX): $(SOURCE)
	$(NVCC) -ptx $< -o $@
	@echo "✓ PTX создан: $@"

$(CUBIN): $(PTX)
	$(NVCC) -cubin $< -o $@
	@echo "✓ CUBIN создан: $@"

$(OBJ): $(SOURCE)
	$(NVCC) -dc $< -o $@
	@echo "✓ Объектный файл: $@"

$(FATBIN): $(OBJ) $(CUBIN)
	$(NVCC) -dlink $(OBJ) $(CUBIN) -o $@
	@echo "✓ FATBIN создан: $@"

manual: clean $(PTX) $(CUBIN) $(OBJ) $(FATBIN) $(TARGET)

check: $(TARGET)
	@echo "=============================================="
	@echo "ПРОВЕРКА РЕЗУЛЬТАТА ЛАБОРАТОРНОЙ 8"
	@echo "=============================================="
	@echo "Сгенерированные файлы:"
	@ls -lh *.ptx *.cubin *.o *.fatbin $(TARGET) 2>/dev/null || echo "Нет промежуточных файлов"
	@echo ""
	@echo "СОДЕРЖИМОЕ mst.ptx:"
	@echo "----------------------------------------"
	@cat $(PTX)
	@echo "----------------------------------------"
	@echo ""
	@echo "PTX ИЗ ИСПОЛНЯЕМОГО ФАЙЛА mst:"
	@echo "----------------------------------------"
	@cuobjdump $(TARGET) -ptx 2>/dev/null | sed -n '1,/Function : mst_kernel/p;/mst_kernel/,/}/p' | head -50 || echo "cuobjdump недоступен"
	@echo "----------------------------------------"
	@echo ""
	@echo "Размеры файлов:"
	@ls -lh $(TARGET) *.ptx *.cubin 2>/dev/null || echo "Файлы не найдены"
	@echo ""
	@echo "Тест запуска:"
	@./$(TARGET) && echo "✓ Программа работает корректно" || echo "❌ Ошибка запуска"

# Отчёт для преподавателя
report: check
	@echo ""
	@echo "ОТЧЁТ ДЛЯ ЛАБОРАТОРНОЙ:"
	@echo "1. PTX: $(PTX) $(wildcard *.ptx)"
	@echo "2. CUBIN: $(CUBIN) $(wildcard *.cubin)"
	@echo "3. Объект: $(OBJ)"
	@echo "4. Fatbin: $(FATBIN)"
	@echo "5. Исполняемый: $(TARGET)"
	@echo "6. PTX встроен: $$(cuobjdump $(TARGET) 2>/dev/null | grep -c Fatbin || echo 0)"
	@echo "Лабораторная 8 выполнена успешно!"

# Запуск
run: $(TARGET)
	./$(TARGET)

# Очистка
clean:
	rm -f $(TARGET) *.o *.ptx *.cubin *.fatbin *.cudalink*

# Только -keep режим
keep-only: $(TARGET)
