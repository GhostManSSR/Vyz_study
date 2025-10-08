#include <stdio.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include <sys/mman.h>

int main(int argc, char* const argv[])
{
    int fd;
    struct stat stat_file;
    char* map_address;

    // Создаем файл для разделяемой памяти
    fd = open("test_shared.txt", O_RDWR | O_CREAT, 
              S_IRUSR | S_IWUSR | S_IRGRP | S_IROTH);
    if (fd == -1) {
        fprintf(stderr, "open error\n");
        return 1;
    }

    // Устанавливаем размер файла
    ftruncate(fd, 256);

    // Отображаем файл в память
    map_address = (char*)mmap(0, 256,
                             PROT_READ | PROT_WRITE,
                             MAP_SHARED, fd, 0);
    if (map_address == MAP_FAILED) {
        fprintf(stderr, "mmap error\n");
        close(fd);
        return 1;
    }

    close(fd);

    // Записываем данные в разделяемую память
    memcpy(map_address, "Take it easy! Hello from process 1!", 
           strlen("Take it easy! Hello from process 1!") + 1);
    
    printf("Данные записаны в разделяемую память. Нажмите Enter для завершения...\n");
    getc(stdin);

    // Освобождаем ресурсы
    munmap(map_address, 256);
    
    return 0;
}