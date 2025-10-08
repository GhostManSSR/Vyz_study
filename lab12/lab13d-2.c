#include <stdio.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include <sys/mman.h>

int main(int argc, char* const argv[])
{
    int fd;
    char* map_address;

    // Открываем существующий объект разделяемой памяти
    fd = shm_open("/common_region", O_RDWR,
                  S_IRUSR | S_IWUSR | S_IRGRP);
    if (fd == -1) {
        perror("shm_open");
        return 1;
    }

    // Отображаем в память
    map_address = (char*)mmap(0, 256,
                             PROT_READ | PROT_WRITE,
                             MAP_SHARED, fd, 0);
    if (map_address == MAP_FAILED) {
        perror("mmap");
        close(fd);
        return 1;
    }

    close(fd);

    printf("Содержимое POSIX разделяемой памяти: %s\n", map_address);
    
    printf("Нажмите Enter для завершения...\n");
    getc(stdin);

    munmap(map_address, 256);
    
    return 0;
}