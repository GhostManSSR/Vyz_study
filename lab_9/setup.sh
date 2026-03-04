#!/bin/bash
python3 -m venv cuda_env
source cuda_env/bin/activate
pip install --upgrade pip setuptools wheel numpy pycuda "numba[cuda]"
echo "Готово! Запустите: source cuda_env/bin/activate && python3 lab_9.py"
