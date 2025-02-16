#ifndef MYSIMPLECOMPUTER_H
#define MYSIMPLECOMPUTER_H

#define MAX_MEMORY 128

int sc_memoryInit(void);
int sc_memorySet(int address, int value);
int sc_memoryGet(int address, int *value);
int sc_memorySave(char *filename);
int sc_memoryLoad(char *filename);

#endif 
