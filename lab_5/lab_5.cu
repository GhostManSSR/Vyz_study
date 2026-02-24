#include <iostream>
#include <cuda_runtime.h>

#define CHECK(call) \
{ \
    cudaError_t err = call; \
    if (err != cudaSuccess) \
    { \
        std::cerr << "CUDA error: " << cudaGetErrorString(err) << std::endl; \
        exit(1); \
    } \
}

////////////////////////////////////////////////////////////////////////////////
// Ядро 1 — обычное копирование по схеме
////////////////////////////////////////////////////////////////////////////////

__global__ void reorderKernel(const float *a, float *b, int N, int K)
{
    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    int total = N * K;

    if (idx >= total) return;

    int i = idx / K;
    int j = idx % K;

    int dst = j * N + i;

    b[dst] = a[idx];
}

////////////////////////////////////////////////////////////////////////////////
// Ядро 2 — искусственная нехватка регистров
////////////////////////////////////////////////////////////////////////////////

__global__ void reorderKernelLocal(const float *a, float *b, int N, int K)
{
    int idx = blockIdx.x * blockDim.x + threadIdx.x;

    float temp[1024];   // большое давление на регистры

    int total = N * K;
    if (idx >= total) return;

    int i = idx / K;
    int j = idx % K;

    int dst = j * N + i;

    temp[threadIdx.x % 1024] = a[idx];

    b[dst] = temp[threadIdx.x % 1024];
}

////////////////////////////////////////////////////////////////////////////////

int main()
{
    int N = 4096;
    int K = 4096;

    size_t size = N * K * sizeof(float);

    std::cout << "Elements: " << N*K << std::endl;

    float *a = (float*)malloc(size);
    float *b = (float*)malloc(size);

    for (int i = 0; i < N*K; i++)
        a[i] = i;

    float *d_a;
    float *d_b;

    CHECK(cudaMalloc(&d_a, size));
    CHECK(cudaMalloc(&d_b, size));

    CHECK(cudaMemcpy(d_a, a, size, cudaMemcpyHostToDevice));

    int block = 256;
    int grid = (N*K + block - 1) / block;

    ////////////////////////////////////////////////////////////
    // Kernel 1
    ////////////////////////////////////////////////////////////

    reorderKernel<<<grid, block>>>(d_a, d_b, N, K);
    CHECK(cudaDeviceSynchronize());

    ////////////////////////////////////////////////////////////
    // Kernel 2
    ////////////////////////////////////////////////////////////

    reorderKernelLocal<<<grid, block>>>(d_a, d_b, N, K);
    CHECK(cudaDeviceSynchronize());

    CHECK(cudaMemcpy(b, d_b, size, cudaMemcpyDeviceToHost));

    std::cout << "Done" << std::endl;

    cudaFree(d_a);
    cudaFree(d_b);

    free(a);
    free(b);

    return 0;
}
