#include <pthread.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/mman.h>

int main(void)
{
    int fd;
    int n = 0;
    char* sh;
    pthread_mutex_t* Mutex;
    pthread_mutexattr_t mutex_attr;

    // Создаем разделяемую память для данных
    fd = shm_open("/common_region1", O_RDWR | O_CREAT,
                  S_IRUSR | S_IWUSR | S_IRGRP);
    if (fd == -1) {
        perror("shm_open data");
        return 1;
    }
    ftruncate(fd, 6);
    
    sh = (char*)mmap(0, 6, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
    if (sh == MAP_FAILED) {
        perror("mmap data");
        close(fd);
        return 1;
    }
    close(fd);
    memset(sh, 0, 6);

    // Создаем разделяемую память для мьютекса
    fd = shm_open("/common_mutex", O_RDWR | O_CREAT,
                  S_IRUSR | S_IWUSR | S_IRGRP);
    if (fd == -1) {
        perror("shm_open mutex");
        return 1;
    }
    ftruncate(fd, sizeof(pthread_mutex_t));

    // Настраиваем атрибуты мьютекса для разделения между процессами
    pthread_mutexattr_init(&mutex_attr);
    pthread_mutexattr_setpshared(&mutex_attr, PTHREAD_PROCESS_SHARED);
    
    Mutex = (pthread_mutex_t*)mmap(0, sizeof(pthread_mutex_t),
                                  PROT_READ | PROT_WRITE, 
                                  MAP_SHARED, fd, 0);
    close(fd);
    
    pthread_mutex_init(Mutex, &mutex_attr);

    // Читаем данные с синхронизацией
    while(n++ < 10) {
        pthread_mutex_lock(Mutex);
        printf("String: %s\n", sh);
        pthread_mutex_unlock(Mutex);
        sleep(1);
    }

    // Освобождаем ресурсы
    munmap(sh, 6);
    munmap(Mutex, sizeof(pthread_mutex_t));
    shm_unlink("/common_mutex");
    shm_unlink("/common_region1");
    
    return 0;
}