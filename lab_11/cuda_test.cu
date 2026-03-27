#include <stdio.h>
#include <stdlib.h>
#include <cuda_runtime.h>
#include <math.h>
#include <time.h>

#define CHECK_CUDA(call) { cudaError_t err = call; if (err != cudaSuccess) { \
  fprintf(stderr, "CUDA error at %s:%d: %s\n", __FILE__, __LINE__, cudaGetErrorString(err)); exit(1); } }

#define N_ELEMENTS 100 * 1024 * 1024  // 100M float ~400MB
#define N_STREAMS 4
#define BLOCK_SIZE 256
#define N_TEST_CHUNKS 12

// Kernel для сложения векторов
__global__ void vectorAdd(const float *a, const float *b, float *c, size_t n) {
  size_t idx = blockIdx.x * blockDim.x + threadIdx.x;
  if (idx < n) c[idx] = a[idx] + b[idx];
}

// Kernel для скалярного умножения
__global__ void scalarMul(const float *a, float scalar, float *c, size_t n) {
  size_t idx = blockIdx.x * blockDim.x + threadIdx.x;
  if (idx < n) c[idx] = a[idx] * scalar;
}

// Измерение времени копирования
void profileCopies(const char* desc, float *h_src, float *h_dst, float *d_buf, size_t bytes,
                   cudaMemcpyKind kind) {
  cudaEvent_t start, stop;
  CHECK_CUDA(cudaEventCreate(&start));
  CHECK_CUDA(cudaEventCreate(&stop));

  CHECK_CUDA(cudaEventRecord(start, 0));
  if (kind == cudaMemcpyHostToDevice) {
    CHECK_CUDA(cudaMemcpy(d_buf, h_src, bytes, kind));
  } else {
    CHECK_CUDA(cudaMemcpy(h_dst, d_buf, bytes, kind));
  }
  CHECK_CUDA(cudaEventRecord(stop, 0));
  CHECK_CUDA(cudaEventSynchronize(stop));
  float ms;
  CHECK_CUDA(cudaEventElapsedTime(&ms, start, stop));
  double gb_per_s = bytes / 1e9 / (ms / 1000.0);
  printf("%s %s: %.2f GB/s\n", desc, (kind == cudaMemcpyHostToDevice ? "H2D" : "D2H"), gb_per_s);

  CHECK_CUDA(cudaEventDestroy(start));
  CHECK_CUDA(cudaEventDestroy(stop));
}

// Сравнение pageable vs pinned копирования
void testCopies() {
  printf("\n=== Сравнение копирования (N=%zu элементов) ===\n", N_ELEMENTS);
  size_t bytes = N_ELEMENTS * sizeof(float);

  // Pageable
  float *h_pageable_src = (float*)malloc(bytes);
  float *h_pageable_dst = (float*)malloc(bytes);
  float *d_buf;
  CHECK_CUDA(cudaMalloc(&d_buf, bytes));

  // Инициализация
  srand(42);
  for (size_t i = 0; i < N_ELEMENTS; ++i) {
    h_pageable_src[i] = rand() / (float)RAND_MAX;
  }
  printf("Pageable ");
  profileCopies("Pageable", h_pageable_src, h_pageable_dst, d_buf, bytes, cudaMemcpyHostToDevice);
  profileCopies("Pageable", h_pageable_src, h_pageable_dst, d_buf, bytes, cudaMemcpyDeviceToHost);

  // Pinned
  float *h_pinned_src, *h_pinned_dst;
  CHECK_CUDA(cudaHostAlloc(&h_pinned_src, bytes, cudaHostAllocDefault));
  CHECK_CUDA(cudaHostAlloc(&h_pinned_dst, bytes, cudaHostAllocDefault));
  memcpy(h_pinned_src, h_pageable_src, bytes);
  printf("Pinned   ");
  profileCopies("Pinned", h_pinned_src, h_pinned_dst, d_buf, bytes, cudaMemcpyHostToDevice);
  profileCopies("Pinned", h_pinned_src, h_pinned_dst, d_buf, bytes, cudaMemcpyDeviceToHost);

  // Cleanup
  free(h_pageable_src); free(h_pageable_dst);
  cudaFreeHost(h_pinned_src); cudaFreeHost(h_pinned_dst);
  cudaFree(d_buf);
}

// Сложение векторов без потоков (baseline)
void vectorAddBaseline(float *h_a, float *h_b, float *h_c) {
  size_t bytes = N_ELEMENTS * sizeof(float);
  float *d_a, *d_b, *d_c;
  CHECK_CUDA(cudaMalloc(&d_a, bytes));
  CHECK_CUDA(cudaMalloc(&d_b, bytes));
  CHECK_CUDA(cudaMalloc(&d_c, bytes));

  cudaEvent_t start, stop;
  CHECK_CUDA(cudaEventCreate(&start));
  CHECK_CUDA(cudaEventCreate(&stop));
  CHECK_CUDA(cudaEventRecord(start));

  CHECK_CUDA(cudaMemcpy(d_a, h_a, bytes, cudaMemcpyHostToDevice));
  CHECK_CUDA(cudaMemcpy(d_b, h_b, bytes, cudaMemcpyHostToDevice));
  dim3 block(BLOCK_SIZE), grid((N_ELEMENTS + BLOCK_SIZE - 1) / BLOCK_SIZE);
  vectorAdd<<<grid, block>>>(d_a, d_b, d_c, N_ELEMENTS);
  CHECK_CUDA(cudaMemcpy(h_c, d_c, bytes, cudaMemcpyDeviceToHost));
  CHECK_CUDA(cudaDeviceSynchronize());

  CHECK_CUDA(cudaEventRecord(stop));
  CHECK_CUDA(cudaEventSynchronize(stop));
  float ms;
  CHECK_CUDA(cudaEventElapsedTime(&ms, start, stop));
  printf("Vector Add Baseline: %.2f ms\n", ms);

  CHECK_CUDA(cudaEventDestroy(start));
  CHECK_CUDA(cudaEventDestroy(stop));
  cudaFree(d_a); cudaFree(d_b); cudaFree(d_c);
}

void vectorAddAutoTune(float *h_a, float *h_b, float *h_c) {
  printf("\n=== АВТООПТИМИЗАЦИЯ Vector Add ===\n");

  double bestTime = 1e9;
  size_t bestChunk = 0;
  size_t chunkSizes[N_TEST_CHUNKS];

  size_t sizes[] = {1<<20, 4<<20, 16<<20, 32<<20, 64<<20, 128<<20, 256<<20,
                    N_ELEMENTS/2, N_ELEMENTS/4, N_ELEMENTS/8, N_ELEMENTS/16, N_ELEMENTS};
  for (int i = 0; i < N_TEST_CHUNKS; i++) {
    chunkSizes[i] = sizes[i] / sizeof(float);
  }

  size_t maxChunkBytes = (N_ELEMENTS * sizeof(float));

  // Подготовка streams и device памяти
  cudaStream_t streams[N_STREAMS];
  float *d_a[N_STREAMS], *d_b[N_STREAMS], *d_c[N_STREAMS];
  for (int i = 0; i < N_STREAMS; i++) {
    CHECK_CUDA(cudaStreamCreate(&streams[i]));
    CHECK_CUDA(cudaMalloc(&d_a[i], maxChunkBytes));
    CHECK_CUDA(cudaMalloc(&d_b[i], maxChunkBytes));
    CHECK_CUDA(cudaMalloc(&d_c[i], maxChunkBytes));
  }

  for (int test = 0; test < N_TEST_CHUNKS; test++) {
    size_t chunkSize = chunkSizes[test];
    size_t chunkBytes = chunkSize * sizeof(float);
    size_t nChunks = (N_ELEMENTS + chunkSize - 1) / chunkSize;

    cudaEvent_t start, stop;
    CHECK_CUDA(cudaEventCreate(&start));
    CHECK_CUDA(cudaEventCreate(&stop));
    CHECK_CUDA(cudaEventRecord(start));

    for (size_t ch = 0; ch < nChunks; ch++) {
      int streamId = ch % N_STREAMS;
      size_t offset = ch * chunkSize;
      size_t curSize = (offset + chunkSize > N_ELEMENTS) ? N_ELEMENTS - offset : chunkSize;
      size_t curBytes = curSize * sizeof(float);

      CHECK_CUDA(cudaMemcpyAsync(d_a[streamId], h_a + offset, curBytes, cudaMemcpyHostToDevice, streams[streamId]));
      CHECK_CUDA(cudaMemcpyAsync(d_b[streamId], h_b + offset, curBytes, cudaMemcpyHostToDevice, streams[streamId]));
      dim3 block(BLOCK_SIZE), grid((curSize + BLOCK_SIZE - 1) / BLOCK_SIZE);
      vectorAdd<<<grid, block, 0, streams[streamId]>>>(d_a[streamId], d_b[streamId], d_c[streamId], curSize);
      CHECK_CUDA(cudaMemcpyAsync(h_c + offset, d_c[streamId], curBytes, cudaMemcpyDeviceToHost, streams[streamId]));
    }
    CHECK_CUDA(cudaDeviceSynchronize());

    CHECK_CUDA(cudaEventRecord(stop));
    CHECK_CUDA(cudaEventSynchronize(stop));
    float ms;
    CHECK_CUDA(cudaEventElapsedTime(&ms, start, stop));

    printf("chunk=%.1fMB: %.2fms ", chunkBytes/1e6, ms);
    if (ms < bestTime) {
      bestTime = ms;
      bestChunk = chunkSize;
      printf("*** НОВЫЙ ЛУЧШИЙ ***");
    }
    printf("\n");

    CHECK_CUDA(cudaEventDestroy(start));
    CHECK_CUDA(cudaEventDestroy(stop));
  }

  printf("\nVector Add: ОПТИМАЛЬНЫЙ chunkSize = %.1fMB (%.2fms vs baseline %.2fms)\n",
         (bestChunk * sizeof(float))/1e6, bestTime, 1263.80); // из ваших результатов

  // Cleanup
  for (int i = 0; i < N_STREAMS; i++) {
    cudaFree(d_a[i]); cudaFree(d_b[i]); cudaFree(d_c[i]);
    CHECK_CUDA(cudaStreamDestroy(streams[i]));
  }
}

// НОВОЕ: Автоматическая оптимизация chunkSize для scalarMul
void scalarMulAutoTune(float *h_a, float *h_c) {
  printf("\n=== АВТООПТИМИЗАЦИЯ Scalar Mul ===\n");
  float scalar = 3.14159f;

  double bestTime = 1e9;
  size_t bestChunk = 0;
  size_t chunkSizes[N_TEST_CHUNKS];

  size_t sizes[] = {1<<20, 4<<20, 16<<20, 32<<20, 64<<20, 128<<20, 256<<20,
                    N_ELEMENTS/2, N_ELEMENTS/4, N_ELEMENTS/8, N_ELEMENTS/16, N_ELEMENTS};
  for (int i = 0; i < N_TEST_CHUNKS; i++) {
    chunkSizes[i] = sizes[i] / sizeof(float);
  }

  size_t maxChunkBytes = (N_ELEMENTS * sizeof(float));

  cudaStream_t streams[N_STREAMS];
  float *d_a[N_STREAMS], *d_c[N_STREAMS];
  for (int i = 0; i < N_STREAMS; i++) {
    CHECK_CUDA(cudaStreamCreate(&streams[i]));
    CHECK_CUDA(cudaMalloc(&d_a[i], maxChunkBytes));
    CHECK_CUDA(cudaMalloc(&d_c[i], maxChunkBytes));
  }

  for (int test = 0; test < N_TEST_CHUNKS; test++) {
    size_t chunkSize = chunkSizes[test];
    size_t chunkBytes = chunkSize * sizeof(float);
    size_t nChunks = (N_ELEMENTS + chunkSize - 1) / chunkSize;

    cudaEvent_t start, stop;
    CHECK_CUDA(cudaEventCreate(&start));
    CHECK_CUDA(cudaEventCreate(&stop));
    CHECK_CUDA(cudaEventRecord(start));

    for (size_t ch = 0; ch < nChunks; ch++) {
      int streamId = ch % N_STREAMS;
      size_t offset = ch * chunkSize;
      size_t curSize = (offset + chunkSize > N_ELEMENTS) ? N_ELEMENTS - offset : chunkSize;
      size_t curBytes = curSize * sizeof(float);

      CHECK_CUDA(cudaMemcpyAsync(d_a[streamId], h_a + offset, curBytes, cudaMemcpyHostToDevice, streams[streamId]));
      dim3 block(BLOCK_SIZE), grid((curSize + BLOCK_SIZE - 1) / BLOCK_SIZE);
      scalarMul<<<grid, block, 0, streams[streamId]>>>(d_a[streamId], scalar, d_c[streamId], curSize);
      CHECK_CUDA(cudaMemcpyAsync(h_c + offset, d_c[streamId], curBytes, cudaMemcpyDeviceToHost, streams[streamId]));
    }
    CHECK_CUDA(cudaDeviceSynchronize());

    CHECK_CUDA(cudaEventRecord(stop));
    CHECK_CUDA(cudaEventSynchronize(stop));
    float ms;
    CHECK_CUDA(cudaEventElapsedTime(&ms, start, stop));

    printf("chunk=%.1fMB: %.2fms ", chunkBytes/1e6, ms);
    if (ms < bestTime) {
      bestTime = ms;
      bestChunk = chunkSize;
      printf("*** НОВЫЙ ЛУЧШИЙ ***");
    }
    printf("\n");

    CHECK_CUDA(cudaEventDestroy(start));
    CHECK_CUDA(cudaEventDestroy(stop));
  }

  printf("\nScalar Mul: ОПТИМАЛЬНЫЙ chunkSize = %.1fMB (%.2fms)\n",
         (bestChunk * sizeof(float))/1e6, bestTime);

  for (int i = 0; i < N_STREAMS; i++) {
    cudaFree(d_a[i]); cudaFree(d_c[i]);
    CHECK_CUDA(cudaStreamDestroy(streams[i]));
  }
}

int main() {
  printf("CUDA Streams Optimization Test (N=%zu элементов)\n", N_ELEMENTS);

  testCopies();

  printf("\n=== Векторное сложение ===\n");

  // Используем PINNED память для максимальной производительности
  size_t bytes = N_ELEMENTS * sizeof(float);
  float *h_a_base, *h_b_base, *h_c_base;
  CHECK_CUDA(cudaHostAlloc(&h_a_base, bytes, cudaHostAllocDefault));
  CHECK_CUDA(cudaHostAlloc(&h_b_base, bytes, cudaHostAllocDefault));
  CHECK_CUDA(cudaHostAlloc(&h_c_base, bytes, cudaHostAllocDefault));

  srand(42);
  for (size_t i = 0; i < N_ELEMENTS; ++i) {
    h_a_base[i] = rand() / (float)RAND_MAX;
    h_b_base[i] = rand() / (float)RAND_MAX;
  }

  vectorAddBaseline(h_a_base, h_b_base, h_c_base);

  // АВТООПТИМИЗАЦИЯ!
  vectorAddAutoTune(h_a_base, h_b_base, h_c_base);
  scalarMulAutoTune(h_a_base, h_c_base);

  cudaFreeHost(h_a_base); cudaFreeHost(h_b_base); cudaFreeHost(h_c_base);

  // Старые тесты закомментированы для чистоты вывода
  /*
  size_t testChunks[] = {N_ELEMENTS / 1, N_ELEMENTS / 4, N_ELEMENTS / 16, N_ELEMENTS / 64};
  char labels[4][20] = {"full", "4 chunks", "16 chunks", "64 chunks"};
  for (int i = 0; i < 4; ++i) {
    vectorAddStreams(testChunks[i], labels[i]);
    scalarMulStreams(testChunks[i], labels[i]);
  }
  */

  return 0;
}
