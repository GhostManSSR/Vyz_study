CXX=g++
CXXFLAGS=-Wall -g -O2 -std=c++17 -pthread
TARGETS=server client tester

all: $(TARGETS)

server: server.cpp
	$(CXX) $(CXXFLAGS) -o $@ $^

client: client.cpp
	$(CXX) $(CXXFLAGS) -o $@ $^

tester: tester.cpp
	$(CXX) $(CXXFLAGS) -o $@ $^

clean:
	rm -f $(TARGETS)

.PHONY: all clean

