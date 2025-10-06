// Общие includes
#include <iostream>
#include <vector>
#include <thread>
#include <pthread.h>
#include <chrono>
#include <numeric>  // для std::accumulate

const int SIZE = 100000000;  // размер массива
const int NUM_THREADS = 4;

std::vector<int> data(SIZE, 1);  // массив из единиц для скорости

// Последовательное суммирование
long long sequential_sum(const std::vector<int>& data) {
    long long sum = 0;
    for (auto v : data) sum += v;
    return sum;
}

// Pthreads: структура для передачи параметров потоку
struct ThreadData {
    int start;
    int end;
    const std::vector<int>* data;
    long long partial_sum;
};

void* pthread_sum(void* arg) {
    ThreadData* td = (ThreadData*) arg;
    td->partial_sum = 0;
    for (int i = td->start; i < td->end; ++i) {
        td->partial_sum += (*td->data)[i];
    }
    return NULL;
}

// std::thread сумма: функция
void cpp_thread_sum(const std::vector<int>& data, int start, int end, long long& result) {
    long long sum = 0;
    for (int i = start; i < end; ++i) sum += data[i];
    result = sum;
}

int main() {
    // Последовательное
    auto start = std::chrono::steady_clock::now();
    long long seq_sum = sequential_sum(data);
    auto end = std::chrono::steady_clock::now();
    std::cout << "Sequential sum: " << seq_sum << ", time: "
              << std::chrono::duration<double>(end - start).count() << " seconds\n";

    // Pthreads
    ThreadData thread_data[NUM_THREADS];
    pthread_t threads[NUM_THREADS];
    int chunk = SIZE / NUM_THREADS;

    start = std::chrono::steady_clock::now();
    for (int i = 0; i < NUM_THREADS; ++i) {
        thread_data[i].start = i * chunk;
        thread_data[i].end = (i == NUM_THREADS - 1) ? SIZE : (i + 1) * chunk;
        thread_data[i].data = &data;
        pthread_create(&threads[i], NULL, pthread_sum, &thread_data[i]);
    }
    long long total_pthread_sum = 0;
    for (int i = 0; i < NUM_THREADS; ++i) {
        pthread_join(threads[i], NULL);
        total_pthread_sum += thread_data[i].partial_sum;
    }
    end = std::chrono::steady_clock::now();
    std::cout << "Pthreads sum: " << total_pthread_sum << ", time: "
              << std::chrono::duration<double>(end - start).count() << " seconds\n";

    // std::thread
    std::thread cpp_threads[NUM_THREADS];
    long long cpp_thread_results[NUM_THREADS] = {0};

    start = std::chrono::steady_clock::now();
    for (int i = 0; i < NUM_THREADS; ++i) {
        int s = i * chunk;
        int e = (i == NUM_THREADS - 1) ? SIZE : (i + 1) * chunk;
        cpp_threads[i] = std::thread(cpp_thread_sum, std::ref(data), s, e, std::ref(cpp_thread_results[i]));
    }
    for (int i = 0; i < NUM_THREADS; ++i) {
        cpp_threads[i].join();
    }
    long long total_cpp_thread_sum = 0;
    for (int i = 0; i < NUM_THREADS; ++i) {
        total_cpp_thread_sum += cpp_thread_results[i];
    }
    end = std::chrono::steady_clock::now();
    std::cout << "C++11 std::thread sum: " << total_cpp_thread_sum << ", time: "
              << std::chrono::duration<double>(end - start).count() << " seconds\n";

    return 0;
}
