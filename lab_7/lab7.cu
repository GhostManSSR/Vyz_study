#include <cuda_runtime.h>
#include <device_launch_parameters.h>
#include <stdio.h>
#include <math.h>
#include <string.h>

#define N 512           // 1GB → помещается в 6-8GB VRAM
#define BLOCK_SIZE 256
#define NUM_ITER 500   // Меньше итераций (быстрее)
#define PI 3.141592653589793f

const float R = 1.0f;
const float EPS = 0.002f;  // Больше точек → больше интерполяций

// Интерполяции (компактные)
__device__ float trilinear_interp(float* f, float x, float y, float z, float dev_R, float dev_dx) {
    float xd = (x + dev_R) / dev_dx, yd = (y + dev_R) / dev_dx, zd = (z + dev_R) / dev_dx;
    int i = floorf(xd), j = floorf(yd), k = floorf(zd);
    if (i < 1 || i >= N-1 || j < 1 || j >= N-1 || k < 1 || k >= N-1) return 0.0f;

    float fx = xd - i, fy = yd - j, fz = zd - k;
    float c000 = f[k*N*N + j*N + i], c100 = f[k*N*N + j*N + (i+1)];
    float c010 = f[k*N*N + (j+1)*N + i], c110 = f[k*N*N + (j+1)*N + (i+1)];
    float c001 = f[(k+1)*N*N + j*N + i], c101 = f[(k+1)*N*N + j*N + (i+1)];
    float c011 = f[(k+1)*N*N + (j+1)*N + i], c111 = f[(k+1)*N*N + (j+1)*N + (i+1)];

    return c000*(1-fx)*(1-fy)*(1-fz) + c100*fx*(1-fy)*(1-fz) + c010*(1-fx)*fy*(1-fz) +
           c110*fx*fy*(1-fz) + c001*(1-fx)*(1-fy)*fz + c101*fx*(1-fy)*fz +
           c011*(1-fx)*fy*fz + c111*fx*fy*fz;
}

__device__ float stepwise_interp(float* f, float x, float y, float z, float dev_R, float dev_dx) {
    float xd = (x + dev_R) / dev_dx, yd = (y + dev_R) / dev_dx, zd = (z + dev_R) / dev_dx;
    int i = roundf(xd), j = roundf(yd), k = roundf(zd);
    if (i < 0 || i >= N || j < 0 || j >= N || k < 0 || k >= N) return 0.0f;
    return f[k*N*N + j*N + i];
}

__global__ void compute_integral_texture(double* partial_sums, cudaTextureObject_t tex_f,
                                        float dev_R, float dev_dx, float dev_eps, float dev_area) {
    extern __shared__ double sdata[];
    sdata[threadIdx.x] = 0.0; __syncthreads();

    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    if (idx >= N*N*N) return;

    int k = idx / (N*N), j = (idx % (N*N)) / N, i = idx % N;
    float x = -dev_R + i * dev_dx, y = -dev_R + j * dev_dx, z = -dev_R + k * dev_dx;
    float r = sqrtf(x*x + y*y + z*z);

    if (fabsf(r - dev_R) < dev_eps) {
        float u = (x + dev_R) / (2.0f * dev_R);
        float v = (y + dev_R) / (2.0f * dev_R);
        float w = (z + dev_R) / (2.0f * dev_R);
        sdata[threadIdx.x] = tex3D<float>(tex_f, u, v, w) * dev_area;
    }

    __syncthreads();
    for (int s = BLOCK_SIZE/2; s > 0; s >>= 1) {
        if (threadIdx.x < s) sdata[threadIdx.x] += sdata[threadIdx.x + s];
        __syncthreads();
    }
    if (threadIdx.x == 0) atomicAdd(partial_sums, sdata[0]);
}

// LINEAR + REDUCTION
__global__ void compute_integral_linear(double* partial_sums, float* f,
                                       float dev_R, float dev_dx, float dev_eps, float dev_area) {
    extern __shared__ double sdata[];
    sdata[threadIdx.x] = 0.0; __syncthreads();

    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    if (idx >= N*N*N) return;

    int k = idx / (N*N), j = (idx % (N*N)) / N, i = idx % N;
    float x = -dev_R + i * dev_dx, y = -dev_R + j * dev_dx, z = -dev_R + k * dev_dx;
    float r = sqrtf(x*x + y*y + z*z);

    if (fabsf(r - dev_R) < dev_eps) {
        sdata[threadIdx.x] = trilinear_interp(f, x, y, z, dev_R, dev_dx) * dev_area;
    }

    __syncthreads();
    for (int s = BLOCK_SIZE/2; s > 0; s >>= 1) {
        if (threadIdx.x < s) sdata[threadIdx.x] += sdata[threadIdx.x + s];
        __syncthreads();
    }
    if (threadIdx.x == 0) atomicAdd(partial_sums, sdata[0]);
}

// STEP + REDUCTION
__global__ void compute_integral_step(double* partial_sums, float* f,
                                     float dev_R, float dev_dx, float dev_eps, float dev_area) {
    extern __shared__ double sdata[];
    sdata[threadIdx.x] = 0.0; __syncthreads();

    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    if (idx >= N*N*N) return;

    int k = idx / (N*N), j = (idx % (N*N)) / N, i = idx % N;
    float x = -dev_R + i * dev_dx, y = -dev_R + j * dev_dx, z = -dev_R + k * dev_dx;
    float r = sqrtf(x*x + y*y + z*z);

    if (fabsf(r - dev_R) < dev_eps) {
        sdata[threadIdx.x] = stepwise_interp(f, x, y, z, dev_R, dev_dx) * dev_area;
    }

    __syncthreads();
    for (int s = BLOCK_SIZE/2; s > 0; s >>= 1) {
        if (threadIdx.x < s) sdata[threadIdx.x] += sdata[threadIdx.x + s];
        __syncthreads();
    }
    if (threadIdx.x == 0) atomicAdd(partial_sums, sdata[0]);
}

int main() {
    float dx = 2.0f * R / N;
    float shell_thickness = 2 * EPS;
    float shell_volume = 4 * PI * R * R * shell_thickness;
    float total_volume = 8 * R * R * R;
    float fraction = shell_volume / total_volume;
    float num_surf_points = fraction * N * N * N;
    float area = 4 * PI * R * R / num_surf_points;

    printf("N=%d, dx=%.4f, EPS=%.4f, точки=%.0f\n", N, dx, EPS, num_surf_points);
    printf("Площадь: %.6f\n", area);

    size_t size = N*N*N * sizeof(float);
    printf("Размер: %.1f GB\n\n", size/1e9f);

    float* h_f = (float*)malloc(size);
    for (int k = 0; k < N; ++k) for (int j = 0; j < N; ++j) for (int i = 0; i < N; ++i) {
        float x = -R + i * dx, y = -R + j * dx, z = -R + k * dx;
        h_f[k*N*N + j*N + i] = 1.0f + x + y + z;
    }

    float* d_f; double* d_partial_sums;
    cudaMalloc(&d_f, size);
    cudaMalloc(&d_partial_sums, 2048 * sizeof(double));
    cudaMemcpy(d_f, h_f, size, cudaMemcpyHostToDevice);

    cudaChannelFormatDesc channelDesc = cudaCreateChannelDesc<float>();
    cudaArray* d_array;
    cudaExtent extent = make_cudaExtent(N, N, N);
    cudaMalloc3DArray(&d_array, &channelDesc, extent);

    cudaMemcpy3DParms copyParams = {0};
    copyParams.srcPtr = make_cudaPitchedPtr(h_f, N*sizeof(float), N, N);
    copyParams.dstArray = d_array; copyParams.extent = extent;
    copyParams.kind = cudaMemcpyHostToDevice;
    cudaMemcpy3D(&copyParams);

    cudaResourceDesc resDesc; memset(&resDesc, 0, sizeof(resDesc));
    resDesc.resType = cudaResourceTypeArray; resDesc.res.array.array = d_array;

    cudaTextureDesc texDesc; memset(&texDesc, 0, sizeof(texDesc));
    texDesc.addressMode[0] = texDesc.addressMode[1] = texDesc.addressMode[2] = cudaAddressModeClamp;
    texDesc.filterMode = cudaFilterModeLinear; texDesc.readMode = cudaReadModeElementType;
    texDesc.normalizedCoords = 1;

    cudaTextureObject_t tex_f = 0;
    cudaCreateTextureObject(&tex_f, &resDesc, &texDesc, NULL);

    dim3 block(BLOCK_SIZE);
    dim3 grid((N*N*N + BLOCK_SIZE - 1) / BLOCK_SIZE);
    cudaEvent_t start, stop;
    cudaEventCreate(&start); cudaEventCreate(&stop);

    printf("1. Текстура...\n");
    cudaMemset(d_partial_sums, 0, 2048 * sizeof(double));
    for(int w = 0; w < 10; w++) compute_integral_texture<<<grid, block, BLOCK_SIZE*sizeof(double)>>>(d_partial_sums, tex_f, R, dx, EPS, area);
    cudaDeviceSynchronize();

    cudaEventRecord(start);
    for(int iter = 0; iter < NUM_ITER; iter++) {
        cudaMemset(d_partial_sums, 0, 2048 * sizeof(double));
        compute_integral_texture<<<grid, block, BLOCK_SIZE*sizeof(double)>>>(d_partial_sums, tex_f, R, dx, EPS, area);
    }
    cudaDeviceSynchronize(); cudaEventRecord(stop); cudaEventSynchronize(stop);

    float time_tex; cudaEventElapsedTime(&time_tex, start, stop); time_tex /= NUM_ITER;
    double* h_partial = new double[2048];
    cudaMemcpy(h_partial, d_partial_sums, 2048 * sizeof(double), cudaMemcpyDeviceToHost);
    double result_tex = 0; for(int i = 0; i < 2048; i++) result_tex += h_partial[i];
    printf("Текстура: %.3f мс → %.6f\n", time_tex, result_tex);

    // 2. Трилинейная
    printf("2. Трилинейная...\n");
    cudaMemset(d_partial_sums, 0, 2048 * sizeof(double));
    for(int w = 0; w < 10; w++) compute_integral_linear<<<grid, block, BLOCK_SIZE*sizeof(double)>>>(d_partial_sums, d_f, R, dx, EPS, area);
    cudaDeviceSynchronize();

    cudaEventRecord(start);
    for(int iter = 0; iter < NUM_ITER; iter++) {
        cudaMemset(d_partial_sums, 0, 2048 * sizeof(double));
        compute_integral_linear<<<grid, block, BLOCK_SIZE*sizeof(double)>>>(d_partial_sums, d_f, R, dx, EPS, area);
    }
    cudaDeviceSynchronize(); cudaEventRecord(stop); cudaEventSynchronize(stop);

    float time_lin; cudaEventElapsedTime(&time_lin, start, stop); time_lin /= NUM_ITER;
    cudaMemcpy(h_partial, d_partial_sums, 2048 * sizeof(double), cudaMemcpyDeviceToHost);
    double result_lin = 0; for(int i = 0; i < 2048; i++) result_lin += h_partial[i];
    printf("Трилинейная: %.3f мс → %.6f (%.1fx)\n", time_lin, result_lin, time_lin/time_tex);

    // 3. Ступенчатая
    printf("3. Ступенчатая...\n");
    cudaMemset(d_partial_sums, 0, 2048 * sizeof(double));
    for(int w = 0; w < 10; w++) compute_integral_step<<<grid, block, BLOCK_SIZE*sizeof(double)>>>(d_partial_sums, d_f, R, dx, EPS, area);
    cudaDeviceSynchronize();

    cudaEventRecord(start);
    for(int iter = 0; iter < NUM_ITER; iter++) {
        cudaMemset(d_partial_sums, 0, 2048 * sizeof(double));
        compute_integral_step<<<grid, block, BLOCK_SIZE*sizeof(double)>>>(d_partial_sums, d_f, R, dx, EPS, area);
    }
    cudaDeviceSynchronize(); cudaEventRecord(stop); cudaEventSynchronize(stop);

    float time_step; cudaEventElapsedTime(&time_step, start, stop); time_step /= NUM_ITER;
    cudaMemcpy(h_partial, d_partial_sums, 2048 * sizeof(double), cudaMemcpyDeviceToHost);
    double result_step = 0; for(int i = 0; i < 2048; i++) result_step += h_partial[i];
    printf("Ступенчатая: %.3f мс → %.6f (%.1fx)\n", time_step, result_step, time_step/time_tex);

    double analytic = 4 * PI * R * R;
    printf("\nАналитическое: %.6f\n", analytic);
    printf("Текстура выигрывает: %.1fx (трилинейная), %.1fx (ступенчатая)\n",
           time_lin/time_tex, time_step/time_tex);

    delete[] h_partial;
    cudaDestroyTextureObject(tex_f); cudaFreeArray(d_array);
    cudaFree(d_f); cudaFree(d_partial_sums); free(h_f);
    cudaEventDestroy(start); cudaEventDestroy(stop);
    return 0;
}
