CC = gcc
CFLAGS = -Wall -Wextra
LDFLAGS = -lrt -lpthread

# Все цели
ALL = lab13b lab13c-1 lab13c-2 lab13d-1 lab13d-2 lab13em-1 lab13em-2 lab13e-1 lab13e-2

all: $(ALL)

# Правила компиляции
lab13b: lab13b.c
	$(CC) $(CFLAGS) -o $@ $<

lab13c-1: lab13c-1.c
	$(CC) $(CFLAGS) -o $@ $<

lab13c-2: lab13c-2.c
	$(CC) $(CFLAGS) -o $@ $<

lab13d-1: lab13d-1.c
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS)

lab13d-2: lab13d-2.c
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS)

lab13em-1: lab13em-1.c
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS)

lab13em-2: lab13em-2.c
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS)

lab13e-1: lab13e-1.c
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS)

lab13e-2: lab13e-2.c
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS)

# Очистка
clean:
	rm -f $(ALL) test.txt test_shared.txt
	rm -f /dev/shm/common_region /dev/shm/common_region1 /dev/shm/common_mutex
	rm -f /dev/shm/sem.common_sem

# Проверка ресурсов
check:
	./check_resources.sh

.PHONY: all clean check