insert(nil, X, tree(nil, X, nil)).
insert(tree(Left, Root, Right), X, tree(NewLeft, Root, Right)) :-
    X =< Root,
    insert(Left, X, NewLeft).
insert(tree(Left, Root, Right), X, tree(Left, Root, NewRight)) :-
    X > Root,
    insert(Right, X, NewRight).

read_tree(N, Tree) :-
    read_tree(N, nil, Tree).

read_tree(0, Tree, Tree) :- !.
read_tree(N, AccTree, Tree) :-
    N > 0,
    write('Введите значение узла: '),
    read(Value),
    insert(AccTree, Value, NewTree),
    N1 is N - 1,
    read_tree(N1, NewTree, Tree).

print_tree(Tree) :-
    print_tree(Tree, 0).

print_tree(nil, _).
print_tree(tree(Left, Root, Right), Indent) :-
    NewIndent is Indent + 4,
    print_tree(Right, NewIndent),
    format('~*|~w~n', [Indent, Root]),
    print_tree(Left, NewIndent).

min_tree(nil, _) :- !, fail.
min_tree(tree(nil, Root, nil), Root) :- !.
min_tree(tree(Left, Root, nil), Min) :-
    min_tree(Left, MinLeft),
    Min is min(Root, MinLeft), !.
min_tree(tree(nil, Root, Right), Min) :-
    min_tree(Right, MinRight),
    Min is min(Root, MinRight), !.
min_tree(tree(Left, Root, Right), Min) :-
    min_tree(Left, MinLeft),
    min_tree(Right, MinRight),
    TempMin is min(MinLeft, MinRight),
    Min is min(Root, TempMin), !.

run :-
    write('Введите количество узлов: '),
    read(N),
    (integer(N), N >= 0 ->
        read_tree(N, Tree),
        (Tree = nil ->
            writeln('Дерево пустое')
        ;
            writeln('Дерево:'),
            print_tree(Tree),
            (min_tree(Tree, Min) ->
                format('Минимальный элемент дерева: ~w~n', [Min])
            ;
                writeln('Минимальный элемент не найден')
            )
        )
    ;
        writeln('Ошибка: введите неотрицательное целое число')
    ).
