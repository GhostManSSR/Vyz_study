// cuMatrixMul.c (линковка: nvcc -ldl matrix_mul.cu)
#include <cuda.h>
#include <stdio.h>

__global__ void matrixMulKernel(float *C, float *A, float *B, int N) {
    // Тот же kernel
}

int main() {
    CUdevice device;
    CUcontext ctx;
    cuInit(0);
    cuDeviceGet(&device, 0);
    cuCtxCreate(&ctx, 0, device);

    CUmodule module;
    CUfunction function;
    cuModuleLoad(&module, "matrix_mul.ptx");  // Предкомпилированный PTX
    cuModuleGetFunction(&function, module, "matrixMulKernel");

    // Выделение памяти: cuMemAlloc, cuMemcpyHtoD
    // Запуск: cuLaunchKernel с параметрами grid/block
    // Замеры времени аналогично

    cuCtxDestroy(ctx);
}
