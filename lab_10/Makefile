# CUDA nvcc из Ubuntu (WSL)
NVCC = nvcc
NVCCFLAGS = -arch=sm_86 -std=c++11 -O3 -Xcompiler -Wall
THRUST_INC = -I/usr/local/cuda/include
THRUST_LIB = -lcudart

TARGET = lab10
PROFILE_TARGET = lab10_profile

all: check_cuda $(TARGET)
	@echo "✅ Сборка завершена! Запуск: ./lab10"

check_cuda:
	@echo "🔍 CUDA версия:"
	@$(NVCC) --version || (echo "❌ nvcc не найден!" && exit 1)
	@nvidia-smi || echo "⚠️  nvidia-smi (но nvcc работает)"

$(TARGET): lab_10.cu
	@echo "📦 Компиляция CUDA + Thrust..."
	$(NVCC) $(NVCCFLAGS) $(THRUST_INC) $(THRUST_LIB) -o $@ $<

$(PROFILE_TARGET): lab_10.cu
	@echo "🎯 Компиляция с профилированием..."
	$(NVCC) $(NVCCFLAGS) -lineinfo --ptxas-options=-v $(THRUST_INC) $(THRUST_LIB) -o $@ $<

# 🚀 Профилирование
profile: $(PROFILE_TARGET)
	@echo "=== Nsight Systems ==="
	nsys profile --stats=true -o lab10_nsys ./$(PROFILE_TARGET) || echo "nsys не найден, запуск обычный"

nsight_compute:
	@echo "=== Nsight Compute (ядра) ==="
	$(NVCC) $(NVCCFLAGS) -G -g -lineinfo $(THRUST_INC) $(THRUST_LIB) -o lab10_ncu lab_10.cu
	ncu --set full -o lab10_ncu ./lab10_ncu || echo "ncu не найден"

run: $(TARGET)
	./$(TARGET)

gpuinfo:
	@nvidia-smi
	@echo ""
	@$(NVCC) --version
	@echo "Архитектура: RTX 3050 (sm_86)"

clean:
	rm -f $(TARGET) $(PROFILE_TARGET) lab10_ncu *.qdrep *.nsys-rep *.ncu-rep

.PHONY: all check_cuda profile nsight_compute run gpuinfo clean
