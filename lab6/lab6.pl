min_tree(nil, _) :- fail.
min_tree(tree(nil, nil, nil), _) :- !, fail.

min_tree(tree(Value, nil, nil), Value) :- !.
min_tree(tree(Value, Left, nil), Min) :-
    min_tree(Left, LeftMin),
    Min is min(Value, LeftMin), !.
min_tree(tree(Value, nil, Right), Min) :-
    min_tree(Right, RightMin),
    Min is min(Value, RightMin), !.
min_tree(tree(Value, Left, Right), Min) :-
    min_tree(Left, LeftMin),
    min_tree(Right, RightMin),
    Min1 is min(Value, LeftMin),
    Min is min(Min1, RightMin).


tree1(tree(1, 
          tree(2, 
               tree(3, nil, nil), 
               tree(-4, nil, nil)), 
          tree(5, 
               tree(4, tree(2, nil, nil), nil), 
               tree(3, nil, tree(7,nil,nil))))).

tree2(tree(nil, nil, nil)).

tree3(tree(5, 
          tree(7, 
               tree(3, nil, nil), 
               tree(1, nil, tree(8,nil,nil))), 
          tree(2, 
               tree(-1, nil, nil), 
               tree(5, nil, tree(4,nil,nil))))).

print_tree(nil, _).
print_tree(tree(Value, Left, Right), Level) :-
    NewLevel is Level + 4,
    print_tree(Right, NewLevel),
    tab(Level),
    write(Value), nl,
    print_tree(Left, NewLevel).

display_tree(Tree) :-
    write('Binary Tree:'), nl,
    print_tree(Tree, 0).

test_tree_min :-
    nl, write('=== TREE MINIMUM ELEMENT TEST ==='), nl, nl,
    tree1(Tree1),
    write('Tree 1:'), nl,
    display_tree(Tree1),
    (   min_tree(Tree1, Min1)
    ->  format('Minimum element: ~w~n', [Min1])
    ;   write('Tree is empty~n')
    ),
    nl,
    
    tree2(Tree2),
    write('Tree 2:'), nl,
    display_tree(Tree2),
    (   min_tree(Tree2, Min2)
    ->  format('Minimum element: ~w~n', [Min2])
    ;   write('Tree is empty~n')
    ),
    nl,
    
    tree3(Tree3),
    write('Tree 3:'), nl,
    display_tree(Tree3),
    (   min_tree(Tree3, Min3)
    ->  format('Minimum element: ~w~n', [Min3])
    ;   write('Tree is empty~n')
    ).

% Пример первого графа
edge1(2, 3).
edge1(2, 7).
edge1(2, 4).
edge1(7, 1).
edge1(4, 5).
edge1(5, 1).
edge1(5, 6).
edge1(6, 1).
edge1(1, 2).

% Пример второго графа
edge2(1, 2).
edge2(1, 5).
edge2(2, 3).
edge2(3, 5).
edge2(6, 5).
edge2(6, 4).
edge2(4, 3).
edge2(7, 4).
edge2(7, 1).

reachable_vertices1(Start, Reachable) :-
    reachable1(Start, [], ReachableList),
    delete(ReachableList, Start, ReachableUnsorted),
    remove_duplicates(ReachableList, Reachable).

reachable1(Vertex, Visited, []) :-
    member(Vertex, Visited), !.

reachable1(Vertex, Visited, [Vertex|Reachable]) :-
    findall(Next, edge1(Vertex, Next), Neighbors),
    reachable_list1(Neighbors, [Vertex|Visited], Reachable).

reachable_list1([], _, []).
reachable_list1([Neighbor|Rest], Visited, AllReachable) :-
    reachable1(Neighbor, Visited, ReachableFromNeighbor),
    reachable_list1(Rest, Visited, ReachableFromRest),
    append(ReachableFromNeighbor, ReachableFromRest, AllReachable).

reachable_vertices2(Start, Reachable) :-
    reachable2(Start, [], ReachableList),
    delete(ReachableList, Start, ReachableUnsorted),
    remove_duplicates(ReachableList, Reachable).

reachable2(Vertex, Visited, []) :-
    member(Vertex, Visited), !.

reachable2(Vertex, Visited, [Vertex|Reachable]) :-
    findall(Next, edge2(Vertex, Next), Neighbors),
    reachable_list2(Neighbors, [Vertex|Visited], Reachable).

reachable_list2([], _, []).
reachable_list2([Neighbor|Rest], Visited, AllReachable) :-
    reachable2(Neighbor, Visited, ReachableFromNeighbor),
    reachable_list2(Rest, Visited, ReachableFromRest),
    append(ReachableFromNeighbor, ReachableFromRest, AllReachable).

remove_duplicates(List, Result) :-
    remove_duplicates_helper(List, [], Result).

remove_duplicates_helper([], _, []).
remove_duplicates_helper([H|T], Seen, Result) :-
    member(H, Seen),
    !,
    remove_duplicates_helper(T, Seen, Result).
remove_duplicates_helper([H|T], Seen, [H|Result]) :-
    remove_duplicates_helper(T, [H|Seen], Result).

test_graphs :-
    write('Graph 1:'), nl,
    forall(
        reachable_vertices1(Start, Reachable),
        format('From ~w reachable: ~w~n', [Start, Reachable])
    ),
    nl,
    write('Graph 2:'), nl,
    forall(
        reachable_vertices2(Start, Reachable2),
        ormat('From ~w reachable: ~w~n', [Start, Reachable2])
    ).


all_vertices1(Vertices) :-
    findall(V, (edge1(V, _); edge1(_, V)), All),
    sort(All, Vertices).

all_vertices2(Vertices) :-
    findall(V, (edge2(V, _); edge2(_, V)), All),
    sort(All, Vertices).

test_reachable_for_graph1 :-
    all_vertices1(Vertices),
    forall(
        member(Start, Vertices),
        (
            reachable_vertices1(Start, Reachable),
            format('From ~w reachable vertices: ~w~n', [Start, Reachable])
        )
    ).

test_reachable_for_graph2 :-
    all_vertices2(Vertices),
    forall(
        member(Start, Vertices),
        (
            reachable_vertices2(Start, Reachable),
            format('From ~w reachable vertices: ~w~n', [Start, Reachable])
        )
    ).






  