#include <iostream>
#include <vector>
#include <thread>
#include <atomic>
#include <chrono>

const int SIZE = 100000000;
const int NUM_THREADS = 2; // для алгоритма Петерсона 2 потока

std::vector<int> data(SIZE, 1);

std::atomic<bool> flag[2] = {false, false};
std::atomic<int> turn = 0;

void peterson_lock(int self) {
    int other = 1 - self;
    flag[self].store(true, std::memory_order_relaxed);
    turn.store(other, std::memory_order_relaxed);
    while (flag[other].load(std::memory_order_acquire) && turn.load(std::memory_order_acquire) == other) {}
}

void peterson_unlock(int self) {
    flag[self].store(false, std::memory_order_release);
}

long long total_sum = 0;

void threaded_sum(int id, int start, int end) {
    long long partial = 0;
    for (int i = start; i < end; ++i) {
        partial += data[i];
    }
    peterson_lock(id);
    total_sum += partial;
    peterson_unlock(id);
}

int main() {
    int chunk = SIZE / NUM_THREADS;

    auto start = std::chrono::steady_clock::now();

    std::thread threads[NUM_THREADS];
    for (int i = 0; i < NUM_THREADS; ++i) {
        int s = i * chunk;
        int e = (i == NUM_THREADS - 1) ? SIZE : (i + 1) * chunk;
        threads[i] = std::thread(threaded_sum, i, s, e);
    }
    for (int i = 0; i < NUM_THREADS; ++i) {
        threads[i].join();
    }
    auto end = std::chrono::steady_clock::now();

    std::cout << "Sum: " << total_sum << ", time: " << std::chrono::duration<double>(end - start).count() << " seconds\n";

    return 0;
}
