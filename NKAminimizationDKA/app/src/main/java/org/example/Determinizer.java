package org.example;

import java.util.*;

public class Determinizer {

    public static Automaton determinize(Automaton nfa) {

        System.out.println("\n========================================");
        System.out.println("       ЭТАП 1. ДЕТЕРМИНИЗАЦИЯ");
        System.out.println("========================================");

        List<String> alphabet = new ArrayList<>(nfa.getAlphabet());
        Set<String> initialSubset = Set.of(nfa.getInitialState());
        Set<Set<String>> reachable = new LinkedHashSet<>();
        Queue<Set<String>> queue = new ArrayDeque<>();
        Map<Set<String>, Map<String, Set<String>>> subsetTransitions = new LinkedHashMap<>();
        reachable.add(initialSubset);
        queue.add(initialSubset);
        while (!queue.isEmpty()) {
            Set<String> current = queue.remove();
            Map<String, Set<String>> row = new LinkedHashMap<>();
            for (String symbol : alphabet) {
                Set<String> destination = new LinkedHashSet<>();
                for (String state : current) {
                    destination.addAll(nfa.getTransitions(state, symbol));
                }
                row.put(symbol, destination);
                // Пустое множество означает отсутствующий переход в неполном ДКА.
                if (!destination.isEmpty() && reachable.add(destination)) {
                    queue.add(destination);
                }
            }
            subsetTransitions.put(current, row);
        }
        List<Set<String>> reachableSubsets = new ArrayList<>(reachable);
        System.out.println("\nДостижимые подмножества (BFS); недостижимые не создаются:");
        printSubsetTable(reachableSubsets, alphabet, subsetTransitions);

        // 7. Присваиваем достижимым состояниям новые имена.
        Map<Set<String>, String> names = new LinkedHashMap<>();

        int index = 0;

        for (Set<String> subset : reachableSubsets) {
            names.put(subset, getStateName(index++));
        }

        // 8. Показываем соответствие старых и новых обозначений.
        System.out.println("\n========================================");
        System.out.println("ЭТАП 2. ПЕРЕИМЕНОВАНИЕ СОСТОЯНИЙ");
        System.out.println("========================================");

        System.out.printf(
                "%-25s %-20s%n",
                "Исходное состояние",
                "Новое обозначение"
        );

        for (Map.Entry<Set<String>, String> entry : names.entrySet()) {

            System.out.printf(
                    "%-25s %-20s%n",
                    formatSubset(entry.getKey()),
                    entry.getValue()
            );
        }

        // 9. Формируем итоговый детерминированный автомат.
        Set<String> states = new LinkedHashSet<>();
        Set<String> finalStates = new LinkedHashSet<>();

        Map<String, Map<String, Set<String>>> transitions =
                new LinkedHashMap<>();

        for (Map.Entry<Set<String>, String> entry : names.entrySet()) {

            Set<String> subset = entry.getKey();
            String stateName = entry.getValue();

            states.add(stateName);

            // Состояние ДКА конечное, если подмножество
            // содержит хотя бы одно конечное состояние НКА.
            for (String state : subset) {

                if (nfa.getFinalStates().contains(state)) {
                    finalStates.add(stateName);
                    break;
                }
            }

            Map<String, Set<String>> transitionsBySymbol =
                    new LinkedHashMap<>();

            for (String symbol : alphabet) {

                Set<String> destination =
                        subsetTransitions.get(subset).get(symbol);

                Set<String> namedDestination = new LinkedHashSet<>();

                if (!destination.isEmpty()) {

                    String destinationName = names.get(destination);

                    if (destinationName != null) {
                        namedDestination.add(destinationName);
                    }
                }

                transitionsBySymbol.put(symbol, namedDestination);
            }

            transitions.put(stateName, transitionsBySymbol);
        }

        String initialState = names.get(initialSubset);

        Automaton dfa = new Automaton(
                states,
                new LinkedHashSet<>(alphabet),
                initialState,
                finalStates,
                transitions
        );

        // 10. Выводим итоговый ДКА.
        System.out.println("\n========================================");
        System.out.println("       ИТОГОВЫЙ ДЕТЕРМИНИРОВАННЫЙ ДКА");
        System.out.println("========================================");

        dfa.print();

        return dfa;
    }

    // Печать промежуточной таблицы.
    private static void printSubsetTable(
            List<Set<String>> subsets,
            List<String> alphabet,
            Map<Set<String>, Map<String, Set<String>>> transitions
    ) {

        System.out.printf("%-15s", "Состояние");

        for (String symbol : alphabet) {
            System.out.printf("%-15s", symbol);
        }

        System.out.println();

        for (Set<String> subset : subsets) {

            System.out.printf("%-15s", formatSubset(subset));

            for (String symbol : alphabet) {

                Set<String> destination =
                        transitions.get(subset).get(symbol);

                String value = destination.isEmpty()
                        ? "—"
                        : formatSubset(destination);

                System.out.printf("%-15s", value);
            }

            System.out.println();
        }
    }

    private static String formatSubset(Set<String> subset) {
        return "{" + String.join(", ", subset) + "}";
    }

    private static String getStateName(int index) {

        StringBuilder name = new StringBuilder();

        do {
            name.insert(0, (char) ('A' + index % 26));
            index = index / 26 - 1;
        } while (index >= 0);

        return name.toString();
    }
}
