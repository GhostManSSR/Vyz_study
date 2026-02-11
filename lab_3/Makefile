# Компилятор
CXX = g++
CXXFLAGS = -std=c++17 -Wall -Wextra -O2
LDFLAGS = -pthread

# Имена исполняемых файлов
TARGET_SERVER = server
TARGET_CLIENT = client
SOURCES_SERVER = server.cpp
SOURCES_CLIENT = client.cpp

# Цели по умолчанию
.PHONY: all server client clean run

all: $(TARGET_SERVER) $(TARGET_CLIENT)

# Компиляция сервера
$(TARGET_SERVER): $(SOURCES_SERVER)
	@echo "Компиляция сервера..."
	$(CXX) $(CXXFLAGS) -o $(TARGET_SERVER) $(SOURCES_SERVER) $(LDFLAGS)

# Компиляция клиента
$(TARGET_CLIENT): $(SOURCES_CLIENT)
	@echo "Компиляция клиента..."
	$(CXX) $(CXXFLAGS) -o $(TARGET_CLIENT) $(SOURCES_CLIENT) $(LDFLAGS)

# Демонстрация (запуск сервера в фоне + клиента)
demo:
	@echo "Запуск сервера..."
	./$(TARGET_SERVER) &
	SERVER_PID=$$!
	sleep 2
	@echo "Запуск клиента..."
	./$(TARGET_CLIENT) 127.0.0.1 12345 2
	kill $$SERVER_PID 2>/dev/null || true

# Очистка
clean:
	@echo "Очистка..."
	rm -f $(TARGET_SERVER) $(TARGET_CLIENT) server_log.txt

# Проверка кода
check:
	@echo "Проверка стиля кода..."
	cppcheck --enable=all --inconclusive $(SOURCES_SERVER) $(SOURCES_CLIENT)

# Установка зависимостей (если нужно)
install-deps:
	# На Ubuntu/Debian
	sudo apt-get update
	sudo apt-get install -y build-essential g++ cppcheck

# Помощь
help:
	@echo "Доступные цели:"
	@echo "  make          - компиляция server и client"
	@echo "  make server   - только сервер"
	@echo "  make client   - только клиент"
	@echo "  make demo     - демонстрация (сервер + клиент)"
	@echo "  make clean    - очистка"
	@echo "  make check    - статический анализ"
