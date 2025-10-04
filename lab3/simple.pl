remove_duplicates([], []). 

remove_duplicates([H|T], Result) :-
    member(H, T),         
    remove_duplicates(T, Result).

remove_duplicates([H|T], [H|Result]) :-
    \+ member(H, T),      
    remove_duplicates(T, Result).  
