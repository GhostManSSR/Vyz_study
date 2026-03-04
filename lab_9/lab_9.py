import pycuda.driver as cuda
import pycuda.autoinit
import numpy as np
from pycuda.compiler import SourceModule
import time

# CUDA kernel с Kahan summation
mod = SourceModule("""
__global__ void matrixMul(float *A, float *B, float *C, int N) {
    int row = blockIdx.y * blockDim.y + threadIdx.y;
    int col = blockIdx.x * blockDim.x + threadIdx.x;
    
    if (row < N && col < N) {
        float sum = 0.0f;
        float correction = 0.0f;
        
        for (int i = 0; i < N; i++) {
            float y = A[row * N + i] * B[i * N + col] - correction;
            float t = sum + y;
            correction = (t - sum) - y;
            sum = t;
        }
        C[row * N + col] = sum;
    }
}
""")

matrix_mul = mod.get_function("matrixMul")

print("=== PyCUDA Matrix Multiplication (RTX 3050) ===")

# Основной тест 1024x1024
size = 1024
N = np.int32(size)
print(f"Размер матриц: {size}x{size}")

# ✅ ИСПРАВЛЕНО: C_gpu объявляем ДО использования
A = np.random.rand(size, size).astype(np.float32)
B = np.random.rand(size, size).astype(np.float32)
C_gpu = np.zeros((size, size), dtype=np.float32)  # ← ПЕРЕМЕСТИЛИ СЮДА

# GPU память
A_g = cuda.mem_alloc(A.nbytes)
B_g = cuda.mem_alloc(B.nbytes)
C_g = cuda.mem_alloc(C_gpu.nbytes)  # ✅ Используем C_gpu.nbytes

cuda.memcpy_htod(A_g, A)
cuda.memcpy_htod(B_g, B)
cuda.memset_d32(C_g, 0, C_gpu.size)

# Оптимальная конфигурация RTX 3050
block = (32, 8, 1)  # 256 threads/block
grid = ((size + 31)//32, (size + 7)//8)

print(f"Grid: {grid}, Block: {block}")

# Замер времени
start = time.perf_counter()
matrix_mul(A_g, B_g, C_g, N, block=block, grid=grid)
cuda.Context.synchronize()
end = time.perf_counter()

cuda.memcpy_dtoh(C_gpu, C_g)

# Проверка точности
C_cpu = np.dot(A, B)
error = np.max(np.abs(C_gpu - C_cpu))
gflops = 2 * size**3 / ((end - start) * 1e9)

print(f"\n✅ РЕЗУЛЬТАТЫ:")
print(f"Время kernel: {(end-start)*1000:.2f} ms")
print(f"Производительность: {gflops:.1f} GFLOPS")
print(f"Макс. ошибка: {error:.2e}")
print(f"Средняя ошибка: {np.mean(np.abs(C_gpu - C_cpu)):.2e}")

if error < 1e-3:
    print("🎉 ЛАБОРАТОРНАЯ 9 УСПЕШНО ВЫПОЛНЕНА!")
else:
    print("❌ Проверьте конфигурацию")

# Cleanup
A_g.free()
B_g.free()
C_g.free()
