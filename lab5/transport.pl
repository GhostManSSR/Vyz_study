% Declare dynamic predicates
:- dynamic transport/3.
:- dynamic start/0.

load_db(File) :-
    exists_file(File),
    writeln('Loading database...'),
    consult(File), !.
load_db(_) :- writeln('No database file loaded.').

% Save database to file
save_db(File) :-
    tell(File),
    listing(transport/3),
    told.

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
    read(Choice),
    handle_choice(Choice, File),
    Choice = 5, !.

handle_choice(1, _) :-
    writeln('Option 1 chosen: Show DB'),
    show_db, !.
handle_choice(2, _) :-
    add_entries, !.
handle_choice(3, _) :-
    delete_entries, !.
handle_choice(4, _) :-
    route_query, !.
handle_choice(5, File) :-
    save_db(File),
    retractall(transport(_,_,_)),
    write('Data saved. Exiting program.'), nl, !.
handle_choice(_, _) :-
    write('Invalid choice. Try again.'), nl, fail.

show_db :-
    writeln('Showing database entries:'),
    ( transport(Name, Route, Stops),
      format('Transport: ~w, Route: ~w, Stops: ~w~n', [Name, Route, Stops]),
      fail
    ; writeln('End of database.'), true ).


add_entries :-
    write('Adding entries. Type stop for transport name to finish.'), nl,
    add_loop.

add_loop :-
    write('Transport name (or stop): '), flush_output,
    read(Name),
    ( Name == stop -> true
    ; write('Route number: '), flush_output,
      read(Route),
      write('List of stops (format: [stop1, stop2, ...]): '), flush_output,
      read(Stops),
      assertz(transport(Name, Route, Stops)),
      add_loop
    ).

delete_entries :-
    write('Deleting entries. Type stop for transport name to finish.'), nl,
    delete_loop.

delete_loop :-
    write('Transport name (or stop): '), flush_output,
    read(Name),
    ( Name == stop -> true
    ; write('Route number: '), flush_output,
      read(Route),
      ( retract(transport(Name, Route, _)) ->
          write('Entry deleted.'), nl
      ; write('Entry not found.'), nl ),
      delete_loop
    ).

route_query :-
    write('Enter start stop: '), flush_output,
    read(Start),
    write('Enter end stop: '), flush_output,
    read(End),
    ( reachable(Start, End, Name, Route) ->
        format('You can travel by ~w, route number ~w~n', [Name, Route])
    ; write('No direct route without transfers found.'), nl ).

reachable(Start, End, Name, Route) :-
    transport(Name, Route, Stops),
    member(Start, Stops),
    member(End, Stops).

start :-
    load_db('transport_db.pl'),
    menu('transport_db.pl').
