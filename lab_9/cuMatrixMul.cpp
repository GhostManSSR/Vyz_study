#include <cuda.h>
#include <iostream>
#include <iomanip>
#include <vector>
#include <chrono>
#include <cmath>

CUresult check(CUresult res, const char* op) {
    if (res != CUDA_SUCCESS) {
        std::cerr << op << " error: " << res << std::endl;
    }
    return res;
}

int main() {
    const int N = 1024;
    size_t size = N * N * sizeof(float);

    CUdevice device; CUcontext ctx; CUmodule module; CUfunction kernel;
    check(cuInit(0), "cuInit");
    check(cuDeviceGet(&device, 0), "cuDeviceGet");
    check(cuCtxCreate(&ctx, 0, device), "cuCtxCreate");

    std::vector<float> h_A(N*N, 1.0f), h_B(N*N, 2.0f), h_C(N*N), h_C_ref(N*N);

    std::cout << "Вычисление эталона..." << std::endl;
    for(int i = 0; i < N; i++)
        for(int j = 0; j < N; j++) {
            h_C_ref[i*N+j] = 0.0f;
            for(int k = 0; k < N; k++)
                h_C_ref[i*N+j] += h_A[i*N+k] * h_B[k*N+j];
        }

    CUdeviceptr d_A, d_B, d_C;
    check(cuMemAlloc(&d_A, size), "memAlloc A");
    check(cuMemAlloc(&d_B, size), "memAlloc B");
    check(cuMemAlloc(&d_C, size), "memAlloc C");

    check(cuMemcpyHtoD(d_A, h_A.data(), size), "HtoD A");
    check(cuMemcpyHtoD(d_B, h_B.data(), size), "HtoD B");

    check(cuModuleLoad(&module, "matrix_mul.ptx"), "cuModuleLoad");

    CUresult funcRes = cuModuleGetFunction(&kernel, module, "_Z15matrixMulKernelPfS_S_i");
    if (funcRes != CUDA_SUCCESS) {
        std::cerr << "Kernel не найден!" << std::endl;
        return 1;
    }

    int grid_x = 32, grid_y = 128;
    int block_x = 32, block_y = 8, block_z = 1;

    std::cout << "\nРазмер матриц: " << N << "x" << N << std::endl;
    std::cout << "Grid: (" << grid_x << ", " << grid_y << "), Block: ("
              << block_x << ", " << block_y << ", " << block_z << ")" << std::endl;

    void *args[] = {&d_C, &d_A, &d_B, (void*)&N};

    auto start = std::chrono::high_resolution_clock::now();
    check(cuLaunchKernel(kernel, grid_x, grid_y, block_z,
                        block_x, block_y, block_z, 0, NULL, args, NULL), "launch");
    check(cuCtxSynchronize(), "sync");
    auto end = std::chrono::high_resolution_clock::now();

    auto duration_ms = std::chrono::duration_cast<std::chrono::microseconds>(end - start).count() / 1000.0;

    check(cuMemcpyDtoH(h_C.data(), d_C, size), "DtoH");

    float max_err = 0.0f, avg_err = 0.0f;
    for(int i = 0; i < N*N; i++) {
        float err = std::abs(h_C[i] - h_C_ref[i]);
        if (err > max_err) max_err = err;
        avg_err += err;
    }
    avg_err /= (N*N);

    double flops = 2.0 * N * N * N;
    double gflops = flops / 1e9 / (duration_ms / 1000.0);

    std::cout << "\nРЕЗУЛЬТАТЫ:" << std::endl;
    std::cout << "Время kernel: " << std::fixed << std::setprecision(2)
              << duration_ms << " ms" << std::endl;
    std::cout << "Производительность: " << std::setprecision(1)
              << gflops << " GFLOPS" << std::endl;
    std::cout << "Макс. ошибка: " << std::scientific << std::setprecision(2)
              << max_err << std::endl;
    std::cout << "Средняя ошибка: " << std::scientific << std::setprecision(2)
              << avg_err << std::endl;
    std::cout << (max_err < 1e-3 ? "Верно" : "ОШИБКА!") << std::endl;

    cuMemFree(d_A); cuMemFree(d_B); cuMemFree(d_C);
    cuModuleUnload(module); cuCtxDestroy(ctx);

    return 0;
}
