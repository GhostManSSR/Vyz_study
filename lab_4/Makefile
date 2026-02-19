CXX = g++
CXXFLAGS = -Wall -Wextra -std=c++11 -g
LDFLAGS = -lsctp

# Сервер
server: server.o
	$(CXX) $(CXXFLAGS) server.o -o server $(LDFLAGS)

server.o: server.cpp
	$(CXX) $(CXXFLAGS) -c server.cpp

# Клиент
client: client.o
	$(CXX) $(CXXFLAGS) client.o -o client $(LDFLAGS)

client.o: client.cpp
	$(CXX) $(CXXFLAGS) -c client.cpp

# Оба сразу
all: server client

clean:
	rm -f *.o server client

.PHONY: clean all
