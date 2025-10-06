#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <unistd.h>
#include <fcntl.h>

void print_proc_maps() {
    char path[256];
    snprintf(path, sizeof(path), "/proc/%d/maps", getpid());
    FILE* file = fopen(path, "r");
    if (!file) {
        perror("fopen");
        return;
    }
    printf("\n=== /proc/%d/maps ===\n", getpid());
    char line[256];
    while (fgets(line, sizeof(line), file)) {
        printf("%s", line);
    }
    fclose(file);
    printf("=====================\n");
}

int main() {
    print_proc_maps();

    void* handle = dlopen("./libmylist.so", RTLD_NOW);
    if (!handle) {
        fprintf(stderr, "dlopen error: %s\n", dlerror());
        return 1;
    }
    printf("Library loaded via dlopen\n");

    print_proc_maps();

    dlclose(handle);
    printf("Library unloaded via dlclose\n");

    print_proc_maps();

    return 0;
}
