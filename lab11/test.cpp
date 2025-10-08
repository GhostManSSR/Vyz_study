int main() {
    std::cout << "=== Тестирование конечного буфера ===\n";
    {
        BoundedBuffer buffer(5);
        
        std::thread prod1(producer, std::ref(buffer), 1, 10);
        std::thread prod2(producer, std::ref(buffer), 2, 10);
        std::thread cons1(consumer, std::ref(buffer), 1, 10);
        std::thread cons2(consumer, std::ref(buffer), 2, 10);
        
        prod1.join();
        prod2.join();
        cons1.join();
        cons2.join();
    }
    
    std::cout << "\n=== Тестирование циклического буфера ===\n";
    {
        CircularBuffer buffer(5);
        
        std::thread prod1(circular_producer, std::ref(buffer), 1, 10);
        std::thread prod2(circular_producer, std::ref(buffer), 2, 10);
        std::thread cons1(circular_consumer, std::ref(buffer), 1, 10);
        std::thread cons2(circular_consumer, std::ref(buffer), 2, 10);
        
        prod1.join();
        prod2.join();
        cons1.join();
        cons2.join();
    }
    
    return 0;
}