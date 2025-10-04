product_list([], 1).                
product_list([H|T], Product) :-
    product_list(T, TailProduct),
    Product is H * TailProduct.

list_product([], []).
list_product([SubList|Tail], Result) :-
    list_product(Tail, Rest),
    product_list(SubList, Prod),
    append(Rest, [Prod], Result).

