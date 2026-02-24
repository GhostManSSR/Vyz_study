# Makefile для CUDA транспонирования матрицы с профилированием NCU

# Компилятор
NVCC = nvcc

# Исходник
SRC = transpose.cu

# Исполняемый файл
TARGET = transpose

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
	ncu --target-processes all --metrics $(NCU_METRICS) ./$(TARGET)

# Очистка
clean:
	rm -f $(TARGET) *.o
