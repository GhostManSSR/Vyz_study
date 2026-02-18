CXX = g++

CXXFLAGS = -Wall -Wextra -std=c++11 -g

SERVER = server
CLIENT = client

SERVER_OBJ = server.o
CLIENT_OBJ = client.o

.PHONY: all
all: $(SERVER) $(CLIENT)

$(SERVER): $(SERVER_OBJ)
	$(CXX) $(CXXFLAGS) -o $(SERVER) $(SERVER_OBJ)

$(CLIENT): $(CLIENT_OBJ)
	$(CXX) $(CXXFLAGS) -o $(CLIENT) $(CLIENT_OBJ)

server.o: server.cpp
	$(CXX) $(CXXFLAGS) -c server.cpp

client.o: client.cpp
	$(CXX) $(CXXFLAGS) -c client.cpp

.PHONY: server_start
server_start: $(SERVER)
	./$(SERVER)

.PHONY: clean
clean:
	rm -f $(SERVER) $(CLIENT) *.o
