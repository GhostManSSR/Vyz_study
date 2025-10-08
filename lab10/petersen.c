#include <atomic>
#include <thread>
#include <iostream>

// Флаги и выбор для алгоритма Петерсона
std::atomic<bool> flag[2];
std::atomic<int> turn;

void peterson_lock(int self) {
    int other = 1 - self;
    flag[self].store(true, std::memory_order_relaxed);
    turn.store(other, std::memory_order_relaxed);
    // spin wait
    while (flag[other].load(std::memory_order_acquire) && turn.load(std::memory_order_acquire) == other);
}

void peterson_unlock(int self) {
    flag[self].store(false, std::memory_order_release);
}
