# :- use_module(library(pce)).

# % --- Граф: ориентированные рёбра ---
# edge(a, b).
# edge(a, c).
# edge(b, d).
# edge(c, d).
# edge(d, e).
# edge(e, f).

# % --- Обход достижимых вершин ---
# reachable(Start, ReachableSorted) :-
#     reachable(Start, [], ReachableUnsorted),
#     sort(ReachableUnsorted, ReachableSorted).

# reachable(Node, Visited, Visited) :- member(Node, Visited), !.
# reachable(Node, Visited, Result) :-
#     \+ member(Node, Visited),
#     findall(Next, edge(Node, Next), NextNodes),
#     reachable_list(NextNodes, [Node|Visited], Result).

# reachable_list([], Visited, Visited).
# reachable_list([H|T], Visited, Result) :-
#     reachable(H, Visited, NewVisited),
#     reachable_list(T, NewVisited, Result).

# % --- Рисование графа XPCE ---
# draw_graph(Vertices) :-
#     new(Window, picture('Граф')),
#     send(Window, size, size(400,400)),
#     send(Window, open),
#     layout_vertices(Vertices, Vertices, Window, 200, 200, 120),
#     draw_edges(Window, Vertices).

# % Разметить вершины по кругу
# layout_vertices([], _, _, _, _, _).
# layout_vertices([V|Vs], AllVs, Window, CX, CY, Radius) :-
#     length(AllVs, N),
#     nth1(Index, AllVs, V),
#     Angle is 2 * pi * Index / N,
#     X is round(CX + Radius * cos(Angle)),
#     Y is round(CY + Radius * sin(Angle)),
#     new(B, ellipse(32,32)),                            % кружок
#     send(B, fill_pattern, colour(grey)),
#     send(Window, display, B, point(X,Y)),
#     new(Text, text(V)),                                % подпись
#     send(Window, display, Text, point(X+8, Y+8)),
#     send(B, name, V),                                  % имя для get
#     layout_vertices(Vs, AllVs, Window, CX, CY, Radius).

# % Нарисовать линии-стрелки для рёбер (только между достижимыми вершинами)
# draw_edges(Window, Vertices) :-
#     forall((edge(From, To), member(From, Vertices), member(To, Vertices)),
#         ( get(Window, member, From, FObj),
#           get(Window, member, To, TObj),
#           get(FObj, center, FPt),
#           get(TObj, center, TPt),
#           send(Window, display, new(L, line(FPt?x, FPt?y, TPt?x, TPt?y)),
#                 point(0,0))
#         )
#     ).

# % --- Запуск всего ---
# run_graph(Start) :-
#     reachable(Start, Vertices),
#     writeln('Достижимые вершины из начальной:'),
#     writeln(Vertices),
#     draw_graph(Vertices).

#?- run_graph(a).

% Пример графа (ориентированный невзвешенный)
edge(a, b).
edge(a, c).
edge(b, d).
edge(c, d).
edge(d, e).
edge(e, f).

% Основной предикат: reachable(+Start, -ReachableList)
% Находит список всех вершин, достижимых из Start, включая Start

reachable(Start, Reachable) :-
    reachable(Start, [], ReachableUnsorted),
    sort(ReachableUnsorted, Reachable).  % Удаляем дубликаты и сортируем

% Вспомогательный предикат с накоплением посещённых вершин
reachable(Current, Visited, Visited) :-
    member(Current, Visited), !.  % Уже посещали - прекращаем обход

reachable(Current, Visited, Result) :-
    \+ member(Current, Visited),
    findall(Next, edge(Current, Next), NextNodes),
    reachable_list(NextNodes, [Current|Visited], Result).

% Обработка списка соседних вершин
reachable_list([], Visited, Visited).
reachable_list([H|T], Visited, Result) :-
    reachable(H, Visited, NewVisited),
    reachable_list(T, NewVisited, Result).

# ?- reachable(a, Reachable).
# Reachable = [a, b, c, d, e, f].