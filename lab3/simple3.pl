% Предикат для произведения элементов списка
product_list([], 1).                % произведение пустого списка равно 1
product_list([H|T], Product) :-
    product_list(T, TailProduct),
    Product is H * TailProduct.

% Основной предикат для списка списков
list_product([], []).
list_product([SubList|Tail], [Prod|Rest]) :-
    product_list(SubList, Prod),
    list_product(Tail, Rest).
