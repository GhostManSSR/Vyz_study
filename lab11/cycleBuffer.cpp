#include <iostream>
#include <thread>
#include <mutex>
#include <semaphore>
#include <vector>
#include <chrono>
#include <random>

class CircularBuffer {
private:
    std::vector<int> buffer;
    size_t capacity;
    
    // Синхронизация
    std::mutex mutex;
    std::counting_semaphore<> empty;
    std::counting_semaphore<> full;
    
    // Индексы для циклического буфера
    size_t head = 0; // Указатель на следующую свободную позицию
    size_t tail = 0; // Указатель на следующий элемент для чтения
    bool is_full = false;

public:
    CircularBuffer(size_t size) 
        : buffer(size), capacity(size), empty(size), full(0) {}
    
    void produce(int item) {
        empty.acquire(); // Ждем свободного места
        
        {
            std::lock_guard<std::mutex> lock(mutex);
            buffer[head] = item;
            head = (head + 1) % capacity;
            
            // Проверяем, не стал ли буфер полным
            if (head == tail) {
                is_full = true;
            }
            
            size_t current_size = size();
            std::cout << "Произведен: " << item << " (буфер: " << current_size << "/" << capacity << ")\n";
        }
        
        full.release(); // Увеличиваем счетчик заполненных ячеек
    }
    
    int consume() {
        full.acquire(); // Ждем доступных данных
        
        int item;
        {
            std::lock_guard<std::mutex> lock(mutex);
            item = buffer[tail];
            tail = (tail + 1) % capacity;
            is_full = false; // После чтения буфер точно не полный
            
            size_t current_size = size();
            std::cout << "Потреблен: " << item << " (буфер: " << current_size << "/" << capacity << ")\n";
        }
        
        empty.release(); // Увеличиваем счетчик свободных ячеек
        return item;
    }
    
    size_t size() const {
        if (is_full) {
            return capacity;
        }
        if (head >= tail) {
            return head - tail;
        }
        return capacity - tail + head;
    }
    
    bool empty() const {
        return !is_full && (head == tail);
    }
};

void circular_producer(CircularBuffer& buffer, int id, int items_count) {
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(100, 400);
    
    for (int i = 0; i < items_count; ++i) {
        int item = id * 1000 + i;
        buffer.produce(item);
        std::this_thread::sleep_for(std::chrono::milliseconds(dis(gen)));
    }
    std::cout << "Производитель " << id << " завершил работу\n";
}

void circular_consumer(CircularBuffer& buffer, int id, int items_count) {
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<> dis(150, 500);
    
    for (int i = 0; i < items_count; ++i) {
        int item = buffer.consume();
        std::this_thread::sleep_for(std::chrono::milliseconds(dis(gen)));
    }
    std::cout << "Потребитель " << id << " завершил работу\n";
}