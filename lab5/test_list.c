// test_list.c

#include <stdio.h>

typedef struct Node {
    int value;
    struct Node *next;
} Node;

// Прототипы функций из библиотеки
void add_node(Node **head, int val);
Node* find_node(Node *head, int val);
void print_list(Node *head);
void free_list(Node *head);

int main() {
    Node *head = NULL;

    add_node(&head, 10);
    add_node(&head, 20);
    add_node(&head, 30);

    print_list(head);

    int search_val = 20;
    Node *found = find_node(head, search_val);
    if (found) {
        printf("Элемент %d найден\n", search_val);
    } else {
        printf("Элемент %d не найден\n", search_val);
    }

    free_list(head);
    return 0;
}