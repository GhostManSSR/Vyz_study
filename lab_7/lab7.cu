#include <cuda_runtime.h>
#include <device_launch_parameters.h>
#include <stdio.h>
#include <math.h>
#include <string.h>

#define N 128
#define BLOCK_SIZE 256
#define NUM_ITER 1000  // Количество итераций для точного замера времени
#define PI 3.141592653589793f

const float R = 1.0f;
const float EPS = 0.01f;

__device__ float trilinear_interp(float* f, float x, float y, float z, float dev_R, float dev_dx) {
    float xd = (x + dev_R) / dev_dx;
    float yd = (y + dev_R) / dev_dx;
    float zd = (z + dev_R) / dev_dx;

    int i = floorf(xd);
    int j = floorf(yd);
    int k = floorf(zd);

    if (i < 1 || i >= N-1 || j < 1 || j >= N-1 || k < 1 || k >= N-1)
        return 0.0f;

    float fx = xd - i, fy = yd - j, fz = zd - k;

    float c000 = f[k*N*N + j*N + i];
    float c100 = f[k*N*N + j*N + (i+1)];
    float c010 = f[k*N*N + (j+1)*N + i];
    float c110 = f[k*N*N + (j+1)*N + (i+1)];
    float c001 = f[(k+1)*N*N + j*N + i];
    float c101 = f[(k+1)*N*N + j*N + (i+1)];
    float c011 = f[(k+1)*N*N + (j+1)*N + i];
    float c111 = f[(k+1)*N*N + (j+1)*N + (i+1)];

    return c000*(1-fx)*(1-fy)*(1-fz) + c100*fx*(1-fy)*(1-fz) +
           c010*(1-fx)*fy*(1-fz) + c110*fx*fy*(1-fz) +
           c001*(1-fx)*(1-fy)*fz + c101*fx*(1-fy)*fz +
           c011*(1-fx)*fy*fz + c111*fx*fy*fz;
}

__device__ float stepwise_interp(float* f, float x, float y, float z, float dev_R, float dev_dx) {
    float xd = (x + dev_R) / dev_dx;
    float yd = (y + dev_R) / dev_dx;
    float zd = (z + dev_R) / dev_dx;

    int i = roundf(xd), j = roundf(yd), k = roundf(zd);
    if (i < 0 || i >= N || j < 0 || j >= N || k < 0 || k >= N)
        return 0.0f;

    return f[k*N*N + j*N + i];
}

// Ядро 1: Текстурная память + аппаратная интерполяция
__global__ void compute_integral_texture(double* result, cudaTextureObject_t tex_f,
                                        float dev_R, float dev_dx, float dev_eps, float dev_area) {
    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    if (idx >= N*N*N) return;

    int k = idx / (N*N), j = (idx % (N*N)) / N, i = idx % N;
    float x = -dev_R + i * dev_dx;
    float y = -dev_R + j * dev_dx;
    float z = -dev_R + k * dev_dx;

    float r = sqrtf(x*x + y*y + z*z);
    if (fabsf(r - dev_R) < dev_eps) {
        float u = (x + dev_R) / (2 * dev_R);
        float v = (y + dev_R) / (2 * dev_R);
        float w = (z + dev_R) / (2 * dev_R);
        float f_val = tex3D<float>(tex_f, u, v, w);
        atomicAdd(result, (double)(f_val * dev_area));
    }
}

// Ядро 2: Программная трилинейная интерполяция
__global__ void compute_integral_linear(double* result, float* f,
                                       float dev_R, float dev_dx, float dev_eps, float dev_area) {
    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    if (idx >= N*N*N) return;

    int k = idx / (N*N), j = (idx % (N*N)) / N, i = idx % N;
    float x = -dev_R + i * dev_dx;
    float y = -dev_R + j * dev_dx;
    float z = -dev_R + k * dev_dx;

    float r = sqrtf(x*x + y*y + z*z);
    if (fabsf(r - dev_R) < dev_eps) {
        float f_val = trilinear_interp(f, x, y, z, dev_R, dev_dx);
        atomicAdd(result, (double)(f_val * dev_area));
    }
}

// Ядро 3: Программная ступенчатая интерполяция
__global__ void compute_integral_step(double* result, float* f,
                                     float dev_R, float dev_dx, float dev_eps, float dev_area) {
    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    if (idx >= N*N*N) return;

    int k = idx / (N*N), j = (idx % (N*N)) / N, i = idx % N;
    float x = -dev_R + i * dev_dx;
    float y = -dev_R + j * dev_dx;
    float z = -dev_R + k * dev_dx;

    float r = sqrtf(x*x + y*y + z*z);
    if (fabsf(r - dev_R) < dev_eps) {
        float f_val = stepwise_interp(f, x, y, z, dev_R, dev_dx);
        atomicAdd(result, (double)(f_val * dev_area));
    }
}

int main() {
    printf("=== ЛАБОРАТОРНАЯ РАБОТА 7: Текстурная vs константная память ===\n");

    float dx = 2.0f * R / (N - 1);
    float shell_thickness = 2 * EPS;  // Толщина оболочки ±EPS
    float shell_volume = 4 * PI * R * R * shell_thickness;
    float total_volume = 8 * R * R * R;
    float fraction = shell_volume / total_volume;
    float num_surf_points = fraction * N * N * N;
    float area = 4 * PI * R * R / num_surf_points;

    printf("Параметры: N=%d, dx=%.4f, EPS=%.4f, ожидаемое кол-во точек=%.0f\n",
           N, dx, EPS, num_surf_points);
    printf("Площадь элемента: %.6f\n\n", area);

    // Генерация тестовой функции f(x,y,z) = 1 + x + y + z
    size_t size = N*N*N * sizeof(float);
    float* h_f = (float*)malloc(size);

    float dx_host = 2.0f * R / (N - 1);
    for (int k = 0; k < N; ++k) {
        for (int j = 0; j < N; ++j) {
            for (int i = 0; i < N; ++i) {
                float x = -R + i * dx_host;
                float y = -R + j * dx_host;
                float z = -R + k * dx_host;
                h_f[k * N * N + j * N + i] = 1.0f + x + y + z;  // Аналитический интеграл = 4πR²
            }
        }
    }

    // GPU память
    float* d_f;
    double* d_result;
    cudaMalloc(&d_f, size);
    cudaMalloc(&d_result, sizeof(double));
    cudaMemcpy(d_f, h_f, size, cudaMemcpyHostToDevice);

    // Создание 3D текстуры
    cudaChannelFormatDesc channelDesc = cudaCreateChannelDesc<float>();
    cudaArray* d_array;
    cudaExtent extent = make_cudaExtent(N, N, N);
    cudaMalloc3DArray(&d_array, &channelDesc, extent);

    cudaMemcpy3DParms copyParams = {0};
    copyParams.srcPtr = make_cudaPitchedPtr(h_f, N*sizeof(float), N, N);
    copyParams.dstArray = d_array;
    copyParams.extent = extent;
    copyParams.kind = cudaMemcpyHostToDevice;
    cudaMemcpy3D(&copyParams);

    cudaResourceDesc resDesc;
    memset(&resDesc, 0, sizeof(resDesc));
    resDesc.resType = cudaResourceTypeArray;
    resDesc.res.array.array = d_array;

    cudaTextureDesc texDesc;
    memset(&texDesc, 0, sizeof(texDesc));
    texDesc.addressMode[0] = cudaAddressModeClamp;
    texDesc.addressMode[1] = cudaAddressModeClamp;
    texDesc.addressMode[2] = cudaAddressModeClamp;
    texDesc.filterMode = cudaFilterModeLinear;
    texDesc.readMode = cudaReadModeElementType;
    texDesc.normalizedCoords = 1;

    cudaTextureObject_t tex_f;
    cudaCreateTextureObject(&tex_f, &resDesc, &texDesc, NULL);

    // Подготовка запуска
    dim3 block(BLOCK_SIZE);
    dim3 grid((N*N*N + BLOCK_SIZE - 1) / BLOCK_SIZE);
    cudaEvent_t start, stop;
    cudaEventCreate(&start);
    cudaEventCreate(&stop);

    float time_tex = 0, time_lin = 0, time_step = 0;

    printf("Тест 1: Текстурная память + аппаратная интерполяция...\n");
    for(int iter = 0; iter < NUM_ITER; iter++) {
        cudaMemset(d_result, 0, sizeof(double));
        compute_integral_texture<<<grid, block>>>(d_result, tex_f, R, dx, EPS, area);
    }
    cudaDeviceSynchronize();
    cudaEventRecord(start);
    for(int iter = 0; iter < NUM_ITER; iter++) {
        cudaMemset(d_result, 0, sizeof(double));
        compute_integral_texture<<<grid, block>>>(d_result, tex_f, R, dx, EPS, area);
    }
    cudaDeviceSynchronize();
    cudaEventRecord(stop);
    cudaEventElapsedTime(&time_tex, start, stop);
    double result_tex;
    cudaMemcpy(&result_tex, d_result, sizeof(double), cudaMemcpyDeviceToHost);
    time_tex /= NUM_ITER;
    printf("Текстура: %.3f мс, результат: %.6f\n", time_tex, result_tex);

    printf("Тест 2: Программная трилинейная интерполяция...\n");
    cudaEventRecord(start);
    for(int iter = 0; iter < NUM_ITER; iter++) {
        cudaMemset(d_result, 0, sizeof(double));
        compute_integral_linear<<<grid, block>>>(d_result, d_f, R, dx, EPS, area);
    }
    cudaDeviceSynchronize();
    cudaEventRecord(stop);
    cudaEventElapsedTime(&time_lin, start, stop);
    double result_lin;
    cudaMemcpy(&result_lin, d_result, sizeof(double), cudaMemcpyDeviceToHost);
    time_lin /= NUM_ITER;
    printf("Линейная: %.3f мс, результат: %.6f (ускорение: %.1fx)\n", time_lin, result_lin, time_lin/time_tex);

    printf("Тест 3: Программная ступенчатая интерполяция...\n");
    cudaEventRecord(start);
    for(int iter = 0; iter < NUM_ITER; iter++) {
        cudaMemset(d_result, 0, sizeof(double));
        compute_integral_step<<<grid, block>>>(d_result, d_f, R, dx, EPS, area);
    }
    cudaDeviceSynchronize();
    cudaEventRecord(stop);
    cudaEventElapsedTime(&time_step, start, stop);
    double result_step;
    cudaMemcpy(&result_step, d_result, sizeof(double), cudaMemcpyDeviceToHost);
    time_step /= NUM_ITER;
    printf("Ступенчатая: %.3f мс, результат: %.6f\n", time_step, result_step);

    double analytic = 4 * PI * R * R;
    printf("\n" "=" "\n");
    printf("АНАЛИТИЧЕСКОЕ ЗНАЧЕНИЕ (∫(1+x+y+z)dS = 4πR²): %.6f\n", analytic);
    printf("ОТНОСИТЕЛЬНЫЕ ОШИБКИ:\n");
    printf("   Текстура:  %.2e\n", fabs(result_tex - analytic));
    printf("   Линейная:  %.2e\n", fabs(result_lin - analytic));
    printf("   Ступенчатая: %.2e\n", fabs(result_step - analytic));
    printf("УСКОРЕНИЕ ТЕКСТУРНОЙ ПАМЯТИ:\n");
    printf("   vs Линейная: %.1fx\n", time_lin / time_tex);
    printf("   vs Ступенчатая: %.1fx\n", time_step / time_tex);
    printf("=""\n");

    // Очистка памяти
    cudaDestroyTextureObject(tex_f);
    cudaFreeArray(d_array);
    cudaFree(d_f);
    cudaFree(d_result);
    free(h_f);
    cudaEventDestroy(start);
    cudaEventDestroy(stop);

    return 0;
}
