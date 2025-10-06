split_even_odd([], [], []).
split_even_odd([H|T], Odd, [H|Even]) :-
    0 is H mod 2,
    split_even_odd(T, Odd, Even).
split_even_odd([H|T], [H|Odd], Even) :-
    1 is abs(H) mod 2,
    split_even_odd(T, Odd, Even).
