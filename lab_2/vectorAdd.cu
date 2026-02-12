#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <cuda_runtime.h>
#include <sys/time.h>

double wtime() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec + tv.tv_usec / 1000000.0;
}

#define CUDA_CHECK(call) { \
    cudaError_t err = call; \
    if (err != cudaSuccess) { \
        fprintf(stderr, "CUDA ошибка '%s' в %s:%d\n", \
                cudaGetErrorString(err), __FILE__, __LINE__); \
        exit(1); \
    } \
}

__global__ void vectorAddFaulty(float *A, float *B, float *C, int N) {
    int i = blockIdx.x * blockDim.x + threadIdx.x;

    if (threadIdx.x % 32 == 0 && i < N) {
        C[i + N] = A[i] + B[i];
    } else if (i < N) {
        C[i] = A[i] + B[i];
    }
}

void testVectorAdd(int N, int blockSize) {
    printf("\n=== Тестирование N=%d элементов, blockSize=%d ===\n", N, blockSize);

    size_t size = N * sizeof(float);

    float *h_A = (float*)malloc(size);
    float *h_B = (float*)malloc(size);
    float *h_C = (float*)malloc(size);
    if (!h_A || !h_B || !h_C) {
        printf("Ошибка malloc\n");
        return;
    }

    for (int i = 0; i < N; i++) {
        h_A[i] = i * 1.0f;
        h_B[i] = i * 2.0f;
    }

    float *d_A, *d_B, *d_C;
    CUDA_CHECK(cudaMalloc(&d_A, size));
    CUDA_CHECK(cudaMalloc(&d_B, size));
    CUDA_CHECK(cudaMalloc(&d_C, size));

    CUDA_CHECK(cudaMemcpy(d_A, h_A, size, cudaMemcpyHostToDevice));
    CUDA_CHECK(cudaMemcpy(d_B, h_B, size, cudaMemcpyHostToDevice));

    dim3 threadsPerBlock(blockSize, 1, 1);
    dim3 numBlocks((N + blockSize - 1) / blockSize, 1, 1);

    printf("Грид: %d блоков по %d нитей = %d нитей всего\n",
           numBlocks.x, threadsPerBlock.x, numBlocks.x * threadsPerBlock.x);

    double start = wtime();
    vectorAddFaulty<<<numBlocks, threadsPerBlock>>>(d_A, d_B, d_C, N);

    cudaError_t launch_err = cudaPeekAtLastError();
    double kernel_time = wtime() - start;

    printf("Время ядра: %.3f мс", kernel_time * 1000);

    if (launch_err != cudaSuccess) {
        printf(" | ОШИБКА запуска: %s\n", cudaGetErrorString(launch_err));
    } else {
        printf(" | Синхронизация...");
        cudaError_t sync_err = cudaDeviceSynchronize();

        if (sync_err != cudaSuccess) {
            printf(" **ОШИБКА ПАМЯТИ: %s**\n", cudaGetErrorString(sync_err));
            printf(">>> Проблемные нити: threadIdx.x %% 32 == 0 (0,32,64,224...)\n");
        } else {
            printf(" OK\n");
            CUDA_CHECK(cudaMemcpy(h_C, d_C, size, cudaMemcpyDeviceToHost));
            int errors = 0;
            for (int i = 0; i < N && errors < 5; i++) {
                if (fabs(h_C[i] - (h_A[i] + h_B[i])) > 1e-5) {
                    printf("  Ошибка в [%d]: %.1f != %.1f\n", i, h_C[i], h_A[i]+h_B[i]);
                    errors++;
                }
            }
            printf("  Проверка: %d вычислительных ошибок\n", errors);
        }
    }

    cudaFree(d_A); cudaFree(d_B); cudaFree(d_C);
    free(h_A); free(h_B); free(h_C);
}

int main() {
    int test_sizes[] = {1000, 10000, 100000, 1000000};
    int optimal_block_sizes[] = {256, 256, 256, 256};

    int num_tests = sizeof(test_sizes) / sizeof(test_sizes[0]);

    for (int t = 0; t < num_tests; t++) {
        testVectorAdd(test_sizes[t], optimal_block_sizes[t]);
    }

    return 0;
}
