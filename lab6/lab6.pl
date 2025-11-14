% Лабораторная работа №6
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
    write('Binary Tree:'), nl,
    print_tree(Tree, 0).

% Тест для минимального элемента
test_tree_min :-
    nl, write('=== TREE MINIMUM ELEMENT TEST ==='), nl, nl,
    tree1(Tree1),
    write('Tree 1:'), nl,
    display_tree(Tree1),
    (   min_tree(Tree1, Min1)
    ->  format('Minimum element: ~w~n', [Min1])
    ;   write('Tree is empty~n')
    ),
    nl,
    
    tree2(Tree2),
    write('Tree 2:'), nl,
    display_tree(Tree2),
    (   min_tree(Tree2, Min2)
    ->  format('Minimum element: ~w~n', [Min2])
    ;   write('Tree is empty~n')
    ),
    nl,
    
    tree3(Tree3),
    write('Tree 3:'), nl,
    display_tree(Tree3),
    (   min_tree(Tree3, Min3)
    ->  format('Minimum element: ~w~n', [Min3])
    ;   write('Tree is empty~n')
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
    write('Directed Graph:'), nl,
    findall(_, (edge(X, Y), format('  ~w -> ~w~n', [X, Y])), _).

% Получить все вершины графа
all_vertices(Vertices) :-
    findall(Vertex, (edge(Vertex, _); edge(_, Vertex)), AllVertices),
    sort(AllVertices, Vertices).

% Тест для достижимых вершин
test_graph_reachable :-
    nl, write('=== REACHABLE VERTICES TEST ==='), nl, nl,
    display_graph,
    nl,
    write('Reachable vertices from each vertex:'), nl,
    all_vertices(Vertices),
    member(Start, Vertices),
    reachable_vertices(Start, Reachable),
    format('From vertex ~w reachable: ~w~n', [Start, Reachable]),
    fail.
test_graph_reachable.

% Главный меню для тестирования
main :-
    nl,
    write('Laboratory Work №6'), nl,
    write('======================'), nl,
    write('1 - Test tree minimum element'), nl,
    write('2 - Test graph reachable vertices'), nl,
    write('3 - All tests'), nl,
    write('Choose option: '),
    read(Choice),
    handle_choice(Choice).

handle_choice(1) :- test_tree_min.
handle_choice(2) :- test_graph_reachable.
handle_choice(3) :- test_tree_min, test_graph_reachable.
handle_choice(_) :- write('Invalid choice').

% Запуск всех тестов
run_all_tests :-
    test_tree_min,
    test_graph_reachable.





  