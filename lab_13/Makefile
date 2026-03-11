NVCC = nvcc
TARGET = lab13_final
SOURCE = lab13_wmma.cu

NVCCFLAGS = -O3 -arch=sm_86  # RTX 3050 = sm_86

all: $(TARGET)
	@echo "✅ Lab13 собрана!"
	@./$(TARGET)

$(TARGET): $(SOURCE)
	$(NVCC) $(NVCCFLAGS) -lcublas -lcublasLt $< -o $@

clean:
	rm -f $(TARGET)

run: $(TARGET)
	./$(TARGET)

check:
	nvidia-smi && nvcc --version && echo "✅ CUDA готова!"
