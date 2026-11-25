#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build -j2
ctest --test-dir build -R image_processor_benchmark -V
./build/image_processor examples/input examples/output grayscale 4
printf '\nГотово! Обработанные файлы лежат в examples/output/\n'

#python3 scripts/run_benchmark.py --program build/image_processor