start :-
    write('Input string -> '),
    read_line_to_string(user_input, Substring),
    open('input1.txt', read, In),
    open('output1.txt', write, Out),
    process_lines(Substring, In, Out),
    close(In),
    close(Out).

process_lines(Substr, In, Out) :-
    at_end_of_stream(In),
    !.
process_lines(Substr, In, Out) :-
    \+ at_end_of_stream(In),
    read_line_to_string(In, Line),
    ( Substr \= "", sub_string(Line, _, _, _, Substr) ->
        format('Read: ~w~nWriting: ~w~n', [Line, Line]),
        writeln(Out, Line),
        flush_output(Out)
    ; format('Read: ~w~n', [Line])
    ),
    process_lines(Substr, In, Out).
