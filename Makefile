.PHONY: all clean

TARGET = program.exe
OBJECTS = $(patsubst console/%.c, console/%.o, $(wildcard console/*.c))

all: $(TARGET)
	./$(TARGET)

$(TARGET): $(OBJECTS)
	$(CC) -o $@ $(OBJECTS)

console/%.o: console/%.c
	$(CC) -c -Wall $< -o $@

clean:
	$(MAKE) -C console clean
	rm -f $(TARGET) $(OBJECTS)
