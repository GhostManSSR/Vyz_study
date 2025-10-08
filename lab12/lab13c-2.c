#include <fcntl.h>
#include <stdio.h>
#include <sys/stat.h>
#include <unistd.h>
#include <string.h>
#include <sys/mman.h>

int main(int argc, char* const argv[])
{
    int fd;
    struct stat stat_file;
    char* map_address;

    // Открываем существующий файл разделяемой памяти
    fd = open("test_shared.txt", O_RDWR);
    if (fd == -1) {
        fprintf(stderr, "open error\n");
        return 1;
    }

    // Отображаем файл в память
    map_address = (char*)mmap(0, 256,
                             PROT_READ | PROT_WRITE,
                             MAP_SHARED, fd, 0);
    if (map_address == MAP_FAILED) {
        fprintf(stderr, "mmap error\n");
        close(fd);
        return 1;
    }

    printf("Содержимое разделяемой памяти: %s\n", map_address);
    
    printf("Нажмите Enter для завершения...\n");
    getc(stdin);

    close(fd);
    munmap(map_address, 256);
    
    return 0;
}