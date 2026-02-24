#include <iostream>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <string>
#include <netinet/sctp.h>
#include <signal.h>
#include <atomic>

std::atomic<bool> running(true);

void handler(int) {
    running = false;
}

int main(int argc, char* argv[]) {

    if (argc != 4) {
        std::cout << "Usage: client <ip> <port> <number>\n";
        return 1;
    }

    std::string ip = argv[1];
    int port = atoi(argv[2]);
    int number = atoi(argv[3]);

    signal(SIGINT, handler);

    int sock = socket(AF_INET, SOCK_STREAM, IPPROTO_SCTP);
    if (sock < 0) {
        perror("socket");
        return 1;
    }

    sockaddr_in addr{};
    addr.sin_family = AF_INET;
    addr.sin_port = htons(port);
    inet_pton(AF_INET, ip.c_str(), &addr.sin_addr);

    std::cout << "Connecting to "
              << ip << ":" << port << std::endl;

    if (connect(sock, (sockaddr*)&addr, sizeof(addr)) < 0) {
        perror("connect");
        return 1;
    }

    std::cout << "Connected\n";

    std::string msg = std::to_string(number) + "\n";

    while (running) {

        ssize_t sent = sctp_sendmsg(sock, msg.c_str(), msg.size(), NULL, 0, 0, 0, 0, 0, 0);

        if (sent < 0) {
            perror("send");
            break;
        }

        char buf[256];
        struct sctp_sndrcvinfo sri{};
        int flags = 0;

        int n = sctp_recvmsg(sock, buf, sizeof(buf) - 1,  NULL, 0, &sri, &flags);

        if (n > 0) {
            buf[n] = 0;
            std::cout << "Server reply: " << buf;
        }

        sleep(number);
    }

    std::cout << "Disconnecting...\n";

    close(sock);
}
