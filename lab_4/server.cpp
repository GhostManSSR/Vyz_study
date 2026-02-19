#include <iostream>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <cstring>
#include <netinet/sctp.h>
#include <arpa/inet.h>

int main() {
    int sfd = socket(AF_INET, SOCK_SEQPACKET, IPPROTO_SCTP);
    if (sfd < 0) { perror("socket"); return 1; }

    struct sctp_event_subscribe events = {0};
    events.sctp_data_io_event = 1;
    events.sctp_association_event = 1;
    setsockopt(sfd, SOL_SCTP, SCTP_EVENTS, &events, sizeof(events));

    struct sockaddr_in addr = {};
    addr.sin_family = AF_INET;
    addr.sin_port = htons(0);
    addr.sin_addr.s_addr = INADDR_ANY;

    if (bind(sfd, (struct sockaddr*)&addr, sizeof(addr)) < 0) {
        perror("bind"); return 1;
    }

    listen(sfd, 5);

    socklen_t addr_len = sizeof(addr);
    if (getsockname(sfd, (struct sockaddr*)&addr, &addr_len) < 0) {
        perror("getsockname"); return 1;
    }

    listen(sfd, 5);
    std::cout << "SCTP сервер готов на порту: "
              << ntohs(addr.sin_port) << std::endl;

    while (true) {
        struct sockaddr_in client_addr = {};
        socklen_t len = sizeof(client_addr);
        char buf[256];
        struct sctp_sndrcvinfo sri = {};
        int flags = 0;

        ssize_t n = sctp_recvmsg(sfd, buf, sizeof(buf)-1,
                               (struct sockaddr*)&client_addr, &len, &sri, &flags);

       // if (n > 0 && n != 20) {  // Только данные, НЕ heartbeat
       //     std::cout << buf[0] << std::endl;
        //}
    }
}
