#!/bin/bash
echo "=== Проверка разделяемой памяти ==="
ls -la /dev/shm/
echo ""
echo "=== Проверка процессов ==="
ps aux | grep lab13
echo ""
echo "=== Проверка отображений памяти ==="
if [ -f /proc/$$/maps ]; then
    grep shm /proc/$$/maps
fi