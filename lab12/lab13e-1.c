#include <semaphore.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/mman.h>

int main(void)
{
    int n = 0;
    int fd;
    char* sh;
    sem_t *sem;

    // Создаем разделяемую память
    fd = shm_open("/common_region", O_RDWR | O_CREAT,
                  S_IRUSR | S_IWUSR | S_IRGRP);
    if (fd == -1) {
        perror("shm_open");
        return 1;
    }
    ftruncate(fd, 6);
    
    sh = (char*)mmap(0, 6, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
    if (sh == MAP_FAILED) {
        perror("mmap");
        close(fd);
        return 1;
    }
    memset(sh, 0, 6);

    // Создаем именованный семафор
    sem = sem_open("/common_sem", O_CREAT, 
                   S_IRUSR | S_IWUSR | S_IRGRP, 1);
    if (sem == SEM_FAILED) {
        perror("sem_open");
        return 1;
    }

    // Читаем данные с синхронизацией
    while(n++ < 10) {
        sem_wait(sem);
        printf("String: %s\n", sh);
        sem_post(sem);
        usleep(500000); // 0.5 секунды
    }

    // Освобождаем ресурсы
    shm_unlink("/common_region");
    munmap(sh, 6);
    sem_unlink("/common_sem");
    sem_close(sem);
    
    return 0;
}