% Пример дерева (можно заменить на nil для пустого)
example_tree(
    tree(
        tree(nil, 3, nil),
        5,
        tree(
            tree(nil, 4, nil),
            7,
            tree(nil, 9, nil)
        )
    )
).

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
    example_tree(T),
    writeln('Дерево:'),
    ( T = nil ->
        writeln('Дерево пустое')
    ;
        print_tree(T),
        ( min_tree(T, Min) ->
            format('Минимальный элемент дерева: ~w', [Min])
        ;
            writeln('Не удалось найти минимальный элемент')
        )
    ).