split_even_odd([], [], []).
split_even_odd([H|T], [H|Even], Odd) :-
    0 is H mod 2,
    split_even_odd(T, Even, Odd).
split_even_odd([H|T], Even, [H|Odd]) :-
    1 is abs(H) mod 2,
    split_even_odd(T, Even, Odd).
