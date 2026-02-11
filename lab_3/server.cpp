//
// Created by Arti on 10.02.2026.
//

#include <iostream>
#include <string>
#include <thread>
#include <mutex>
#include <vector>
#include <cstring>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <fstream>

std::mutex file_mutex;
int sockfd;
std::ofstream log_file;

void handle_client(int client_fd) {
    char buffer[1024];
    ssize_t bytes_received;

    while ((bytes_received = recv(client_fd, buffer, sizeof(buffer)-1, 0)) > 0) {
        buffer[bytes_received] = '\0';
        std::cout << "Получено от клиента: " << buffer;

        // Запись в общий файл с блокировкой
        {
            std::lock_guard<std::mutex> lock(file_mutex);
            log_file << "[" << std::this_thread::get_id() << "] " << buffer;
            log_file.flush();
        }
    }

    close(client_fd);
}

int main() {
    struct sockaddr_in server_addr, client_addr;
    socklen_t client_len = sizeof(client_addr);

    // Открытие общего файла
    log_file.open("server_log.txt", std::ios::app);
    if (!log_file.is_open()) {
        std::cerr << "Не удалось открыть файл лога\n";
        return 1;
    }

    // Создание сокета
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        perror("Ошибка создания сокета");
        return 1;
    }

    // Настройка адреса (порт 0 = любой свободный)
    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = 0;

    // Привязка к порту
    if (bind(sockfd, (struct sockaddr*)&server_addr, sizeof(server_addr)) < 0) {
        perror("Ошибка bind");
        return 1;
    }

    // Получение номера назначенного порта
    socklen_t addr_len = sizeof(server_addr);
    if (getsockname(sockfd, (struct sockaddr*)&server_addr, &addr_len) < 0) {
        perror("Ошибка getsockname");
        return 1;
    }

    int port = ntohs(server_addr.sin_port);
    std::cout << "Сервер запущен на порту: " << port << std::endl;

    // Прослушивание
    listen(sockfd, 5);
    std::cout << "Ожидание подключений...\n";

    while (true) {
        int client_fd = accept(sockfd, (struct sockaddr*)&client_addr, &client_len);
        if (client_fd < 0) {
            perror("Ошибка accept");
            continue;
        }

        std::cout << "Новое подключение: " << inet_ntoa(client_addr.sin_addr)
                  << ":" << ntohs(client_addr.sin_port) << std::endl;

        // Создание потока для клиента
        std::thread client_thread(handle_client, client_fd);
        client_thread.detach();  // Поток работает независимо
    }

    log_file.close();
    close(sockfd);
    return 0;
}
