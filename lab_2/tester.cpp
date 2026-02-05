//
// Created by Arti on 05.02.2026.
//

#include <iostream>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <cstring>

int main(int argc, char* argv[]) {
    if (argc != 4) {
        std::cerr << "Использование: " << argv[0] << " <IP> <порт> <количество>\n";
        std::cerr << "Пример: " << argv[0] << " 127.0.0.1 12345 3\n";
        return 1;
    }

    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        perror("socket");
        return 1;
    }

    struct sockaddr_in addr = {};
    addr.sin_family = AF_INET;
    addr.sin_port = htons(std::atoi(argv[2]));

    if (inet_pton(AF_INET, argv[1], &addr.sin_addr) <= 0) {
        std::cerr << "Неверный IP-адрес\n";
        close(sock);
        return 1;
    }

    if (connect(sock, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
        perror("connect");
        close(sock);
        return 1;
    }

    std::cout << "Подключен к " << argv[1] << ":" << argv[2] << std::endl;

    int count = std::atoi(argv[3]);
    int value = count;

    for (int i = 0; i < count; ++i) {
        std::cout << "Отправка #" << (i+1) << "/" << count << ": " << value << std::endl;

        if (write(sock, &value, sizeof(value)) != sizeof(value)) {
            perror("write");
            break;
        }
        sleep(value);
    }

    close(sock);
    return 0;
}

