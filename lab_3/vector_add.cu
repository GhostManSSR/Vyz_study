#include <stdio.h>
#include <cuda_runtime.h>
#include <iostream>
#include <vector>
#include <iomanip>
#include <cmath>

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

double measureKernelTime(cudaEvent_t start, cudaEvent_t stop, dim3 blockSize, int gridSize) {
    float *d_A, *d_B, *d_C;
    CHECK_CUDA(cudaMalloc(&d_A, N * sizeof(float)));
    CHECK_CUDA(cudaMalloc(&d_B, N * sizeof(float)));
    CHECK_CUDA(cudaMalloc(&d_C, N * sizeof(float)));

    std::vector<float> h_A(N, 1.0f), h_B(N, 2.0f);
    CHECK_CUDA(cudaMemcpy(d_A, h_A.data(), N * sizeof(float), cudaMemcpyHostToDevice));
    CHECK_CUDA(cudaMemcpy(d_B, h_B.data(), N * sizeof(float), cudaMemcpyHostToDevice));

    for (int i = 0; i < 10; i++) {
        vectorAdd<<<gridSize, blockSize>>>(d_A, d_B, d_C, N);
    }
    CHECK_CUDA(cudaDeviceSynchronize());

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
    cudaDeviceProp prop;
    CHECK_CUDA(cudaGetDeviceProperties(&prop, 0));

    printf("=== CUDA Vector Addition Benchmark ===\n");
    printf("GPU: %s (Compute Capability: %d.%d)\n", prop.name, prop.major, prop.minor);
    printf("N = %d elements | RTX 3050 (sm_86)\n", N);
    printf("\n");

    printf("Threads/block | Time (ms) | Grid size | Occupancy | GFLOPS\n");
    printf("-----------------------------------------------------------\n");

    cudaEvent_t start, stop;
    CHECK_CUDA(cudaEventCreate(&start));
    CHECK_CUDA(cudaEventCreate(&stop));

    std::vector<int> blockSizes = {1, 16, 32, 64, 128, 256, 512, 1024};

    int max_warps_sm = prop.maxThreadsPerMultiProcessor / 32;

    double min_time = 1e9;
    int optimal_block = 0;

    for (int threadsPerBlock : blockSizes) {
        int gridSize = (N + threadsPerBlock - 1) / threadsPerBlock;
        double time_ms = measureKernelTime(start, stop, threadsPerBlock, gridSize);

        int max_active_blocks;
        CHECK_CUDA(cudaOccupancyMaxActiveBlocksPerMultiprocessor(&max_active_blocks,
                                                                 vectorAdd, threadsPerBlock, 0));

        float num_warps_block = threadsPerBlock / 32.0f;
        float occupancy = (max_active_blocks * num_warps_block / max_warps_sm) * 100.0f;

        double gflops = (2.0 * N / 1e9) / (time_ms / 1000.0);

        printf("%12d | %8.4f | %8d | %7.1f%% | %7.1f\n",
               threadsPerBlock, time_ms, gridSize, occupancy, gflops);

        if (time_ms < min_time) {
            min_time = time_ms;
            optimal_block = threadsPerBlock;
        }
    }

    printf("-----------------------------------------------------------\n");
    printf("ОПТИМУМ: %d нитей/блок | %.4f мс | %.1f GFLOPS\n",
           optimal_block, min_time, (2.0 * N / 1e9) / (min_time / 1000.0));

    CHECK_CUDA(cudaEventDestroy(start));
    CHECK_CUDA(cudaEventDestroy(stop));
    return 0;
}
