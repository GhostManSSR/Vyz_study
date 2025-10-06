// mylist.c - библиотека для работы с рекурсивным списком

#include <stdio.h>
#include <stdlib.h>

typedef struct Node {
    int value;
    struct Node *next;
} Node;

// Рекурсивное добавление в конец списка
void add_node(Node **head, int val) {
    if (*head == NULL) {
        *head = (Node*)malloc(sizeof(Node));
        (*head)->value = val;
        (*head)->next = NULL;
    } else {
        add_node(&((*head)->next), val);
    }
}

// Рекурсивный поиск значения в списке (возвращает указатель на найденный узел или NULL)
Node* find_node(Node *head, int val) {
    if (head == NULL) return NULL;
    if (head->value == val) return head;
    return find_node(head->next, val);
}

// Рекурсивный вывод элементов списка
void print_list(Node *head) {
    if (head == NULL) {
        printf("NULL\n");
        return;
    }
    printf("%d -> ", head->value);
    print_list(head->next);
}

// Рекурсивное освобождение памяти списка
void free_list(Node *head) {
    if (head == NULL) return;
    free_list(head->next);
    free(head);
}