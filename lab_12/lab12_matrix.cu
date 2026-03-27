#include <stdio.h>
#include <stdlib.h>
#include <cuda_runtime.h>
#include <cublas_v2.h>
#include <cuda_fp16.h>


#define CHECK_CUDA(call) { cudaError_t err = call; if (err != cudaSuccess) { \
    fprintf(stderr, "CUDA error: %s\n", cudaGetErrorString(err)); exit(1); } }

#define CHECK_CUBLAS(call) { cublasStatus_t err = call; if (err != CUBLAS_STATUS_SUCCESS) { \
    fprintf(stderr, "cuBLAS error: %d\n", (int)err); exit(1); } }

// Regular: float матрица
__global__ void matrixMul_regular(float *C, const float *A, const float *B, int N) {
    int row = blockIdx.y * blockDim.y + threadIdx.y;
    int col = blockIdx.x * blockDim.x + threadIdx.x;

    if (row < N && col < N) {
        float sum = 0.0f;
        for (int k = 0; k < N; k++) {
            sum += A[row * N + k] * B[k * N + col];
        }
        C[row * N + col] = sum;
    }
}

// Прототип TensorCores‑ядрала — виден всегда
__global__ void matrixMul_tensor(float *C, const __half *A, const __half *B, int N);

// Тело TensorCores‑ядрала — только для arch >= 800
#if __CUDA_ARCH__ >= 800
#include <mma.h>
using namespace nvcuda;

__global__ void matrixMul_tensor(float *C, const __half *A, const __half *B, int N) {
    int row = blockIdx.y * blockDim.y + threadIdx.y;
    int col = blockIdx.x * blockDim.x + threadIdx.x;

    if (row < N && col < N) {
        wmma::fragment<wmma::matrix_a, 16, 16, 16, __half, wmma::row_major> a_frag;
        wmma::fragment<wmma::matrix_b, 16, 16, 16, __half, wmma::col_major> b_frag;
        wmma::fragment<wmma::accumulator, 16, 16, 16, float> c_frag;

        wmma::fill_fragment(c_frag, 0.0f);

        int tile_row = row & ~15;
        int tile_col = col & ~15;

        wmma::load_matrix_sync(a_frag, A + tile_row * N + tile_col, N);
        wmma::load_matrix_sync(b_frag, B + tile_row * N + tile_col, N);

        wmma::mma_sync(c_frag, a_frag, b_frag, c_frag);
        wmma::store_matrix_sync(C + tile_row * N + tile_col, c_frag, N, wmma::mem_row_major);
    }
}

#endif

void cublas_gemm_float(cublasHandle_t handle, float *d_A, float *d_B, float *d_C, int N) {
    const float alpha = 1.0f, beta = 0.0f;
    CHECK_CUBLAS(cublasSgemm(handle,
                              CUBLAS_OP_N, CUBLAS_OP_N,
                              N, N, N,
                              &alpha,
                              d_B, N,        // B (col‑major layout)
                              d_A, N,        // A (col‑major layout)
                              &beta,
                              d_C, N));      // C (col‑major)
}

void cublas_gemm_tensor_half(cublasHandle_t handle, __half *d_A, __half *d_B, float *d_C, int N) {
    const float alpha = 1.0f, beta = 0.0f;
    const cublasOperation_t op = CUBLAS_OP_N;

    // Включаем Tensor‑Core‑режим для поддерживаемых arch
    CHECK_CUBLAS(cublasSetMathMode(handle, CUBLAS_TENSOR_OP_MATH));

    // https://docs.nvidia.com/cuda/cublas/#cublas-gemmex
    CHECK_CUBLAS(cublasGemmEx(handle,
                               op, op,
                               N, N, N,
                               &alpha,
                               d_B, CUDA_R_16F, N,  // B (half)
                               d_A, CUDA_R_16F, N,  // A (half)
                               &beta,
                               d_C, CUDA_R_32F, N,  // C (float)
                               CUDA_R_32F,          // computeType
                               CUBLAS_GEMM_DEFAULT));

    // Чтобы не «засорять» другие вызовы, можно вернуть default после
    CHECK_CUBLAS(cublasSetMathMode(handle, CUBLAS_DEFAULT_MATH));
}


// Обычный матричный умножитель (float × float)
void matrix_multiply_regular(int N, float *d_A, float *d_B, float *d_C, const char* method) {
    dim3 block(16, 16);
    dim3 grid((N + 15) / 16, (N + 15) / 16);

    printf("  %-12s: ", method);

    cudaEvent_t start, stop;
    CHECK_CUDA(cudaEventCreate(&start));
    CHECK_CUDA(cudaEventCreate(&stop));

    CHECK_CUDA(cudaEventRecord(start));

    matrixMul_regular<<<grid, block>>>(d_C, d_A, d_B, N);
    CHECK_CUDA(cudaGetLastError());

    CHECK_CUDA(cudaEventRecord(stop));
    CHECK_CUDA(cudaDeviceSynchronize());

    float ms;
    CHECK_CUDA(cudaEventElapsedTime(&ms, start, stop));

    double flops = 2.0 * N * N * N / 1e9;
    double gflops = flops / (ms / 1000.0);

    printf("%6.2fms | %6.1f GFLOPS\n", ms, gflops);

    CHECK_CUDA(cudaEventDestroy(start));
    CHECK_CUDA(cudaEventDestroy(stop));
}

// TensorCores‑матричный умножитель (half → float)

void matrix_multiply_tensor(int N, const __half *d_A, const __half *d_B, float *d_C, const char* method) {
    dim3 block(16, 16);
    dim3 grid((N + 15) / 16, (N + 15) / 16);

    printf("  %-12s: ", method);

    cudaEvent_t start, stop;
    CHECK_CUDA(cudaEventCreate(&start));
    CHECK_CUDA(cudaEventCreate(&stop));

    CHECK_CUDA(cudaEventRecord(start));

    // Всегда лаунчем ядро
    matrixMul_tensor<<<grid, block>>>(d_C, d_A, d_B, N);
    CHECK_CUDA(cudaGetLastError());   // error check после запуска

    CHECK_CUDA(cudaEventRecord(stop));
    CHECK_CUDA(cudaDeviceSynchronize());

    float ms;
    CHECK_CUDA(cudaEventElapsedTime(&ms, start, stop));

    double flops = 2.0 * N * N * N / 1e9;
    double gflops = flops / (ms / 1000.0);

    printf("%6.2fms | %6.1f GFLOPS\n", ms, gflops);

    CHECK_CUDA(cudaEventDestroy(start));
    CHECK_CUDA(cudaEventDestroy(stop));
}


int main() {
    printf("=== Лабораторная 12: RTX 3050 Собственные CUDA ядра ===\n");
    printf("Tensor Cores vs Regular Kernels\n\n");

    int nDevices;
    CHECK_CUDA(cudaGetDeviceCount(&nDevices));
    printf("GPU устройств: %d\n", nDevices);

    cudaDeviceProp prop;
    CHECK_CUDA(cudaGetDeviceProperties(&prop, 0));
    printf("GPU: %s (Compute %d.%d)\n", prop.name, prop.major, prop.minor);
    printf("Tensor Cores: %s\n\n", (prop.major >= 8) ? "Ampere (RTX 30)" : "Нет");

    cublasHandle_t cublas_handle;
    CHECK_CUBLAS(cublasCreate(&cublas_handle));
    // Тест разных размеров
    int sizes[] = {1024, 2048, 4096, 10256};

    for (int i = 0; i < 4; i++) {
        int N = sizes[i];
        printf("%dx%d матрицы\n", N, N);
        printf("Метод          Время(ms) GFLOPS\n");
        printf("------------------------\n");

        float *h_A = (float*)malloc(N * N * sizeof(float));
        float *h_B = (float*)malloc(N * N * sizeof(float));
        float *d_C;

        CHECK_CUDA(cudaMalloc(&d_C, N * N * sizeof(float)));

        // Инициализация float
        for (int j = 0; j < N * N; j++) {
            h_A[j] = 1.0f + j * 0.001f;
            h_B[j] = 2.0f + j * 0.001f;
        }

        float *d_A, *d_B;
        CHECK_CUDA(cudaMalloc(&d_A, N * N * sizeof(float)));
        CHECK_CUDA(cudaMalloc(&d_B, N * N * sizeof(float)));
        CHECK_CUDA(cudaMemcpy(d_A, h_A, N * N * sizeof(float), cudaMemcpyHostToDevice));
        CHECK_CUDA(cudaMemcpy(d_B, h_B, N * N * sizeof(float), cudaMemcpyHostToDevice));

        // Regular CUDA (ваше ядро)
        cudaMemset(d_C, 0, N * N * sizeof(float));
        matrix_multiply_regular(N, d_A, d_B, d_C, "Regular");

        // cuBLAS (float)
        cudaMemset(d_C, 0, N * N * sizeof(float));
        {
            cudaEvent_t start, stop;
            CHECK_CUDA(cudaEventCreate(&start));
            CHECK_CUDA(cudaEventCreate(&stop));
            CHECK_CUDA(cudaEventRecord(start));

            cublas_gemm_float(cublas_handle, d_A, d_B, d_C, N);

            CHECK_CUDA(cudaEventRecord(stop));
            CHECK_CUDA(cudaDeviceSynchronize());
            float ms;
            CHECK_CUDA(cudaEventElapsedTime(&ms, start, stop));

            double flops = 2.0 * N * N * N / 1e9;
            double gflops = flops / (ms / 1000.0);
            printf("  cuBLAS      : %6.2fms | %6.1f GFLOPS\n", ms, gflops);

            CHECK_CUDA(cudaEventDestroy(start));
            CHECK_CUDA(cudaEventDestroy(stop));
        }

        // TensorCores CUDA
        __half *h_A_half = new __half[N * N];
        __half *h_B_half = new __half[N * N];
        for (int j = 0; j < N * N; j++) {
            h_A_half[j] = __float2half(h_A[j]);
            h_B_half[j] = __float2half(h_B[j]);
        }

        __half *d_A_half, *d_B_half;
        CHECK_CUDA(cudaMalloc(&d_A_half, N * N * sizeof(__half)));
        CHECK_CUDA(cudaMalloc(&d_B_half, N * N * sizeof(__half)));
        CHECK_CUDA(cudaMemcpy(d_A_half, h_A_half, N * N * sizeof(__half), cudaMemcpyHostToDevice));
        CHECK_CUDA(cudaMemcpy(d_B_half, h_B_half, N * N * sizeof(__half), cudaMemcpyHostToDevice));

        cudaMemset(d_C, 0, N * N * sizeof(float));
        matrix_multiply_tensor(N, d_A_half, d_B_half, d_C, "TensorCores");

        // cuBLAS (Tensor Cores через half)
        cudaMemset(d_C, 0, N * N * sizeof(float));
        {
            cudaEvent_t start, stop;
            CHECK_CUDA(cudaEventCreate(&start));
            CHECK_CUDA(cudaEventCreate(&stop));
            CHECK_CUDA(cudaEventRecord(start));

            cublas_gemm_tensor_half(cublas_handle, d_A_half, d_B_half, d_C, N);

            CHECK_CUDA(cudaEventRecord(stop));
            CHECK_CUDA(cudaDeviceSynchronize());
            float ms;
            CHECK_CUDA(cudaEventElapsedTime(&ms, start, stop));

            double flops = 2.0 * N * N * N / 1e9;
            double gflops = flops / (ms / 1000.0);
            printf("  cuBLAS TC   : %6.2fms | %6.1f GFLOPS\n", ms, gflops);

            CHECK_CUDA(cudaEventDestroy(start));
            CHECK_CUDA(cudaEventDestroy(stop));
        }

        printf("\n");

        delete[] h_A_half;
        delete[] h_B_half;
        free(h_A);
        free(h_B);
        CHECK_CUDA(cudaFree(d_A));
        CHECK_CUDA(cudaFree(d_B));
        CHECK_CUDA(cudaFree(d_C));
        CHECK_CUDA(cudaFree(d_A_half));
        CHECK_CUDA(cudaFree(d_B_half));
    }

    CHECK_CUBLAS(cublasDestroy(cublas_handle));

    return 0;
}
