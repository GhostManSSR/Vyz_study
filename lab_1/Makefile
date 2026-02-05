CXX     = g++
CXXFLAGS= -std=c++17 -Wall -Wextra -O2

SERVER  = udp_server
CLIENT  = udp_client

.PHONY: all clean run_server run_client test help

all: $(SERVER) $(CLIENT)

$(SERVER): udp_server.cpp
	$(CXX) $(CXXFLAGS) -o $@ $<

$(CLIENT): udp_client.cpp
	$(CXX) $(CXXFLAGS) -o $@ $<

run_server: $(SERVER)
	./$(SERVER)

run_client: $(CLIENT)
	./$(CLIENT) 127.0.0.1 50139 2 5

test:
	./$(SERVER) &
	@echo "Server started. Starting clients..."
	sleep 1
	./$(CLIENT) 127.0.0.1 50139 2 3 &
	./$(CLIENT) 127.0.0.1 50139 3 2 &
	wait

clean:
	rm -f $(SERVER) $(CLIENT) *.o
