#include <iostream>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <string>

int main(int argc, char* argv[]) {
    if (argc != 5) {
        std::cerr << "Использование: " << argv[0] << " <IP> <port> <число> <delay>" << std::endl;
        std::cerr << "Пример: " << argv[0] << " 127.0.0.1 40715 2 3" << std::endl;
        std::cerr << "       Отправит число '2' с задержкой 3 сек" << std::endl;
        return 1;
    }

    std::string ip = argv[1];
    int port = std::stoi(argv[2]);
    int number = std::stoi(argv[3]);
    int delay = std::stoi(argv[4]);

    if (number < 1 || number > 10) {
        std::cerr << "Число должно быть от 1 до 10!" << std::endl;
        return 1;
    }

    if (delay < 1 || delay > 10) {
        std::cerr << "Задержка должна быть от 1 до 10 секунд!" << std::endl;
        return 1;
    }

    int sock;
    struct sockaddr_in server_addr;

    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        perror("socket");
        return 1;
    }

    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(port);
    inet_pton(AF_INET, ip.c_str(), &server_addr.sin_addr);

    if (connect(sock, (struct sockaddr*)&server_addr, sizeof(server_addr)) < 0) {
        perror("connect");
        close(sock);
        return 1;
    }

    std::cout << "Подключен к серверу " << ip << ":" << port << std::endl;
    std::cout << "Отправка числа '" << number << "' с интервалом " << delay
              << " сек (Ctrl+C для остановки)..." << std::endl;

    while (true) {
        std::string message = std::to_string(number) + "\n";
        ssize_t bytes_sent = write(sock, message.c_str(), message.length());

        if (bytes_sent < 0) {
            perror("write");
            break;
        }

        std::cout << "Отправлено: " << number << std::endl;

        sleep(delay);
    }

    close(sock);
    return 0;
}
