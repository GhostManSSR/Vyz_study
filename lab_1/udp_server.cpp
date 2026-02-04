//
// Created by Arti on 05.02.2026.
//

#include <iostream>
#include <cstring>
#include <string>
#include <cstdlib>

#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netinet/in.h>

int main() {
    // 1. Создаем UDP-сокет
    int sockfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sockfd < 0) {
        perror("socket");
        return 1;
    }

    // 2. Заполняем структуру адреса сервера
    sockaddr_in servaddr{};
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(50000); // 0 => ядро само выберет свободный порт

    // 3. Привязываем сокет к адресу (bind)
    if (bind(sockfd, reinterpret_cast<sockaddr*>(&servaddr), sizeof(servaddr)) < 0) {
        perror("bind");
        close(sockfd);
        return 1;
    }

    // 4. Узнаём, какой порт реально выделен (getsockname)
    socklen_t addrlen = sizeof(servaddr);
    if (getsockname(sockfd, reinterpret_cast<sockaddr*>(&servaddr), &addrlen) < 0) {
        perror("getsockname");
        close(sockfd);
        return 1;
    }

    char ipbuf[INET_ADDRSTRLEN];
    inet_ntop(AF_INET, &servaddr.sin_addr, ipbuf, sizeof(ipbuf));
    std::cout << "Server listening on IP " << ipbuf
              << " port " << ntohs(servaddr.sin_port) << std::endl;

    // 5. Основной цикл обработки датаграмм
    while (true) {
        char buffer[1024];
        sockaddr_in cliaddr{};
        socklen_t cli_len = sizeof(cliaddr);

        // recvfrom: получаем данные и одновременно узнаем IP/порт клиента
        ssize_t n = recvfrom(sockfd, buffer, sizeof(buffer) - 1, 0,
                             reinterpret_cast<sockaddr*>(&cliaddr), &cli_len);
        if (n < 0) {
            perror("recvfrom");
            continue;
        }

        buffer[n] = '\0'; // делаем C-строку
        char client_ip[INET_ADDRSTRLEN];
        inet_ntop(AF_INET, &cliaddr.sin_addr, client_ip, sizeof(client_ip));
        uint16_t client_port = ntohs(cliaddr.sin_port);

        std::cout << "Received from " << client_ip << ":" << client_port
                  << " -> \"" << buffer << "\"" << std::endl;

        // Преобразуем полученную информацию (пример: добавим текст)
        std::string msg(buffer);
        std::string response = "Server processed i=" + msg;

        // Отправляем обратно клиенту
        ssize_t sent = sendto(sockfd, response.c_str(), response.size(), 0,
                              reinterpret_cast<sockaddr*>(&cliaddr), cli_len);
        if (sent < 0) {
            perror("sendto");
        }
    }

    close(sockfd);
    return 0;
}
