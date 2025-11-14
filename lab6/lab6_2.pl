  %% Лабораторная работа №6
% Задача I.2: Минимальный элемент двоичного дерева
% Задача II.2: Достижимые вершины в ориентированном графе

% === ЧАСТЬ I: РАБОТА С ДВОИЧНЫМИ ДЕРЕВЬЯМИ ===

% Предикат для представления бинарного дерева
% tree(Value, Left, Right) или nil для пустого дерева

% Минимальный элемент в бинарном дереве
min_tree(nil, _) :- fail. % Пустое дерево не имеет минимального элемента
min_tree(tree(Value, nil, nil), Value) :- !. % Лист
min_tree(tree(Value, Left, nil), Min) :-
    min_tree(Left, LeftMin),
    Min is min(Value, LeftMin), !.
min_tree(tree(Value, nil, Right), Min) :-
    min_tree(Right, RightMin),
    Min is min(Value, RightMin), !.
min_tree(tree(Value, Left, Right), Min) :-
    min_tree(Left, LeftMin),
    min_tree(Right, RightMin),
    Min1 is min(Value, LeftMin),
    Min is min(Min1, RightMin).

% Примеры деревьев для тестирования
tree1(tree(5, 
          tree(3, 
               tree(1, nil, nil), 
               tree(4, nil, nil)), 
          tree(8, 
               tree(6, nil, nil), 
               tree(9, nil, nil)))).

tree2(tree(10,
          tree(5,
               tree(2, nil, nil),
               tree(7, nil, nil)),
          tree(15,
               nil,
               tree(20, nil, nil)))).

tree3(tree(1, nil, nil)). % Одно элементное дерево

% Предикат для красивого вывода дерева
print_tree(nil, _).
print_tree(tree(Value, Left, Right), Level) :-
    NewLevel is Level + 4,
    print_tree(Right, NewLevel),
    tab(Level),
    write(Value), nl,
    print_tree(Left, NewLevel).

display_tree(Tree) :-
    write('Бинарное дерево:'), nl,
    print_tree(Tree, 0).

% Тест для минимального элемента
test_tree_min :-
    nl, write('=== ТЕСТ МИНИМАЛЬНОГО ЭЛЕМЕНТА ДЕРЕВА ==='), nl, nl,
    tree1(Tree1),
    write('Дерево 1:'), nl,
    display_tree(Tree1),
    (   min_tree(Tree1, Min1)
    ->  format('Минимальный элемент: ~w~n', [Min1])
    ;   write('Дерево пустое~n')
    ),
    nl,
    
    tree2(Tree2),
    write('Дерево 2:'), nl,
    display_tree(Tree2),
    (   min_tree(Tree2, Min2)
    ->  format('Минимальный элемент: ~w~n', [Min2])
    ;   write('Дерево пустое~n')
    ),
    nl,
    
    tree3(Tree3),
    write('Дерево 3:'), nl,
    display_tree(Tree3),
    (   min_tree(Tree3, Min3)
    ->  format('Минимальный элемент: ~w~n', [Min3])
    ;   write('Дерево пустое~n')
    ).

% === ЧАСТЬ II: РАБОТА С ОРИЕНТИРОВАННЫМИ ГРАФАМИ ===

% Представление ориентированного графа
% edge(From, To) - ориентированное ребро

% Пример ориентированного графа
edge(a, b).
edge(a, c).
edge(b, d).
edge(b, e).
edge(c, f).
edge(d, e).
edge(e, f).
edge(f, a). % Цикл для тестирования

% Предикат для нахождения всех достижимых вершин из заданной
reachable_vertices(Start, Reachable) :-
    reachable(Start, [], ReachableList),
    sort(ReachableList, Reachable). % Убираем дубликаты

% Базовый случай: вершина уже посещена
reachable(Vertex, Visited, []) :-
    member(Vertex, Visited), !.

% Рекурсивный случай: находим все достижимые вершины
reachable(Vertex, Visited, [Vertex|Reachable]) :-
    findall(Next, edge(Vertex, Next), Neighbors),
    reachable_list(Neighbors, [Vertex|Visited], Reachable).

% Обработка списка соседей
reachable_list([], _, []).
reachable_list([Neighbor|Rest], Visited, AllReachable) :-
    reachable(Neighbor, Visited, ReachableFromNeighbor),
    reachable_list(Rest, Visited, ReachableFromRest),
    append(ReachableFromNeighbor, ReachableFromRest, AllReachable).

% Вывод всех рёбер графа
display_graph :-
    write('Ориентированный граф:'), nl,
    findall(_, (edge(X, Y), format('  ~w -> ~w~n', [X, Y])), _).

% Получить все вершины графа
all_vertices(Vertices) :-
    findall(Vertex, (edge(Vertex, _); edge(_, Vertex)), AllVertices),
    sort(AllVertices, Vertices).

% Тест для достижимых вершин
test_graph_reachable :-
    nl, write('=== ТЕСТ ДОСТИЖИМЫХ ВЕРШИН В ГРАФЕ ==='), nl, nl,
    display_graph,
    nl,
    write('Достижимые вершины из каждой вершины:'), nl,
    all_vertices(Vertices),
    member(Start, Vertices),
    reachable_vertices(Start, Reachable),
    format('Из вершины ~w достижимы: ~w~n', [Start, Reachable]),
    fail.
test_graph_reachable.

% Главный меню для тестирования
main :-
    nl,
    write('Лабораторная работа №6'), nl,
    write('======================'), nl,
    write('1 - Тест минимального элемента дерева'), nl,
    write('2 - Тест достижимых вершин в графе'), nl,
    write('3 - Все тесты'), nl,
    write('Выберите вариант: '),
    read(Choice),
    handle_choice(Choice).

handle_choice(1) :- test_tree_min.
handle_choice(2) :- test_graph_reachable.
handle_choice(3) :- test_tree_min, test_graph_reachable.
handle_choice(_) :- write('Неверный выбор').

% Запуск всех тестов
run_all_tests :-
    test_tree_min,
    test_graph_reachable.