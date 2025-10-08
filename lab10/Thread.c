#include <stdio.h>
#include <pthread.h>
#include <unistd.h>
#include <atomic>
#include <chrono>

char sh[6];

// Флаги и переменная turn для Петерсона
std::atomic<bool> flag[2] = {false, false};
std::atomic<int> turn = 0;
pthread_spinlock_t spinlock;

bool use_peterson = true;

void peterson_lock(int self) {
    int other = 1 - self;
    flag[self].store(true, std::memory_order_relaxed);
    turn.store(other, std::memory_order_relaxed);
    while (flag[other].load(std::memory_order_acquire) && turn.load(std::memory_order_acquire) == other) {}
}

void peterson_unlock(int self) {
    flag[self].store(false, std::memory_order_release);
}

struct ThreadArg {
    int id;
};

void* Thread(void* pParams) {
    ThreadArg* arg = (ThreadArg*)pParams;
    int id = arg->id;
    int counter = 0;
    while (1) {
        if (use_peterson) {
            peterson_lock(id);
        } else {
            pthread_spin_lock(&spinlock);
        }

        if (counter % 2) {
            sh[0] = 'H'; sh[1] = 'e'; sh[2] = 'l'; sh[3] = 'l'; sh[4] = 'o'; sh[5] = '\0';
        } else {
            sh[0] = 'B'; sh[1] = 'y'; sh[2] = 'e'; sh[3] = '_'; sh[4] = 'u'; sh[5] = '\0';
        }
        counter++;

        if (use_peterson) {
            peterson_unlock(id);
        } else {
            pthread_spin_unlock(&spinlock);
        }
        usleep(100000);
    }
    return NULL;
}

int main(void) {
    pthread_t thread_id;
    ThreadArg arg = {0};

    pthread_spin_init(&spinlock, PTHREAD_PROCESS_PRIVATE);

    pthread_create(&thread_id, NULL, Thread, &arg);

    int main_id = 1;

    int iterations = 50;

    for (int mode = 0; mode < 2; ++mode) {
        use_peterson = (mode == 0);

        // Прогреваем поток
        usleep(500000);

        auto start_time = std::chrono::steady_clock::now();

        for (int i = 0; i < iterations; ++i) {
            if (use_peterson) {
                peterson_lock(main_id);
            } else {
                pthread_spin_lock(&spinlock);
            }
            printf("%s\n", sh);
            if (use_peterson) {
                peterson_unlock(main_id);
            } else {
                pthread_spin_unlock(&spinlock);
            }
        }

        auto end_time = std::chrono::steady_clock::now();
        std::chrono::duration<double> duration = end_time - start_time;

        printf("%s lock time for %d iterations: %f seconds\n",
               use_peterson ? "Peterson" : "pthread_spin", iterations, duration.count());
    }

    pthread_spin_destroy(&spinlock);

    return 0;
}
