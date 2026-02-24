#include <iostream>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <cstring>
#include <arpa/inet.h>
#include <netinet/sctp.h>
#include <map>

int main() {

    int listen_fd = socket(AF_INET, SOCK_STREAM, IPPROTO_SCTP);
    if (listen_fd < 0) {
        perror("socket");
        return 1;
    }

    sockaddr_in addr{};
    addr.sin_family = AF_INET;
    addr.sin_port = htons(0);
    addr.sin_addr.s_addr = INADDR_ANY;

    if (bind(listen_fd, (sockaddr*)&addr, sizeof(addr)) < 0) {
        perror("bind");
        return 1;
    }

    if (listen(listen_fd, 10) < 0) {
        perror("listen");
        return 1;
    }

    socklen_t len = sizeof(addr);
    getsockname(listen_fd, (sockaddr*)&addr, &len);

    std::cout << "SERVER STARTED ON PORT: "
              << ntohs(addr.sin_port) << std::endl;

    fd_set master_set, read_set;

    FD_ZERO(&master_set);
    FD_SET(listen_fd, &master_set);

    int max_fd = listen_fd;

    std::map<int, sockaddr_in> clients;

    while (true) {

        read_set = master_set;

        if (select(max_fd + 1, &read_set, nullptr, nullptr, nullptr) < 0) {
            perror("select");
            break;
        }

        for (int fd = 0; fd <= max_fd; fd++) {

            if (!FD_ISSET(fd, &read_set))
                continue;

            if (fd == listen_fd) {

                sockaddr_in client_addr{};
                socklen_t clen = sizeof(client_addr);

                int client_fd = accept(listen_fd, (sockaddr*)&client_addr, &clen);

                if (client_fd < 0) {
                    perror("accept");
                    continue;
                }

                clients[client_fd] = client_addr;

                std::cout << "Client connected " << inet_ntoa(client_addr.sin_addr) << ":" << ntohs(client_addr.sin_port) << std::endl;

                FD_SET(client_fd, &master_set);

                if (client_fd > max_fd)
                    max_fd = client_fd;
            }
            else {

                char buf[256];
                int flags = 0;
                struct sctp_sndrcvinfo sri{};

                int n = sctp_recvmsg(fd, buf, sizeof(buf) - 1, NULL, 0, &sri, &flags);

                if (n <= 0) {

                    auto c = clients[fd];

                    std::cout << "Client disconnected " << inet_ntoa(c.sin_addr) << ":" << ntohs(c.sin_port) << std::endl;

                    close(fd);
                    FD_CLR(fd, &master_set);
                    clients.erase(fd);

                    continue;
                }

                buf[n] = 0;

                int value = atoi(buf);

                std::cout << "Received from " << inet_ntoa(clients[fd].sin_addr) << ":" << ntohs(clients[fd].sin_port) << " -> " << value << std::endl;

                int result = value * 2;

                std::string reply = std::to_string(result) + "\n";

                sctp_sendmsg(fd, reply.c_str(), reply.size(), NULL, 0, 0, 0, 0, 0, 0);
            }
        }
    }

    close(listen_fd);
    return 0;
}
