//
// Created by Arti on 05.02.2026.
//

#include <iostream>
#include <string>
#include <cstring>
#include <cstdlib>

#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netinet/in.h>

int main(int argc, char* argv[]) {
    if (argc != 5) {
        std::cerr << "Usage: " << argv[0]
                  << " <server_ip> <server_port> <i> <count>\n";
        std::cerr << "Example: " << argv[0] << " 127.0.0.1 50000 3 5\n";
        return 1;
    }

    const char* server_ip = argv[1];
    uint16_t server_port = static_cast<uint16_t>(std::stoi(argv[2]));
    int i = std::stoi(argv[3]);       // задержка в секундах и отправляемое число
    int count = std::stoi(argv[4]);   // сколько раз отправлять

    // 1. Создаем UDP-сокет
    int sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sockfd < 0) {
        perror("socket");
        return 1;
    }

    // 2. Заполняем структуру адреса сервера
    sockaddr_in servaddr{};
    servaddr.sin_family = AF_INET;
    servaddr.sin_port = htons(server_port);

    if (inet_pton(AF_INET, server_ip, &servaddr.sin_addr) <= 0) {
        perror("inet_pton");
        close(sockfd);
        return 1;
    }

    // 3. Цикл отправки числа i с задержкой i сек
    for (int k = 0; k < count; ++k) {
        std::string msg = std::to_string(i);

        ssize_t sent = sendto(sockfd, msg.c_str(), msg.size(), 0,
                              reinterpret_cast<sockaddr*>(&servaddr),
                              sizeof(servaddr));
        if (sent < 0) {
            perror("sendto");
            break;
        }

        std::cout << "Sent i=" << i << " (" << (k + 1) << "/" << count << ")\n";

        // Получаем ответ от сервера
        char buffer[1024];
        socklen_t len = sizeof(servaddr);
        ssize_t n = recvfrom(sockfd, buffer, sizeof(buffer) - 1, 0,
                             reinterpret_cast<sockaddr*>(&servaddr), &len);
        if (n < 0) {
            perror("recvfrom");
            break;
        }
        buffer[n] = '\0';
        std::cout << "Response: " << buffer << std::endl;

        // Задержка i секунд
        sleep(i);
    }

    close(sockfd);
    return 0;
}
