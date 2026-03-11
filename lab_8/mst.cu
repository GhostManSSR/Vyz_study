#include <stdio.h>
#include <cuda_runtime.h>

__global__ void mst_kernel(float *data, int n) {
    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    if (idx < n) {
        data[idx] *= 2.0f;  // Простая операция для демонстрации
    }
}

int main() {
    const int N = 1024;
    float *h_data = (float*)malloc(N * sizeof(float));
    float *d_data;

    // Инициализация данных
    for (int i = 0; i < N; i++) {
        h_data[i] = (float)i / N;
    }

    // Выделение памяти на GPU
    cudaMalloc(&d_data, N * sizeof(float));
    cudaMemcpy(d_data, h_data, N * sizeof(float), cudaMemcpyHostToDevice);

    // Запуск ядра
    dim3 block(256);
    dim3 grid((N + block.x - 1) / block.x);
    mst_kernel<<<grid, block>>>(d_data, N);
    cudaDeviceSynchronize();

    // Копирование результата
    cudaMemcpy(h_data, d_data, N * sizeof(float), cudaMemcpyDeviceToHost);

    // Проверка результата
    printf("Первые 5 элементов после обработки: ");
    for (int i = 0; i < 5; i++) {
        printf("%.2f ", h_data[i]);
    }
    printf("\n");

    cudaFree(d_data);
    free(h_data);
    return 0;
}
