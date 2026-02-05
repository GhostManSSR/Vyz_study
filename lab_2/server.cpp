//
// Created by Arti on 05.02.2026.
//

#include <iostream>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/wait.h>
#include <signal.h>
#include <cstring>

void handle_zombie(int sig) {
    while (waitpid(-1, NULL, WNOHANG) > 0);
}

int main() {
    struct sigaction sa = {};
    sa.sa_handler = handle_zombie;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_RESTART | SA_NOCLDWAIT;
    sigaction(SIGCHLD, &sa, nullptr);

    int sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock < 0) {
        perror("socket");
        return 1;
    }

    struct sockaddr_in addr = {};
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = INADDR_ANY;
    addr.sin_port = 0;

    if (bind(sock, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
        perror("bind");
        close(sock);
        return 1;
    }

    socklen_t len = sizeof(addr);
    getsockname(sock, (struct sockaddr*)&addr, &len);
    std::cout << "Сервер слушает на порту: " << ntohs(addr.sin_port) << std::endl;

    listen(sock, 10);

    std::cout << "Ожидание подключений..." << std::endl;

    while (true) {
        struct sockaddr_in client_addr;
        socklen_t client_len = sizeof(client_addr);
        int client_sock = accept(sock, (struct sockaddr*)&client_addr, &client_len);

        if (client_sock < 0) {
            perror("accept");
            continue;
        }

        pid_t pid = fork();
        if (pid < 0) {
            perror("fork");
            close(client_sock);
            continue;
        }

        if (pid == 0) {
            close(sock);

            char ip[INET_ADDRSTRLEN];
            inet_ntop(AF_INET, &client_addr.sin_addr, ip, sizeof(ip));
            std::cout << "Клиент " << ip << ":" << ntohs(client_addr.sin_port)
                      << " подключился (PID: " << getpid() << ")" << std::endl;

            int num;
            while (read(client_sock, &num, sizeof(num)) > 0) {
                std::cout << "PID " << getpid() << " от клиента " << ip
                          << ": " << num << std::endl;
            }

            std::cout << "Клиент " << ip << " отключился (PID: " << getpid() << ")" << std::endl;
            close(client_sock);
            exit(0);
        } else {
            close(client_sock);
        }
    }

    close(sock);
    return 0;
}
