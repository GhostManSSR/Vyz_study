example2 :-
    open('input2.txt', read, In),
    read_all_lines(In, Lines),
    close(In),
    atomic_list_concat(Lines, '', Text),
    remove_spaces(Text, TextNoSpaces),
    open('output2.txt', write, Out),
    write_in_chunks(TextNoSpaces, 20, Out),
    close(Out),
    !.

read_all_lines(In, []) :-
    at_end_of_stream(In),
    !.
read_all_lines(In, [Line|Rest]) :-
    \+ at_end_of_stream(In),
    read_line_to_string(In, Line),
    read_all_lines(In, Rest).

remove_spaces(Text, Result) :-
    string_chars(Text, Chars),
    exclude(=(' '), Chars, Filtered),
    string_chars(Result, Filtered).

write_in_chunks(Text, N, Out) :-
    string_length(Text, Len),
    ( Len =:= 0 ->
        true
    ; Len < N ->
        writeln(Out, Text)
    ;
        sub_string(Text, 0, N, RestLen, Chunk),
        writeln(Out, Chunk),
        sub_string(Text, N, RestLen, 0, Rest),
        write_in_chunks(Rest, N, Out)
    ), !.


