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

    // Создаем объект разделяемой памяти POSIX
    fd = shm_open("/common_region", O_RDWR | O_CREAT,
                  S_IRUSR | S_IWUSR | S_IRGRP);
    if (fd == -1) {
        perror("shm_open");
        return 1;
    }

    // Устанавливаем размер
    ftruncate(fd, 256);

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

    // Записываем данные
    memcpy(map_address, "Take it easy! Be happy! POSIX shared memory works!", 
           strlen("Take it easy! Be happy! POSIX shared memory works!") + 1);
    
    printf("Данные записаны в POSIX разделяемую память. Нажмите Enter...\n");
    getc(stdin);

    // Освобождаем ресурсы
    munmap(map_address, 256);
    shm_unlink("/common_region");
    
    return 0;
}