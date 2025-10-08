# Компилятор и флаги
CXX = g++
CXXFLAGS = -std=c++20 -pthread -O2 -Wall -Wextra
TARGET = producer_consumer
SRC = producer_consumer.cpp

# Правило по умолчанию
all: $(TARGET)

# Сборка основной программы
$(TARGET): $(SRC)
	$(CXX) $(CXXFLAGS) -o $(TARGET) $(SRC)

# Сборка с отладочной информацией
debug: CXXFLAGS += -g -DDEBUG
debug: $(TARGET)

# Сборка с санитайзерами
sanitize: CXXFLAGS += -g -fsanitize=address -fsanitize=thread
sanitize: $(TARGET)

# Запуск программы
run: $(TARGET)
	./$(TARGET)

# Очистка
clean:
	rm -f $(TARGET) *.o

# Пересборка
rebuild: clean all

# Помощь
help:
	@echo "Доступные цели:"
	@echo "  all      - сборка программы (по умолчанию)"
	@echo "  debug    - сборка с отладочной информацией"
	@echo "  sanitize - сборка с санитайзерами для поиска гонок"
	@echo "  run      - сборка и запуск программы"
	@echo "  clean    - удаление скомпилированных файлов"
	@echo "  rebuild  - полная пересборка"
	@echo "  help     - показать эту справку"

.PHONY: all debug sanitize run clean rebuild help