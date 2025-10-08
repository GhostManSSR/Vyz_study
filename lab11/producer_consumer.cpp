#include <iostream>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <vector>
#include <chrono>
#include <random>

class BoundedBuffer {
private:
    std::vector<int> buffer;
    size_t capacity;
    size_t count = 0;
    size_t in = 0;
    size_t out = 0;
    
    std::mutex mutex;
    std::condition_variable not_full;
    std::condition_variable not_empty;

public:
    BoundedBuffer(size_t size) : buffer(size), capacity(size) {}
    
    void produce(int item) {
        std::unique_lock<std::mutex> lock(mutex);
        not_full.wait(lock, [this]() { return count < capacity; });
        
        buffer[in] = item;
        in = (in + 1) % capacity;
        ++count;
        
        std::cout << "Произведен: " << item << " (буфер: " << count << "/" << capacity << ")\n";
        
        lock.unlock();
        not_empty.notify_one();
    }
    
    int consume() {
        std::unique_lock<std::mutex> lock(mutex);
        not_empty.wait(lock, [this]() { return count > 0; });
        
        int item = buffer[out];
        out = (out + 1) % capacity;
        --count;
        
        std::cout << "Потреблен: " << item << " (буфер: " << count << "/" << capacity << ")\n";
        
        lock.unlock();
        not_full.notify_one();
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
    std::cout << ">>> Производитель " << id << " завершил работу\n";
}

void consumer(BoundedBuffer& buffer, int id, int items_count) {
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(150, 600);
    
    for (int i = 0; i < items_count; ++i) {
        int item = buffer.consume();
        std::this_thread::sleep_for(std::chrono::milliseconds(dis(gen)));
    }
    std::cout << ">>> Потребитель " << id << " завершил работу\n";
}

int main() {
    std::cout << "=== Тестирование конечного буфера ===\n";
    
    BoundedBuffer buffer(5);
    
    std::thread prod1(producer, std::ref(buffer), 1, 5);
    std::thread prod2(producer, std::ref(buffer), 2, 5);
    std::thread cons1(consumer, std::ref(buffer), 1, 5);
    std::thread cons2(consumer, std::ref(buffer), 2, 5);
    
    prod1.join();
    prod2.join();
    cons1.join();
    cons2.join();
    
    std::cout << "=== Тест завершен ===\n";
    return 0;
}