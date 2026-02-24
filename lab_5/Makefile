# Makefile для CUDA транспонирования матрицы с профилированием NCU

# Компилятор
NVCC = nvcc

# Исходник
SRC = lab_5.cu

# Исполняемый файл
TARGET = lab5

# Флаги компиляции
NVCCFLAGS = -O2

# Метрики для NCU
NCU_METRICS = l1tex__data_bank_conflicts_pipe_lsu_mem_shared,smsp__sass_inst_executed_op_shared,l1tex__data_pipe_lsu_wavefronts_mem_shared

# ---------------------- Цели ----------------------

# Сборка исполняемого файла
all: $(TARGET)

$(TARGET): $(SRC)
	$(NVCC) $(NVCCFLAGS) -o $(TARGET) $(SRC)

# Запуск профилирования NCU
profile: $(TARGET)
	ncu ./$(TARGET)

# Очистка
clean:
	rm -f $(TARGET) *.o
