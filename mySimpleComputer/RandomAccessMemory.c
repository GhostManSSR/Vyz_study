#include <stdio.h>
#include <stdlib.h>
#include "RandomAccessMemory.h"

static int memory[MAX_MEMORY];

int sc_memoryInit(void) {
    for (int i = 0; i < MAX_MEMORY; i++) {
        memory[i] = 0; 
    }
    return 0; 
}

int sc_memorySet(int address, int value) {
    if (address < 0 || address >= MAX_MEMORY || value < -32768 || value > 32767) {
        return -1; 
    }
    memory[address] = value; 
    return 0; 
}

int sc_memoryGet(int address, int *value) {
    if (address < 0 || address >= MAX_MEMORY || value == NULL) {
        return -1;
    }
    *value = memory[address]; 
    return 0; 
}

int sc_memorySave(char *filename) {
    if (filename == NULL) {
        return -1;
    }
    FILE *file = fopen(filename, "wb");
    if (!file) {
        return -1; 
    }
    fwrite(memory, sizeof(int), MAX_MEMORY, file); 
    fclose(file); 
    return 0; 
}


int sc_memoryLoad(char *filename) {
    if (filename == NULL) {
        return -1; 
    }
    FILE *file = fopen(filename, "rb");
    if (!file) {
        return -1; 
    }
    int result = fread(memory, sizeof(int), MAX_MEMORY, file); 
    fclose(file); 
    if (result < 0) {
        return -1; 
    }
    return 0; 
}
