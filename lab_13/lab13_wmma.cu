#include <stdio.h>
#include <stdlib.h>
#include <cuda_runtime.h>
#include <cublas_v2.h>
#include <mma.h>
#include <math.h>

#define CHECK_CUDA(call) { cudaError_t err = call; if (err != cudaSuccess) { \
  printf("CUDA error: %s\n", cudaGetErrorString(err)); exit(1);} }

#define CHECK_CUBLAS(call) { if(call != CUBLAS_STATUS_SUCCESS){ \
  printf("CUBLAS error\n"); exit(1);} }

using namespace nvcuda;
using half = __half;

#define WMMA_M 16
#define WMMA_N 16
#define WMMA_K 16

__global__ void wmmaKernel(half* A, half* B, float* C, int M, int N, int K) {
    int row = blockIdx.x * WMMA_M;
    int col = blockIdx.y * WMMA_N;

    if (row + WMMA_M > M || col + WMMA_N > N) return;

    wmma::fragment<wmma::matrix_a, WMMA_M, WMMA_N, WMMA_K, half, wmma::row_major> a;
    wmma::fragment<wmma::matrix_b, WMMA_M, WMMA_N, WMMA_K, half, wmma::row_major> b;
    wmma::fragment<wmma::accumulator, WMMA_M, WMMA_N, WMMA_K, float> c;

    wmma::fill_fragment(c, 0.0f);

    for (int k = 0; k < K; k += WMMA_K) {
        wmma::load_matrix_sync(a, A + row * K + k, K);
        wmma::load_matrix_sync(b, B + k * N + col, N);
        wmma::mma_sync(c, a, b, c);
    }

    wmma::store_matrix_sync(C + row * N + col, c, N, wmma::mem_row_major);
}

void init(half* a, int size) {
    for (int i = 0; i < size; i++)
        a[i] = __float2half((float)rand()/RAND_MAX);
}

bool check(float* a, float* b, int size) {
    float max_diff = 0;
    for (int i = 0; i < size; i++) {
        float d = fabs(a[i] - b[i]);
        if (d > max_diff) max_diff = d;
    }
    printf("Max diff: %.3f\n", max_diff);
    return max_diff < 0.5f;
}

double wmmaRun(half* A, half* B, float* C, int M, int N, int K) {
    dim3 grid((M+15)/16, (N+15)/16);
    dim3 block(32,1);

    cudaEvent_t s,e;
    cudaEventCreate(&s);
    cudaEventCreate(&e);

    cudaEventRecord(s);
    for(int i=0;i<10;i++)
        wmmaKernel<<<grid,block>>>(A,B,C,M,N,K);
    cudaEventRecord(e);
    cudaEventSynchronize(e);

    float ms;
    cudaEventElapsedTime(&ms,s,e);
    return ms/10;
}

double cublasRun(cublasHandle_t h, half* A, half* B, float* C, int M, int N, int K) {
    float alpha=1,beta=0;

    cudaEvent_t s,e;
    cudaEventCreate(&s);
    cudaEventCreate(&e);

    cudaEventRecord(s);
    for(int i=0;i<10;i++){
        cublasGemmEx(h,
            CUBLAS_OP_T, CUBLAS_OP_T,
            M,N,K,
            &alpha,
            A, CUDA_R_16F, K,
            B, CUDA_R_16F, N,
            &beta,
            C, CUDA_R_32F, M,
            CUDA_R_32F,
            CUBLAS_GEMM_DEFAULT_TENSOR_OP);
    }
    cudaEventRecord(e);
    cudaEventSynchronize(e);

    float ms;
    cudaEventElapsedTime(&ms,s,e);
    return ms/10;
}

int main(){
    cudaDeviceProp p;
    cudaGetDeviceProperties(&p,0);

    printf("Устройство: %s\n",p.name);
    printf("Архитектура: %d.%d\n",p.major,p.minor);
    printf("Tensor Cores: %s\n",(p.major>=7)?"Да":"Нет");
    printf("SMs: %d\n\n",p.multiProcessorCount);

    int sizes[]={64,128,256,512,1024};

    printf("Size      WMMA(ms)  cuBLAS(ms)  GFLOPS   Speedup   OK\n");
    printf("----------------------------------------------------------\n");

    cublasHandle_t h;
    cublasCreate(&h);
    cublasSetMathMode(h,CUBLAS_TENSOR_OP_MATH);

    for(int s=0;s<5;s++){
        int M=sizes[s],N=sizes[s],K=sizes[s];

        size_t As=M*K*sizeof(half);
        size_t Bs=K*N*sizeof(half);
        size_t Cs=M*N*sizeof(float);

        half *hA,*hB;
        float *hC1,*hC2;

        cudaHostAlloc(&hA,As,0);
        cudaHostAlloc(&hB,Bs,0);
        cudaHostAlloc(&hC1,Cs,0);
        cudaHostAlloc(&hC2,Cs,0);

        init(hA,M*K);
        init(hB,K*N);

        half *dA,*dB;
        float *dC1,*dC2;

        cudaMalloc(&dA,As);
        cudaMalloc(&dB,Bs);
        cudaMalloc(&dC1,Cs);
        cudaMalloc(&dC2,Cs);

        cudaMemcpy(dA,hA,As,cudaMemcpyHostToDevice);
        cudaMemcpy(dB,hB,Bs,cudaMemcpyHostToDevice);

        double t1=wmmaRun(dA,dB,dC1,M,N,K);
        double t2=cublasRun(h,dA,dB,dC2,M,N,K);

        cudaMemcpy(hC1,dC1,Cs,cudaMemcpyDeviceToHost);
        cudaMemcpy(hC2,dC2,Cs,cudaMemcpyDeviceToHost);

        double gflops=(2.0*M*N*K)/(t1*1e6);

        bool ok=check(hC1,hC2,M*N);

        printf("%dx%dx%d  %8.3f  %8.3f  %8.1f   %6.2fx   %s\n",
            M,M,M,t1,t2,gflops,t1>0?t2/t1:0,ok?"✓":"✗");

        cudaFree(dA); cudaFree(dB);
        cudaFree(dC1); cudaFree(dC2);
    }

    return 0;
}
