#include <sys/types.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/wait.h>

int main() {
    pid_t pid1, pid2, pid3;

    pid1 = fork();
    if (pid1 == 0) {
        // Первый дочерний процесс
        printf("Child 1: PID = %d, PPID = %d\n", getpid(), getppid());
        execl("/bin/echo", "echo", "Hello from child 1", NULL);
        perror("exec failed");
        exit(1);
    }

    pid2 = fork();
    if (pid2 == 0) {
        // Второй дочерний процесс
        printf("Child 2: PID = %d, PPID = %d\n", getpid(), getppid());
        execl("/bin/echo", "echo", "Hello from child 2", NULL);
        perror("exec failed");
        exit(2);
    }

    pid3 = fork();
    if (pid3 == 0) {
        // Третий дочерний процесс
        printf("Child 3: PID = %d, PPID = %d\n", getpid(), getppid());
        execl("/bin/echo", "echo", "Hello from child 3", NULL);
        perror("exec failed");
        exit(3);
    }

    // Родительский процесс
    printf("Parent: PID = %d\n", getpid());
    printf("Launched child processes: %d, %d, %d\n", pid1, pid2, pid3);

    // Ожидание завершения всех дочерних процессов
    waitpid(pid1, NULL, 0);
    waitpid(pid2, NULL, 0);
    waitpid(pid3, NULL, 0);

    printf("All child processes completed.\n");
    return 0;
}
