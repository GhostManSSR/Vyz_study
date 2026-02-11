#include <iostream>
#include <string>
#include <cstring>  // Добавлен для memset()
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

int main(int argc, char* argv[]) {
    if (argc != 4) {
        std::cout << "Использование: " << argv[0] << " <IP_сервера> <порт> <число_i>\n";
        return 1;
    }

    std::string server_ip = argv[1];
    int port = std::stoi(argv[2]);
    int i = std::stoi(argv[3]);
    int iterations = 10;

    int sockfd;
    struct sockaddr_in server_addr;

    // Создание сокета
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        perror("Ошибка создания сокета");
        return 1;
    }

    // Настройка адреса сервера
    memset(&server_addr, 0, sizeof(server_addr));  // Теперь memset доступен
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(port);
    inet_pton(AF_INET, server_ip.c_str(), &server_addr.sin_addr);

    // Подключение к серверу
    if (connect(sockfd, (struct sockaddr*)&server_addr, sizeof(server_addr)) < 0) {
        perror("Ошибка подключения");
        return 1;
    }

    std::cout << "Подключено к " << server_ip << ":" << port
              << ". Отправка числа " << i << " (" << iterations << " раз)\n";

    std::string buffer;
    for (int j = 0; j < iterations; ++j) {
        buffer = "Число " + std::to_string(i) + ", итерация " + std::to_string(j + 1) + "\n";

        if (send(sockfd, buffer.c_str(), buffer.size(), 0) < 0) {
            perror("Ошибка отправки");
            break;
        }

        std::cout << "Отправлено: " << buffer;
        sleep(i);  // Задержка i секунд
    }

    close(sockfd);
    return 0;
}
