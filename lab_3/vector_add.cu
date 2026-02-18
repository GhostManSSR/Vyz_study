#include <stdio.h>
#include <cuda_runtime.h>
#include <iostream>
#include <vector>
#include <iomanip>

#define N (1 << 20)
#define CHECK_CUDA(call) { \
    cudaError_t err = call; \
    if (err != cudaSuccess) { \
        printf("CUDA error: %s\n", cudaGetErrorString(err)); \
        exit(1); \
    } \
}

__global__ void vectorAdd(float *A, float *B, float *C, int n) {
    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    if (idx < n) {
        C[idx] = A[idx] + B[idx];
    }
}

double measureKernelTime(cudaEvent_t start, cudaEvent_t stop, dim3 blockSize) {
    float *d_A, *d_B, *d_C;
    CHECK_CUDA(cudaMalloc(&d_A, N * sizeof(float)));
    CHECK_CUDA(cudaMalloc(&d_B, N * sizeof(float)));
    CHECK_CUDA(cudaMalloc(&d_C, N * sizeof(float)));

    // Инициализация на хосте
    std::vector<float> h_A(N, 1.0f), h_B(N, 2.0f);
    CHECK_CUDA(cudaMemcpy(d_A, h_A.data(), N * sizeof(float), cudaMemcpyHostToDevice));
    CHECK_CUDA(cudaMemcpy(d_B, h_B.data(), N * sizeof(float), cudaMemcpyHostToDevice));

    int gridSize = (N + blockSize.x - 1) / blockSize.x;

    // Сброс событий
    CHECK_CUDA(cudaEventRecord(start, 0));
    CHECK_CUDA(cudaEventRecord(stop, 0));

    // Несколько прогонов для стабильности
    for (int i = 0; i < 10; i++) {
        vectorAdd<<<gridSize, blockSize>>>(d_A, d_B, d_C, N);
    }
    CHECK_CUDA(cudaDeviceSynchronize());

    // Замер времени самого ядра (исключая memcpy)
    CHECK_CUDA(cudaEventRecord(start, 0));
    vectorAdd<<<gridSize, blockSize>>>(d_A, d_B, d_C, N);
    CHECK_CUDA(cudaEventRecord(stop, 0));
    CHECK_CUDA(cudaEventSynchronize(stop));

    float milliseconds = 0;
    CHECK_CUDA(cudaEventElapsedTime(&milliseconds, start, stop));

    CHECK_CUDA(cudaFree(d_A));
    CHECK_CUDA(cudaFree(d_B));
    CHECK_CUDA(cudaFree(d_C));

    return milliseconds;
}

int main() {
    printf("Vector addition benchmark (N=%d)\n", N);
    printf("Threads/block | Kernel time (ms) | Grid size\n");
    printf("------------------------------------------\n");

    cudaEvent_t start, stop;
    CHECK_CUDA(cudaEventCreate(&start));
    CHECK_CUDA(cudaEventCreate(&stop));

    std::vector<int> blockSizes = {1, 16, 32, 64, 128, 256, 512, 1024};

    for (int threadsPerBlock : blockSizes) {
        double time_ms = measureKernelTime(start, stop, threadsPerBlock);
        int gridSize = (N + threadsPerBlock - 1) / threadsPerBlock;

        printf("%4d           | %10.4f     | %8d\n",
               threadsPerBlock, time_ms, gridSize);
    }

    CHECK_CUDA(cudaEventDestroy(start));
    CHECK_CUDA(cudaEventDestroy(stop));

    return 0;
}
