remove_duplicates([], []).
remove_duplicates([H|T], [H|Result]) :-
    delete(T, H, T1),
remove_duplicates(T1, Result).
