#include <stdio.h>
#include <cuda_runtime.h>

// Ядро с искусственной ошибкой памяти для определенных нитей
__global__ void vectorAdd(const float *A, const float *B, float *C, int N) {
    int i = blockIdx.x * blockDim.x + threadIdx.x;

    // ГЕНЕРАЦИЯ ОШИБКИ: нити с threadIdx.x < 4 обращаются за пределы массива
    if (threadIdx.x < 4) {
        // Ошибка: обращение к A[N] (за пределами выделенной памяти)
        float invalid_value = A[N];  // ← ИСКУСТВЕННАЯ ОШИБКА ЗДЕСЬ!
        C[i] = invalid_value + B[i];
        printf("Warp %d, thread %d: Ошибочное обращение A[%d]\n",
               blockIdx.x, threadIdx.x, N);
    } else {
        // Нормальное выполнение для остальных нитей
        if (i < N) {
            C[i] = A[i] + B[i];
        }
    }
}

// Функция проверки ошибок CUDA
void checkCudaError(cudaError_t err, const char* msg, int line) {
    if (err != cudaSuccess) {
        fprintf(stderr, "CUDA ОШИБКА '%s': %s в строке %d\n",
                msg, cudaGetErrorString(err), line);
        cudaDeviceReset();
        exit(-1);
    }
}

int main(int argc, char* argv[]) {
    int N = 1024;  // Размер векторов
    if (argc > 1) N = atoi(argv[1]);

    printf("Сложение векторов размером N=%d\n", N);
    printf("ОШИБОЧНЫЕ НИТИ: threadIdx.x < 4\n\n");

    // 1. Выделение памяти на хосте
    float *h_A = (float*)malloc(N * sizeof(float));
    float *h_B = (float*)malloc(N * sizeof(float));
    float *h_C = (float*)malloc(N * sizeof(float));

    // Инициализация данных
    for (int i = 0; i < N; i++) {
        h_A[i] = i * 1.0f;
        h_B[i] = i * 2.0f;
    }

    // 2. Выделение памяти на устройстве
    float *d_A, *d_B, *d_C;
    checkCudaError(cudaMalloc(&d_A, N * sizeof(float)), "cudaMalloc A", __LINE__);
    checkCudaError(cudaMalloc(&d_B, N * sizeof(float)), "cudaMalloc B", __LINE__);
    checkCudaError(cudaMalloc(&d_C, N * sizeof(float)), "cudaMalloc C", __LINE__);

    // 3. Копирование данных на устройство
    checkCudaError(cudaMemcpy(d_A, h_A, N * sizeof(float), cudaMemcpyHostToDevice),
                   "cudaMemcpy HtoD A", __LINE__);
    checkCudaError(cudaMemcpy(d_B, h_B, N * sizeof(float), cudaMemcpyHostToDevice),
                   "cudaMemcpy HtoD B", __LINE__);

    // 4. Конфигурация запуска ядра
    int threadsPerBlock = 256;
    int blocksPerGrid = (N + threadsPerBlock - 1) / threadsPerBlock;

    printf("Блоков: %d, нитей/блок: %d\n", blocksPerGrid, threadsPerBlock);

    // 5. ОБРАБОТКА ОШИБКИ: запуск ядра с проверкой
    printf("\n=== ЗАПУСК ЯДРА С ОШИБКОЙ ===\n");
    vectorAdd<<<blocksPerGrid, threadsPerBlock>>>(d_A, d_B, d_C, N);

    // Проверка асинхронных ошибок ядра
    cudaError_t async_err = cudaGetLastError();
    if (async_err != cudaSuccess) {
        printf("АСИНХРОННАЯ ОШИБКА ЯДРА: %s\n", cudaGetErrorString(async_err));
        printf("Нити threadIdx.x < 4 вызвали illegal memory access\n");
    }

    // Синхронизация с обработкой ошибок
    printf("\n=== СИНХРОНИЗАЦИЯ ===\n");
    cudaError_t sync_err = cudaDeviceSynchronize();
    if (sync_err != cudaSuccess) {
        printf("ОШИБКА СИНХРОНИЗАЦИИ: %s\n", cudaGetErrorString(sync_err));
        printf("Проблемные нити остановлены GPU\n");
    } else {
        printf("Синхронизация успешна (но данные некорректны)\n");
    }

    // 6. Копирование результата обратно (может не сработать из-за ошибки)
    printf("\n=== КОПИРОВАНИЕ РЕЗУЛЬТАТА ===\n");
    cudaError_t memcpy_err = cudaMemcpy(h_C, d_C, N * sizeof(float), cudaMemcpyDeviceToHost);
    if (memcpy_err != cudaSuccess) {
        printf("ОШИБКА КОПИРОВАНИЯ: %s\n", cudaGetErrorString(memcpy_err));
    }

    // Вывод первых 16 элементов результата
    printf("\nПервые 16 элементов C[]:\n");
    for (int i = 0; i < 16 && i < N; i++) {
        if (i < 4) {
            printf("C[%2d] = %.1f (ошибочная нить)\n", i, h_C[i]);
        } else {
            printf("C[%2d] = %.1f\n", i, h_C[i]);
        }
    }

    // 7. Освобождение памяти
    cudaFree(d_A); cudaFree(d_B); cudaFree(d_C);
    free(h_A); free(h_B); free(h_C);

    printf("\nПрограмма завершена. Проверьте отладку с cuda-gdb\n");
    return 0;
}
