#include <iostream>
#include <vector>
#include <iomanip>
#include <chrono>
#include <thrust/host_vector.h>
#include <thrust/device_vector.h>
#include <thrust/inner_product.h>
#include <thrust/transform.h>
#include <thrust/sequence.h>
#include <thrust/copy.h>
#include <thrust/iterator/counting_iterator.h>
#include <cuda_runtime.h>

#define TILE_WIDTH 32
#define BLOCK_SIZE 256
#define CHECK_CUDA(call) { cudaError_t err = call; if(err != cudaSuccess) { \
    std::cerr << "CUDA error: " << cudaGetErrorString(err) << " at " << __FILE__ << ":" << __LINE__ << std::endl; exit(1); } }

// Скалярное произведение - сырой CUDA
__global__ void dotKernel(float *a, float *b, float *c, int n) {
    extern __shared__ float sdata[];
    int tid = threadIdx.x;
    int i = blockIdx.x * blockDim.x + tid;
    sdata[tid] = (i < n) ? a[i] * b[i] : 0.0f;
    __syncthreads();

    for (int s = blockDim.x / 2; s > 0; s >>= 1) {
        if (tid < s) sdata[tid] += sdata[tid + s];
        __syncthreads();
    }
    if (tid == 0) c[blockIdx.x] = sdata[0];
}

float dotCUDA(const std::vector<float>& h_a, const std::vector<float>& h_b) {
    int n = h_a.size();
    float *d_a, *d_b, *d_c;
    int blocks = (n + BLOCK_SIZE - 1) / BLOCK_SIZE;

    CHECK_CUDA(cudaMalloc(&d_a, n * sizeof(float)));
    CHECK_CUDA(cudaMalloc(&d_b, n * sizeof(float)));
    CHECK_CUDA(cudaMalloc(&d_c, blocks * sizeof(float)));

    CHECK_CUDA(cudaMemcpy(d_a, h_a.data(), n * sizeof(float), cudaMemcpyHostToDevice));
    CHECK_CUDA(cudaMemcpy(d_b, h_b.data(), n * sizeof(float), cudaMemcpyHostToDevice));

    dotKernel<<<blocks, BLOCK_SIZE, BLOCK_SIZE * sizeof(float)>>>(d_a, d_b, d_c, n);
    CHECK_CUDA(cudaDeviceSynchronize());

    std::vector<float> h_c(blocks);
    CHECK_CUDA(cudaMemcpy(h_c.data(), d_c, blocks * sizeof(float), cudaMemcpyDeviceToHost));

    float result = 0.0f;
    for (int i = 0; i < blocks; ++i) result += h_c[i];

    CHECK_CUDA(cudaFree(d_a)); CHECK_CUDA(cudaFree(d_b)); CHECK_CUDA(cudaFree(d_c));
    return result;
}

// Транспонирование - сырой CUDA
__global__ void transposeKernel(float *in, float *out, int width, int height) {
    __shared__ float tile[TILE_WIDTH][TILE_WIDTH + 1];

    int x = blockIdx.x * TILE_WIDTH + threadIdx.x;
    int y = blockIdx.y * TILE_WIDTH + threadIdx.y;

    if (x < width && y < height)
        tile[threadIdx.y][threadIdx.x] = in[y * width + x];
    __syncthreads();

    x = blockIdx.y * TILE_WIDTH + threadIdx.x;
    y = blockIdx.x * TILE_WIDTH + threadIdx.y;

    if (x < height && y < width)
        out[y * height + x] = tile[threadIdx.x][threadIdx.y];
}

void transposeCUDA(const std::vector<float>& h_in, std::vector<float>& h_out, int width, int height) {
    size_t size = width * height * sizeof(float);
    float *d_in, *d_out;

    CHECK_CUDA(cudaMalloc(&d_in, size));
    CHECK_CUDA(cudaMalloc(&d_out, size));

    CHECK_CUDA(cudaMemcpy(d_in, h_in.data(), size, cudaMemcpyHostToDevice));

    dim3 block(TILE_WIDTH, TILE_WIDTH);
    dim3 grid((width + TILE_WIDTH - 1) / TILE_WIDTH, (height + TILE_WIDTH - 1) / TILE_WIDTH);

    transposeKernel<<<grid, block>>>(d_in, d_out, width, height);
    CHECK_CUDA(cudaDeviceSynchronize());

    CHECK_CUDA(cudaMemcpy(h_out.data(), d_out, size, cudaMemcpyDeviceToHost));

    CHECK_CUDA(cudaFree(d_in)); CHECK_CUDA(cudaFree(d_out));
}

// Thrust: Скалярное произведение
float dotThrust(int n) {
    thrust::host_vector<float> h_a(n, 1.0f);
    thrust::host_vector<float> h_b(n, 2.0f);
    thrust::device_vector<float> d_a = h_a;
    thrust::device_vector<float> d_b = h_b;
    return thrust::inner_product(d_a.begin(), d_a.end(), d_b.begin(), 0.0f);
}

// Thrust: Транспонирование
struct TransposeFunctor {
    int width;
    const thrust::device_vector<float>* input;

    TransposeFunctor(int w, const thrust::device_vector<float>* in) : width(w), input(in) {}

    __host__ __device__
    float operator()(int idx) const {
        int row = idx / width;
        int col = idx % width;
        return (*input)[col * width + row];
    }
};

void transposeThrust(const std::vector<float>& h_in, std::vector<float>& h_out, int width, int height) {
    thrust::device_vector<float> d_in(h_in.begin(), h_in.end());
    thrust::device_vector<float> d_out(width * height);

    TransposeFunctor functor(width, &d_in);
    thrust::transform(thrust::counting_iterator<int>(0),
                     thrust::counting_iterator<int>(width * height),
                     d_out.begin(), functor);

    thrust::copy(d_out.begin(), d_out.end(), h_out.begin());
}

// Измерение времени для CUDA
template<typename Func>
float measureCUDATime(Func func, int iter = 50) {
    cudaEvent_t start, stop;
    CHECK_CUDA(cudaEventCreate(&start));
    CHECK_CUDA(cudaEventCreate(&stop));

    // Прогрев
    for (int i = 0; i < iter; ++i) func();
    CHECK_CUDA(cudaDeviceSynchronize());

    float time;
    CHECK_CUDA(cudaEventRecord(start, 0));
    for (int i = 0; i < iter; ++i) func();
    CHECK_CUDA(cudaEventRecord(stop, 0));
    CHECK_CUDA(cudaEventSynchronize(stop));
    CHECK_CUDA(cudaEventElapsedTime(&time, start, stop));

    CHECK_CUDA(cudaEventDestroy(start));
    CHECK_CUDA(cudaEventDestroy(stop));
    return time / iter;
}

int main() {
    std::cout << std::fixed << std::setprecision(3);

    // 1. Скалярное произведение
    std::cout << "\n=== Скалярное произведение векторов ===\n";
    std::vector<int> vec_sizes = {1000000, 10000000};

    for (int n : vec_sizes) {
        std::vector<float> h_a(n, 1.0f), h_b(n, 2.0f);

        // CUDA время
        auto cuda_func = [&]() { dotCUDA(h_a, h_b); };
        float cuda_time = measureCUDATime(cuda_func);

        // Thrust время
        auto start = std::chrono::high_resolution_clock::now();
        float thrust_res = dotThrust(n);
        auto end = std::chrono::high_resolution_clock::now();
        float thrust_time = std::chrono::duration<float, std::milli>(end - start).count();

        std::cout << "N=" << n << ": CUDA=" << cuda_time << "мс, Thrust=" << thrust_time
                  << "мс (CUDA/Thrust=" << cuda_time/thrust_time << "x)\n";
    }

    // 2. Транспонирование - ИСПРАВЛЕНО C++11 синтаксис
    std::cout << "\n=== Транспонирование матрицы ===\n";
    int matrix_sizes[][2] = {{1024,1024}, {2048,2048}};

    for (int i = 0; i < 2; ++i) {
        int width = matrix_sizes[i][0];
        int height = matrix_sizes[i][1];
        int size = width * height;

        std::vector<float> h_in(size), h_out(size);

        // Заполнение тестовой матрицы
        for (int j = 0; j < size; ++j) h_in[j] = float(j);

        // CUDA время
        std::vector<float> cuda_out(size);
        auto cuda_trans_func = [&]() {
            transposeCUDA(h_in, cuda_out, width, height);
        };
        float cuda_time = measureCUDATime(cuda_trans_func);

        // Thrust время
        std::vector<float> thrust_out(size);
        auto start = std::chrono::high_resolution_clock::now();
        transposeThrust(h_in, thrust_out, width, height);
        auto end = std::chrono::high_resolution_clock::now();
        float thrust_time = std::chrono::duration<float, std::milli>(end - start).count();

        std::cout << width << "x" << height << ": CUDA=" << cuda_time << "мс, Thrust=" << thrust_time
                  << "мс (CUDA/Thrust=" << cuda_time/thrust_time << "x)\n";
    }

    return 0;
}
