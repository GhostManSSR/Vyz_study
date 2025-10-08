#include <stdlib.h>
#include <stdio.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <time.h>
#include <unistd.h>
#include <sys/mman.h>

int main(int argc, char* const argv[])
{
    int fd;
    struct stat stat_file;
    char dummy;
    char* map_address;

    // Создаем и открываем файл
    fd = open("test.txt", O_RDWR | O_CREAT, S_IRUSR | S_IWUSR | S_IRGRP);
    if (fd == -1) {
        fprintf(stderr, "open error\n");
        return 1;
    }

    // Записываем начальные данные в файл
    write(fd, "This is a test", 14);
    
    // Получаем информацию о файле
    if (fstat(fd, &stat_file) == -1) {
        fprintf(stderr, "stat error\n");
        close(fd);
        return 1;
    }

    // Отображаем файл в память
    map_address = (char*)mmap(0, stat_file.st_size, 
                             PROT_READ | PROT_WRITE,
                             MAP_PRIVATE, fd, 0);
    if (map_address == MAP_FAILED) {
        fprintf(stderr, "mmap error\n");
        close(fd);
        return 1;
    }

    // Манипулируем данными через отображенную память
    printf("Исходная строка: %s\n", map_address);
    
    dummy = map_address[1];
    map_address[0] = map_address[5] - 0x20;
    map_address[1] = map_address[3];
    map_address[2] = map_address[4];
    map_address[3] = map_address[10];
    map_address[4] = dummy;
    map_address[14] = '?';

    printf("Измененная строка: %s\n", map_address);

    // Синхронизируем изменения с файлом
    msync(map_address, stat_file.st_size, MS_SYNC);
    
    // Освобождаем ресурсы
    munmap(map_address, stat_file.st_size);
    close(fd);
    
    return 0;
}