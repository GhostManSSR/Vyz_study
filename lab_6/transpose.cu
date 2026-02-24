#include <cuda_runtime.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#define N 1024
#define K 1024
#define TILE_DIM 32
#define BLOCK_ROWS 8

// ---------------------- Ядра ----------------------

// 1. Наивное транспонирование (global memory only)
__global__ void transposeNaive(float *odata, float *idata, int width, int height) {
    int x = blockIdx.x * blockDim.x + threadIdx.x;
    int y = blockIdx.y * blockDim.y + threadIdx.y;

    if (x < width && y < height) {
        odata[x * height + y] = idata[y * width + x];
    }
}

// 2. Shared memory без устранения конфликтов банков
__global__ void transposeShared(float *odata, float *idata, int width, int height) {
    __shared__ float tile[TILE_DIM][TILE_DIM];

    int x = blockIdx.x * TILE_DIM + threadIdx.x;
    int y = blockIdx.y * TILE_DIM + threadIdx.y;

    if (x < width && y < height)
        tile[threadIdx.y][threadIdx.x] = idata[y * width + x];

    __syncthreads();

    x = blockIdx.y * TILE_DIM + threadIdx.x;
    y = blockIdx.x * TILE_DIM + threadIdx.y;

    if (x < height && y < width)
        odata[y * height + x] = tile[threadIdx.y][threadIdx.x];
}

// 3. Shared memory с устранением конфликтов банков
__global__ void transposeSharedNoBankConflicts(float *odata, float *idata, int width, int height) {
    __shared__ float tile[TILE_DIM][TILE_DIM + 1]; // +1 для устранения конфликтов банков

    int x = blockIdx.x * TILE_DIM + threadIdx.x;
    int y = blockIdx.y * TILE_DIM + threadIdx.y;

    if (x < width && y < height)
        tile[threadIdx.y][threadIdx.x] = idata[y * width + x];

    __syncthreads();

    x = blockIdx.y * TILE_DIM + threadIdx.x;
    y = blockIdx.x * TILE_DIM + threadIdx.y;

    if (x < height && y < width)
        odata[y * height + x] = tile[threadIdx.y][threadIdx.x];
}

// ---------------------- Проверка результата ----------------------
void checkTranspose(float *A, float *B, int width, int height) {
    for (int i = 0; i < height; i++)
        for (int j = 0; j < width; j++)
            if (A[i * width + j] != B[j * height + i]) {
                printf("Mismatch at (%d,%d): %f != %f\n", i, j, A[i * width + j], B[j * height + i]);
                return;
            }
    printf("Transpose check PASSED\n");
}

// ---------------------- Основная программа ----------------------
int main() {
    size_t size = N * K * sizeof(float);

    float *h_A = (float*)malloc(size);
    float *h_B = (float*)malloc(size);

    // Инициализация матрицы
    for (int i = 0; i < N*K; i++)
        h_A[i] = rand() % 100;

    float *d_A, *d_B;
    cudaMalloc(&d_A, size);
    cudaMalloc(&d_B, size);
    cudaMemcpy(d_A, h_A, size, cudaMemcpyHostToDevice);

    dim3 block(TILE_DIM, BLOCK_ROWS);
    dim3 grid((K + TILE_DIM - 1)/TILE_DIM, (N + TILE_DIM - 1)/TILE_DIM);

    cudaEvent_t start, stop;
    float milliseconds;

    // 1. Naive
    cudaEventCreate(&start);
    cudaEventCreate(&stop);
    cudaEventRecord(start);
    transposeNaive<<<grid, block>>>(d_B, d_A, K, N);
    cudaEventRecord(stop);
    cudaEventSynchronize(stop);
    cudaMemcpy(h_B, d_B, size, cudaMemcpyDeviceToHost);
    cudaEventElapsedTime(&milliseconds, start, stop);
    printf("Naive transpose time: %f ms\n", milliseconds);
    checkTranspose(h_A, h_B, K, N);

    // 2. Shared memory без устранения конфликтов банков
    cudaEventRecord(start);
    transposeShared<<<grid, block>>>(d_B, d_A, K, N);
    cudaEventRecord(stop);
    cudaEventSynchronize(stop);
    cudaMemcpy(h_B, d_B, size, cudaMemcpyDeviceToHost);
    cudaEventElapsedTime(&milliseconds, start, stop);
    printf("Shared memory transpose (bank conflicts) time: %f ms\n", milliseconds);
    checkTranspose(h_A, h_B, K, N);

    // 3. Shared memory с устранением конфликтов банков
    cudaEventRecord(start);
    transposeSharedNoBankConflicts<<<grid, block>>>(d_B, d_A, K, N);
    cudaEventRecord(stop);
    cudaEventSynchronize(stop);
    cudaMemcpy(h_B, d_B, size, cudaMemcpyDeviceToHost);
    cudaEventElapsedTime(&milliseconds, start, stop);
    printf("Shared memory transpose (no bank conflicts) time: %f ms\n", milliseconds);
    checkTranspose(h_A, h_B, K, N);

    cudaFree(d_A);
    cudaFree(d_B);
    free(h_A);
    free(h_B);

    return 0;
}
