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

__global__ void reorderKernelSpill(const float *a, float *b, int N, int K)
{
    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    int total = N * K;

    float temp[256];
    volatile int tid = threadIdx.x % 256;

    if (idx >= total) return;

    int i = idx / K;
    int j = idx % K;
    int dst = j * N + i;

    temp[tid] = a[idx];
    float val1 = temp[tid];
    float val2 = temp[tid];
    float val3 = temp[tid];
    float computed = val1 + val2 * 1.1f + val3 * 0.9f;

    b[dst] = computed;
}

int main()
{
    int N = 4096;
    int K = 4096;
    size_t size = N * K * sizeof(float);

    std::cout << "Matrix: " << N << "x" << K << " = " << N*K << " elements" << std::endl;

    float *a = (float*)malloc(size);
    float *b = (float*)malloc(size);
    float *b_ref = (float*)malloc(size);

    for (int i = 0; i < N*K; i++) {
        a[i] = i;
        b[i] = 0;
        b_ref[i] = 0;
    }

    float *d_a, *d_b;
    CHECK(cudaMalloc(&d_a, size));
    CHECK(cudaMalloc(&d_b, size));

    CHECK(cudaMemcpy(d_a, a, size, cudaMemcpyHostToDevice));

    int block = 256;
    int grid = (N*K + block - 1) / block;

    std::cout << "Grid: " << grid << ", Block: " << block << std::endl;

    CHECK(cudaMemset(d_b, 0, size));

    std::cout << "\n=== Kernel 1: reorderKernel (no spill) ===" << std::endl;
    CHECK(cudaMemset(d_b, 0, size));
    reorderKernel<<<grid, block>>>(d_a, d_b, N, K);
    CHECK(cudaDeviceSynchronize());

    std::cout << "\n=== Kernel 2: reorderKernelSpill (WITH spill) ===" << std::endl;
    CHECK(cudaMemset(d_b, 0, size));
    reorderKernelSpill<<<grid, block>>>(d_a, d_b, N, K);
    CHECK(cudaDeviceSynchronize());

    CHECK(cudaMemcpy(b, d_b, size, cudaMemcpyDeviceToHost));

    std::cout << "\nFirst 10 results (should be ~transformed values):" << std::endl;
    for (int i = 0; i < 10; i++) {
        std::cout << "b[" << i << "] = " << b[i] << std::endl;
    }

    std::cout << "\nDone! Profile with: ncu --section MemoryWorkloadAnalysis ./lab5" << std::endl;

    cudaFree(d_a);
    cudaFree(d_b);
    free(a); free(b); free(b_ref);

    return 0;
}
