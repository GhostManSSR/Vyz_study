#include <iostream>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <string>
#include <netinet/sctp.h>
#include <fcntl.h>
#include <sys/select.h>
#include <signal.h>
#include <atomic>

std::atomic<bool> running(true);

void signal_handler(int /*sig*/) {
    running = false;
    std::cout << "\nОстановка..." << std::endl;
}

int main(int argc, char* argv[]) {
    if (argc != 5) {
        std::cerr << "Использование: " << argv[0] << " <IP> <port> <число> <delay>" << std::endl;
        std::cerr << "Пример: " << argv[0] << " 127.0.0.1 36329 2 3" << std::endl;
        return 1;
    }

    std::string ip = argv[1];
    int port = std::stoi(argv[2]);
    int number = std::stoi(argv[3]);
    int delay = std::stoi(argv[4]);

    signal(SIGINT, signal_handler);

    int sock = socket(AF_INET, SOCK_SEQPACKET, IPPROTO_SCTP);
    if (sock < 0) { perror("socket"); return 1; }

    struct sctp_initmsg initmsg = {
        .sinit_num_ostreams = 5,
        .sinit_max_instreams = 5,
        .sinit_max_attempts = 5,
        .sinit_max_init_timeo = 2000
    };
    setsockopt(sock, IPPROTO_SCTP, SCTP_INITMSG, &initmsg, sizeof(initmsg));

    struct sockaddr_in server_addr;
    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(port);
    inet_pton(AF_INET, ip.c_str(), &server_addr.sin_addr);

    std::cout << "Подключение к " << ip << ":" << port << std::endl;

    if (connect(sock, (struct sockaddr*)&server_addr, sizeof(server_addr)) < 0) {
        perror("connect");
        close(sock);
        return 1;
    }

    std::cout << "Подключен к " << ip << ":" << port << std::endl;

    std::string message = std::to_string(number) + "\n";

    std::cout << "Отправка '" << number << "' каждые " << delay
              << "с (Ctrl+C для остановки)..." << std::endl;

    while (running) {
        ssize_t bytes_sent = sctp_sendmsg(sock, message.c_str(), message.length(),
                                        NULL, 0, 0, 0, 0, 0, 0);

        if (bytes_sent > 0) {
            std::cout << "" << bytes_sent << " байт отправлено" << std::endl;
        } else {
            perror("sctp_sendmsg");
            break;
        }

        for (int i = 0; i < delay && running; i++) {
            sleep(1);
        }
    }

    std::cout << "Отключение..." << std::endl;
    close(sock);
    return 0;
}
