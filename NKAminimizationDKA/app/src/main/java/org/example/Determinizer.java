package org.example;

import java.util.*;

public class Determinizer {

    public static Automaton determinize(Automaton nfa) {

        System.out.println("\n========================================");
        System.out.println("       ЭТАП 1. ДЕТЕРМИНИЗАЦИЯ");
        System.out.println("========================================");

        List<String> originalStates = new ArrayList<>(nfa.getStates());
        List<String> alphabet = new ArrayList<>(nfa.getAlphabet());

        // 1. Генерируем все непустые подмножества исходных состояний.
        List<Set<String>> allSubsets = new ArrayList<>();

        for (int size = 1; size <= originalStates.size(); size++) {
            generateCombinations(
                    originalStates,
                    size,
                    0,
                    new LinkedHashSet<>(),
                    allSubsets
            );
        }

        // 2. Строим переходы для всех подмножеств.
        Map<Set<String>, Map<String, Set<String>>> subsetTransitions =
                new LinkedHashMap<>();

        for (Set<String> subset : allSubsets) {

            Map<String, Set<String>> transitionsBySymbol =
                    new LinkedHashMap<>();

            for (String symbol : alphabet) {

                Set<String> destinations = new LinkedHashSet<>();

                for (String state : subset) {
                    destinations.addAll(
                            nfa.getTransitions(state, symbol)
                    );
                }

                transitionsBySymbol.put(symbol, destinations);
            }

            subsetTransitions.put(subset, transitionsBySymbol);
        }

        // 3. Выводим таблицу всех возможных подмножеств.
        System.out.println("\nЭТАП 1.1. ВСЕ ВОЗМОЖНЫЕ НОВЫЕ СОСТОЯНИЯ");

        printSubsetTable(allSubsets, alphabet, subsetTransitions);

        // 4. Находим достижимые подмножества методом BFS.
        Set<String> initialSubset = new LinkedHashSet<>();
        initialSubset.add(nfa.getInitialState());

        Set<Set<String>> reachable = new LinkedHashSet<>();
        Queue<Set<String>> queue = new LinkedList<>();

        reachable.add(initialSubset);
        queue.add(initialSubset);

        while (!queue.isEmpty()) {

            Set<String> current = queue.poll();

            for (String symbol : alphabet) {

                Set<String> destination =
                        subsetTransitions.get(current).get(symbol);

                // Пустое множество не добавляем как состояние.
                if (destination.isEmpty()) {
                    continue;
                }

                Set<String> next = new LinkedHashSet<>(destination);

                if (reachable.add(next)) {
                    queue.add(next);
                }
            }
        }

        // 5. Формируем список достижимых подмножеств
        // в исходном порядке генерации.
        List<Set<String>> reachableSubsets = new ArrayList<>();

        for (Set<String> subset : allSubsets) {
            if (reachable.contains(subset)) {
                reachableSubsets.add(subset);
            }
        }

        System.out.println("\n========================================");
        System.out.println("ЭТАП 2. УДАЛЕНИЕ НЕДОСТИЖИМЫХ СОСТОЯНИЙ");
        System.out.println("========================================");

        System.out.println("\nДостижимые состояния:");

        for (Set<String> subset : reachableSubsets) {
            System.out.println(formatSubset(subset));
        }

        System.out.println("\nУдалённые недостижимые состояния:");

        boolean hasRemoved = false;

        for (Set<String> subset : allSubsets) {

            if (!reachable.contains(subset)) {
                System.out.println(formatSubset(subset));
                hasRemoved = true;
            }
        }

        if (!hasRemoved) {
            System.out.println("Нет");
        }

        // 6. Выводим таблицу достижимых состояний
        // до переименования.
        System.out.println("\nТаблица достижимых состояний до переименования:");

        printSubsetTable(
                reachableSubsets,
                alphabet,
                subsetTransitions
        );

        // 7. Присваиваем достижимым состояниям новые имена.
        Map<Set<String>, String> names = new LinkedHashMap<>();

        int index = 0;

        for (Set<String> subset : reachableSubsets) {
            names.put(subset, getStateName(index++));
        }

        // 8. Показываем соответствие старых и новых обозначений.
        System.out.println("\n========================================");
        System.out.println("ЭТАП 3. ПЕРЕИМЕНОВАНИЕ СОСТОЯНИЙ");
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

    // Генерация комбинаций заданного размера.
    private static void generateCombinations(
            List<String> states,
            int targetSize,
            int start,
            Set<String> current,
            List<Set<String>> result
    ) {

        if (current.size() == targetSize) {
            result.add(new LinkedHashSet<>(current));
            return;
        }

        for (int i = start; i < states.size(); i++) {

            current.add(states.get(i));

            generateCombinations(
                    states,
                    targetSize,
                    i + 1,
                    current,
                    result
            );

            current.remove(states.get(i));
        }
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
        return "{" + String.join("", subset) + "}";
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
