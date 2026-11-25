#!/usr/bin/env python3
"""Run repeated sequential/parallel measurements and write a Markdown report."""
import argparse
import pathlib
import platform
import re
import shutil
import statistics
import subprocess
import sys


ELAPSED = re.compile(r'elapsed:\s*([0-9.]+)\s*ms')
RESULT = re.compile(r'(\d+) succeeded,\s*(\d+) failed')
THREAD_COUNTS = (1, 2, 4, 8)


def run(program, input_dir, output_dir, threads, image_count):
    if output_dir.exists():
        shutil.rmtree(output_dir)
    command = [
        str(program), '--input', str(input_dir), '--output', str(output_dir),
        '--filter', 'grayscale', '--threads', str(threads), '--quiet',
    ]
    completed = subprocess.run(command, capture_output=True, text=True, check=False)
    if completed.returncode != 0:
        raise RuntimeError(
            f'Processing failed with {threads} threads:\n'
            f'{completed.stdout}\n{completed.stderr}'
        )
    elapsed_match = ELAPSED.search(completed.stdout)
    result_match = RESULT.search(completed.stdout)
    if not elapsed_match or not result_match:
        raise RuntimeError(f'Could not parse processor output:\n{completed.stdout}')
    succeeded, failed = map(int, result_match.groups())
    if succeeded != image_count or failed != 0:
        raise RuntimeError(
            f'Expected {image_count} successful images, got {succeeded} succeeded '
            f'and {failed} failed'
        )
    return float(elapsed_match.group(1)) / 1000.0


def print_results_table(averages):
    headers = ('Режим', 'Потоков', 'Время, с', 'Ускорение T(1)/T(p)')
    baseline = averages[1][0]
    labels = {1: 'Последовательный', 2: 'Параллельный',
              4: 'Параллельный', 8: 'Параллельный'}
    rows = [
        (labels[threads], str(threads), f'{averages[threads][0]:.3f}',
         f'{baseline / averages[threads][0]:.2f}')
        for threads in THREAD_COUNTS
    ]
    widths = [
        max(len(headers[column]), *(len(row[column]) for row in rows))
        for column in range(len(headers))
    ]

    def format_row(values):
        return '│ ' + ' │ '.join(
            value.ljust(widths[column]) for column, value in enumerate(values)
        ) + ' │'

    top = '┌' + '┬'.join('─' * (width + 2) for width in widths) + '┐'
    divider = '├' + '┼'.join('─' * (width + 2) for width in widths) + '┤'
    bottom = '└' + '┴'.join('─' * (width + 2) for width in widths) + '┘'
    print('\nРЕЗУЛЬТАТЫ ЭКСПЕРИМЕНТА')
    print(top)
    print(format_row(headers))
    print(divider)
    for row in rows:
        print(format_row(row))
    print(bottom)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--program', required=True, type=pathlib.Path)
    parser.add_argument('--report', type=pathlib.Path,
                        default=pathlib.Path('experiments/REPORT.md'))
    parser.add_argument('--count', type=int, default=60)
    parser.add_argument('--repeats', type=int, default=3)
    args = parser.parse_args()
    if args.count < 50:
        parser.error('--count must be at least 50')
    if args.repeats != 3:
        parser.error('--repeats must be 3 to match the experiment requirements')
    program = args.program.resolve()
    if not program.is_file():
        parser.error(f'processor executable does not exist: {program}')

    generator = pathlib.Path(__file__).with_name('generate_samples.py')
    examples_dir = pathlib.Path(__file__).resolve().parent.parent / 'examples'
    input_dir = examples_dir / 'benchmark_input'
    output_dir = examples_dir / 'benchmark_output'
    if input_dir.is_symlink() or output_dir.is_symlink():
        raise RuntimeError('Benchmark input/output paths must not be symbolic links')
    if input_dir.exists() and not input_dir.is_dir():
        raise RuntimeError(f'Benchmark input path is not a directory: {input_dir}')
    if output_dir.exists() and not output_dir.is_dir():
        raise RuntimeError(f'Benchmark output path is not a directory: {output_dir}')
    examples_dir.mkdir(parents=True, exist_ok=True)
    if input_dir.exists():
        shutil.rmtree(input_dir)

    averages = {}
    subprocess.run(
        [sys.executable, str(generator), '--output', str(input_dir),
         '--count', str(args.count)],
        check=True,
    )
    for threads in THREAD_COUNTS:
        samples = [
            run(program, input_dir, output_dir, threads, args.count)
            for _ in range(args.repeats)
        ]
        averages[threads] = (statistics.mean(samples), samples)

    baseline = averages[1][0]
    print_results_table(averages)
    sizes = ((320, 240), (800, 600), (1280, 720))
    size_counts = [sum(1 for i in range(args.count) if i % 3 == j) for j in range(3)]
    size_summary = ', '.join(
        '{}x{} ({} шт.)'.format(size[0], size[1], count)
        for size, count in zip(sizes, size_counts)
    )
    report_lines = [
        '# Эксперименты и отчёт',
        '',
        '## Условия эксперимента',
        '',
        f'- Изображений: **{args.count}**; набор состоит из PNG трёх размеров: {size_summary}.',
        '- Обработка: фильтр `grayscale`; замер включает запуск рабочих потоков, '
        'чтение, обработку и запись PNG.',
        '- Режимы: 1, 2, 4 и 8 потоков; каждый запуск повторён 3 раза.',
        '- Перед каждым запуском выходной каталог очищался.',
        f'- Среда: {platform.platform()}, Python {platform.python_version()}.',
        '',
        '## Результаты',
        '',
        '| Режим | Потоков | Среднее время, с | Ускорение T(1)/T(p) |',
        '|---|---:|---:|---:|',
    ]
    labels = {1: 'Последовательный', 2: 'Параллельный',
              4: 'Параллельный', 8: 'Параллельный'}
    for threads in THREAD_COUNTS:
        average, samples = averages[threads]
        speedup = baseline / average
        report_lines.append(
            f'| {labels[threads]} | {threads} | {average:.3f} | {speedup:.2f} |'
        )
    report_lines.extend([
        '',
        '### Замеры по повторам, с',
        '',
        '| Потоков | Повтор 1 | Повтор 2 | Повтор 3 |',
        '|---:|---:|---:|---:|',
    ])
    for threads in THREAD_COUNTS:
        _, samples = averages[threads]
        report_lines.append(
            f'| {threads} | ' + ' | '.join(f'{sample:.3f}' for sample in samples) + ' |'
        )
    report_lines.extend([
        '',
        '## Вывод',
        '',
        f'Для набора из {args.count} изображений среднее время последовательной '
        f'обработки составило {baseline:.3f} с. Ускорение многопоточных режимов '
        'рассчитано относительно этого значения. Результат зависит от числа '
        'доступных ядер, дисковой подсистемы и размера изображений.',
        '',
    ])
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text('\n'.join(report_lines), encoding='utf-8')
    print(f'Report written to {args.report}')


if __name__ == '__main__':
    main()
