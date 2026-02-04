:- dynamic transport/3.
:- dynamic start/0.

load_db(File) :-
    (   exists_file(File)
    ->  writeln('Loading database...'),
        consult(File),
        writeln('Database loaded successfully')
    ;   writeln('No database file loaded.'),
        fail
    ).

save_db(File) :-
    tell(File),
    listing(transport/3),
    told,
    writeln('Database saved successfully').

menu(File) :-
    repeat,
    nl, write('--------------------------------'), nl,
    write('City Transport Database'), nl, nl,
    write('1 - View database'), nl,
    write('2 - Add entry'), nl,
    write('3 - Delete entry'), nl,
    write('4 - Find direct route'), nl,
    write('5 - Exit and save'), nl,
    write('--------------------------------'), nl,
    write('Choose menu option (1-5): '), flush_output,
    read_line_to_string(user_input, Input),
    atom_number(Input, Choice),
    handle_choice(Choice, File),
    Choice = 5, !.

handle_choice(1, _) :-
    writeln('Option 1 chosen: Show DB'),
    show_db, !.
handle_choice(2, _) :-
    writeln('Option 2 chosen: Add entry'),
    add_entries, !.
handle_choice(3, _) :-
    writeln('Option 3 chosen: Delete entry'),
    delete_entries, !.
handle_choice(4, _) :-
    writeln('Option 4 chosen: Find route'),
    route_query, !.
handle_choice(5, File) :-
    save_db(File),
    retractall(transport(_,_,_)),
    write('Data saved. Exiting program.'), nl, !.
handle_choice(_, _) :-
    write('Invalid choice. Try again.'), nl, fail.

show_db :-
    findall(_, transport(_, _, _), List),
    (   List == []
    ->  writeln('Database is empty.')
    ;   writeln('Showing database entries:'),
        transport(Name, Route, Stops),
        format('Transport: ~w, Route: ~w, Stops: ~w~n', [Name, Route, Stops]),
        fail
    ;   writeln('End of database.')
    ).

add_entries :-
    write('Adding entries. Type "stop" for transport name to finish.'), nl,
    add_loop.

valid_route_number(Route) :-
    integer(Route),
    Route > 0.

add_loop :-
    write('Transport name (or stop): '), flush_output,
    read_line_to_string(user_input, NameStr),
    string_trim(NameStr, TrimmedNameStr),
    atom_string(Name, TrimmedNameStr),
    (   Name == stop
    ->  writeln('Finished adding entries.')
    ;   write('Route number: '), flush_output,
        read_line_to_string(user_input, RouteStr),
        (   atom_number(RouteStr, Route),
            valid_route_number(Route)
        ->  true
        ;   writeln('Invalid route number. Please enter a natural number.'),
            add_loop
        ),
        write('List of stops (format: [stop1, stop2, ...] or "stop" to finish): '), flush_output,
        read_line_to_string(user_input, StopsStr),
        string_trim(StopsStr, TrimmedStopsStr),
        (   TrimmedStopsStr == "stop"
        ->  writeln('Finished adding entries.')  % Завершаем цикл, не вызывая add_loop повторно
        ;   (   catch(term_string(Stops, TrimmedStopsStr), _, fail),
                is_list(Stops)
            ->  assertz(transport(Name, Route, Stops)),
                format('Added: ~w, route ~w, stops: ~w~n', [Name, Route, Stops]),
                add_loop
            ;   writeln('Invalid stops format. Please use format: [stop1, stop2, ...]'),
                add_loop
            )
        )
    ).

delete_entries :-
    write('Deleting entries. Type "stop" for transport name to finish.'), nl,
    delete_loop.

delete_loop :-
    write('Transport name (or stop): '), flush_output,
    read_line_to_string(user_input, NameStr),
    string_trim(NameStr, TrimmedNameStr),
    atom_string(Name, TrimmedNameStr),
    (   Name == stop
    ->  writeln('Finished deleting entries.')
    ;   write('Route number: '), flush_output,
        read_line_to_string(user_input, RouteStr),
        (   atom_number(RouteStr, Route)
        ->  (   retract(transport(Name, Route, _))
            ->  format('Entry deleted: ~w, route ~w~n', [Name, Route])
            ;   writeln('Entry not found.')
            ),
            delete_loop
        ;   writeln('Invalid route number.'),
            delete_loop
        )
    ).

route_query :-
    write('Enter start stop: '), flush_output,
    read_line_to_string(user_input, StartStr),
    string_trim(StartStr, TrimmedStart),
    atom_string(Start, TrimmedStart),
    write('Enter end stop: '), flush_output,
    read_line_to_string(user_input, EndStr),
    string_trim(EndStr, TrimmedEnd),
    atom_string(End, TrimmedEnd),
    find_routes(Start, End).

find_routes(Start, End) :-
    findall([Name, Route], reachable(Start, End, Name, Route), Routes),
    (   Routes == []
    ->  format('No direct route found from ~w to ~w~n', [Start, End])
    ;   format('Direct routes from ~w to ~w with max 3 stops between:~n', [Start, End]),
        print_routes(Routes)
    ).

print_routes([]).
print_routes([[Name, Route]|Rest]) :-
    format('  - ~w, route number ~w~n', [Name, Route]),
    print_routes(Rest).

reachable(Start, End, Name, Route) :-
    transport(Name, Route, Stops),
    nth0(StartIndex, Stops, Start),
    nth0(EndIndex, Stops, End),
    Start \== End,
    Diff is abs(StartIndex - EndIndex),
    Diff =< 3.

string_trim(In, Out) :-
    string_chars(In, Chars),
    trim_leading(Chars, TrimmedFront),
    reverse(TrimmedFront, RevChars),
    trim_leading(RevChars, RevTrimmedBack),
    reverse(RevTrimmedBack, Trimmed),
    string_chars(Out, Trimmed).

trim_leading([], []).
trim_leading([H|T], Rest) :-
    char_type(H, space), !,
    trim_leading(T, Rest).
trim_leading(L, L).

start :-
    load_db('transport_db.pl'),
    menu('transport_db.pl').
