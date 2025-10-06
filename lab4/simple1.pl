start :-
    write('Введите строку -> '),
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
    format('Читаем: ~w~n', [Line]),
    ( sub_string(Line, _, _, _, Substr)
    ->  format('Записываем: ~w~n', [Line]),
        writeln(Out, Line),
        flush_output(Out)
    ; true
    ),
    process_lines(Substr, In, Out).
