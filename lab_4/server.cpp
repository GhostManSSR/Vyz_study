#include <iostream>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/select.h>
#include <cstring>
#include <vector>
#include <map>

int main() {
    int server_fd, client_fd;
    struct sockaddr_in server_addr, client_addr;
    socklen_t client_len = sizeof(client_addr);

    server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd < 0) {
        perror("socket");
        return 1;
    }

    int opt = 1;
    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = 0;

    if (bind(server_fd, (struct sockaddr*)&server_addr, sizeof(server_addr)) < 0) {
        perror("bind");
        close(server_fd);
        return 1;
    }

    socklen_t addr_len = sizeof(server_addr);
    getsockname(server_fd, (struct sockaddr*)&server_addr, &addr_len);
    uint16_t server_port = ntohs(server_addr.sin_port);
    std::cout << "Сервер запущен на порту: " << server_port << std::endl;

    listen(server_fd, 10);

    std::vector<int> client_sockets;
    std::map<int, std::pair<std::string, uint16_t>> client_info;
    fd_set readfds;
    int max_fd = server_fd;

    std::cout << "Ожидание подключений клиентов..." << std::endl;

    while (true) {
        FD_ZERO(&readfds);

        FD_SET(server_fd, &readfds);

        for (int sock : client_sockets) {
            FD_SET(sock, &readfds);
            if (sock > max_fd) {
                max_fd = sock;
            }
        }

        int activity = select(max_fd + 1, &readfds, NULL, NULL, NULL);

        if (activity < 0) {
            perror("select");
            break;
        }

        if (FD_ISSET(server_fd, &readfds)) {
            client_fd = accept(server_fd, (struct sockaddr*)&client_addr, &client_len);
            if (client_fd >= 0) {
                char client_ip[INET_ADDRSTRLEN];
                uint16_t client_port = ntohs(client_addr.sin_port);
                inet_ntop(AF_INET, &client_addr.sin_addr, client_ip, INET_ADDRSTRLEN);

                std::cout << "Новое подключение от: " << client_ip
                          << ":" << client_port << std::endl;

                client_info[client_fd] = {std::string(client_ip), client_port};

                client_sockets.push_back(client_fd);
                if (client_fd > max_fd) {
                    max_fd = client_fd;
                }
            }
        }

        for (size_t i = 0; i < client_sockets.size(); ) {
            int sock = client_sockets[i];
            if (FD_ISSET(sock, &readfds)) {
                char buffer[256];
                ssize_t bytes_read = read(sock, buffer, sizeof(buffer) - 1);

                if (bytes_read <= 0) {
                    auto it = client_info.find(sock);
                    if (it != client_info.end()) {
                        std::cout << "Клиент " << it->second.first
                                  << ":" << it->second.second << " отключился" << std::endl;
                        client_info.erase(it);
                    }

                    close(sock);
                    client_sockets.erase(client_sockets.begin() + i);
                    if (sock == max_fd) {
                        max_fd--;
                    }
                    continue;
                }

                buffer[bytes_read] = '\0';
                std::cout << "Получено от клиента " << client_info[sock].first
                          << ":" << client_info[sock].second << ": " << buffer << std::endl;
            }
            i++;
        }
    }

    for (int sock : client_sockets) {
        close(sock);
    }
    close(server_fd);

    return 0;
}
