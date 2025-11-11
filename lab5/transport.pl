% Declare dynamic predicates
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

% Save database to file
save_db(File) :-
    tell(File),
    listing(transport/3),
    told,
    writeln('Database saved successfully').

% Main menu
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

add_loop :-
    write('Transport name (or stop): '), flush_output,
    read_line_to_string(user_input, NameStr),
    atom_string(Name, NameStr),
    (   Name == stop
    ->  writeln('Finished adding entries.')
    ;   write('Route number: '), flush_output,
        read_line_to_string(user_input, RouteStr),
        (   atom_number(RouteStr, Route)
        ->  true
        ;   writeln('Invalid route number. Please enter a number.'),
            add_loop
        ),
        write('List of stops (format: [stop1, stop2, ...]): '), flush_output,
        read_line_to_string(user_input, StopsStr),
        (   catch(term_string(Stops, StopsStr), _, fail),
            is_list(Stops)
        ->  assertz(transport(Name, Route, Stops)),
            format('Added: ~w, route ~w, stops: ~w~n', [Name, Route, Stops]),
            add_loop
        ;   writeln('Invalid stops format. Please use format: [stop1, stop2, ...]'),
            add_loop
        )
    ).

delete_entries :-
    write('Deleting entries. Type "stop" for transport name to finish.'), nl,
    delete_loop.

delete_loop :-
    write('Transport name (or stop): '), flush_output,
    read_line_to_string(user_input, NameStr),
    atom_string(Name, NameStr),
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
    atom_string(Start, StartStr),
    write('Enter end stop: '), flush_output,
    read_line_to_string(user_input, EndStr),
    atom_string(End, EndStr),
    (   reachable(Start, End, Name, Route)
    ->  format('Direct route found: ~w, route number ~w~n', [Name, Route])
    ;   write('No direct route without transfers found.'), nl
    ).

reachable(Start, End, Name, Route) :-
    transport(Name, Route, Stops),
    member(Start, Stops),
    member(End, Stops).

start :-
    load_db('transport_db.pl'),
    menu('transport_db.pl').