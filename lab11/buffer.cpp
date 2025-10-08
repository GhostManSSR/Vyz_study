#include <iostream>
#include <thread>
#include <mutex>
#include <semaphore>
#include <vector>
#include <chrono>
#include <random>

class BoundedBuffer {
private:
    std::vector<int> buffer;
    size_t capacity;
    
    // Синхронизация
    std::mutex mutex;
    std::counting_semaphore<> empty;
    std::counting_semaphore<> full;
    
    // Индексы для записи и чтения
    size_t in = 0;
    size_t out = 0;

public:
    BoundedBuffer(size_t size) 
        : buffer(size), capacity(size), empty(size), full(0) {}
    
    void produce(int item) {
        empty.acquire(); // Ждем свободного места
        
        {
            std::lock_guard<std::mutex> lock(mutex);
            buffer[in] = item;
            in = (in + 1) % capacity;
            std::cout << "Произведен: " << item << " (буфер: " << (in - out + capacity) % capacity << "/" << capacity << ")\n";
        }
        
        full.release(); // Увеличиваем счетчик заполненных ячеек
    }
    
    int consume() {
        full.acquire(); // Ждем доступных данных
        
        int item;
        {
            std::lock_guard<std::mutex> lock(mutex);
            item = buffer[out];
            out = (out + 1) % capacity;
            std::cout << "Потреблен: " << item << " (буфер: " << (in - out + capacity) % capacity << "/" << capacity << ")\n";
        }
        
        empty.release(); // Увеличиваем счетчик свободных ячеек
        return item;
    }
};

void producer(BoundedBuffer& buffer, int id, int items_count) {
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(100, 500);
    
    for (int i = 0; i < items_count; ++i) {
        int item = id * 1000 + i;
        buffer.produce(item);
        std::this_thread::sleep_for(std::chrono::milliseconds(dis(gen)));
    }
    std::cout << "Производитель " << id << " завершил работу\n";
}

void consumer(BoundedBuffer& buffer, int id, int items_count) {
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(150, 600);
    
    for (int i = 0; i < items_count; ++i) {
        int item = buffer.consume();
        std::this_thread::sleep_for(std::chrono::milliseconds(dis(gen)));
    }
    std::cout << "Потребитель " << id << " завершил работу\n";
}