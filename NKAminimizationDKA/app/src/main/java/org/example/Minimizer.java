package org.example;

import java.util.*;

public class Minimizer {

    public static Automaton minimize(Automaton dfa) {

        System.out.println("\n=== МИНИМИЗАЦИЯ ДКА ===");

        if (!dfa.isDeterministic()) {
            throw new IllegalArgumentException(
                    "Минимизация невозможна: автомат не является ДКА"
            );
        }

        // 1. Удаление недостижимых состояний.
        Set<String> reachable = findReachableStates(dfa);

        System.out.println("\nДостижимые состояния: " + formatSubset(reachable));

        Set<String> unreachable =
                new LinkedHashSet<>(dfa.getStates());

        unreachable.removeAll(reachable);

        System.out.println(
                "Недостижимые состояния: " + formatSubset(unreachable)
        );

        // 2. Создание R(0).
        List<Set<String>> partitions = new ArrayList<>();

        Set<String> finalStates = new LinkedHashSet<>(
                dfa.getFinalStates()
        );

        finalStates.retainAll(reachable);

        Set<String> nonFinalStates = new LinkedHashSet<>(reachable);

        nonFinalStates.removeAll(finalStates);

        if (!finalStates.isEmpty()) {
            partitions.add(finalStates);
        }

        if (!nonFinalStates.isEmpty()) {
            partitions.add(nonFinalStates);
        }

        printPartitions(0, partitions);

        // 3. Построение R(n).
        int iteration = 0;

        while (true) {

            iteration++;

            List<Set<String>> newPartitions = new ArrayList<>();

            Map<String, Integer> stateClasses =
                    createStateClasses(partitions);

            for (Set<String> partition : partitions) {

                Map<List<Integer>, Set<String>> groups =
                        new LinkedHashMap<>();

                for (String state : partition) {

                    List<Integer> signature =
                            getSignature(
                                    dfa,
                                    state,
                                    stateClasses
                            );

                    groups.computeIfAbsent(
                            signature,
                            k -> new LinkedHashSet<>()
                    ).add(state);
                }

                newPartitions.addAll(groups.values());
            }

            System.out.println();

            printPartitions(iteration, newPartitions);

            // Если разбиение не изменилось, алгоритм завершен.
            if (partitions.equals(newPartitions)) {

                System.out.println(
                        "\nРазбиение стабилизировалось."
                );

                break;
            }

            partitions = newPartitions;
        }

        // 4. Построение минимизированного ДКА.
        return buildMinimalDfa(dfa, partitions, reachable);
    }

    /**
     * Поиск достижимых состояний.
     */
    private static Set<String> findReachableStates(
            Automaton dfa
    ) {

        Set<String> visited = new LinkedHashSet<>();

        Queue<String> queue = new ArrayDeque<>();

        queue.add(dfa.getInitialState());

        visited.add(dfa.getInitialState());

        while (!queue.isEmpty()) {

            String current = queue.poll();

            for (String symbol : dfa.getAlphabet()) {

                for (String next :
                        dfa.getTransitions(current, symbol)) {

                    if (visited.add(next)) {
                        queue.add(next);
                    }
                }
            }
        }

        return visited;
    }

    /**
     * Каждому состоянию присваивается номер класса.
     */
    private static Map<String, Integer> createStateClasses(
            List<Set<String>> partitions
    ) {

        Map<String, Integer> result = new HashMap<>();

        for (int i = 0; i < partitions.size(); i++) {

            for (String state : partitions.get(i)) {

                result.put(state, i);
            }
        }

        return result;
    }

    /**
     * Получение сигнатуры состояния.
     */
    private static List<Integer> getSignature(
            Automaton dfa,
            String state,
            Map<String, Integer> stateClasses
    ) {

        List<Integer> signature = new ArrayList<>();

        for (String symbol : dfa.getAlphabet()) {

            Set<String> destinations =
                    dfa.getTransitions(state, symbol);

            if (destinations.isEmpty()) {
                signature.add(-1);
            } else {

                String destination =
                        destinations.iterator().next();

                signature.add(
                        stateClasses.get(destination)
                );
            }
        }

        return signature;
    }

    /**
     * Построение итогового минимального автомата.
     */
    private static Automaton buildMinimalDfa(
            Automaton dfa,
            List<Set<String>> partitions,
            Set<String> reachable
    ) {

        Map<String, String> stateToClass =
                new LinkedHashMap<>();

        Set<String> minimalStates = new LinkedHashSet<>();

        Set<String> minimalFinalStates = new LinkedHashSet<>();

        Map<String, Map<String, Set<String>>> minimalTransitions =
                new LinkedHashMap<>();

        // Каждому классу присваиваем новое имя.
        int counter = 0;

        System.out.println("\nСоответствие состояний после минимизации:");

        for (Set<String> partition : partitions) {

            String className = "M" + counter++;

            minimalStates.add(className);

            for (String state : partition) {
                stateToClass.put(state, className);
            }

            if (!Collections.disjoint(
                    partition,
                    dfa.getFinalStates()
            )) {
                minimalFinalStates.add(className);
            }

            System.out.println(
                    className + " = " + formatSubset(partition)
            );
        }

        // Определяем новое начальное состояние.
        String minimalInitialState =
                stateToClass.get(dfa.getInitialState());

        // Строим переходы между классами.
        for (Set<String> partition : partitions) {

            String representative = partition.iterator().next();

            String currentClass = stateToClass.get(representative);

            Map<String, Set<String>> transitions =
                    new LinkedHashMap<>();

            for (String symbol : dfa.getAlphabet()) {

                Set<String> destinations =
                        dfa.getTransitions(representative, symbol);

                Set<String> newDestination = new LinkedHashSet<>();

                if (!destinations.isEmpty()) {

                    String destination =
                            destinations.iterator().next();

                    newDestination.add(
                            stateToClass.get(destination)
                    );
                }

                transitions.put(symbol, newDestination);
            }

            minimalTransitions.put(currentClass, transitions);
        }

        System.out.println("\n=== ИТОГОВЫЙ АВТОМАТ ===");

        System.out.println(
                "Количество состояний до минимизации: "
                        + reachable.size()
        );

        System.out.println(
                "Количество состояний после минимизации: "
                        + minimalStates.size()
        );

        return new Automaton(
                minimalStates,
                dfa.getAlphabet(),
                minimalInitialState,
                minimalFinalStates,
                minimalTransitions
        );
    }

    private static String formatSubset(Set<String> subset) {
        if(subset.size() > 1) return "{" + String.join(",", subset) + "}";
        return "{" + String.join("", subset) + "}";
    }

    /**
     * Вывод классов эквивалентности.
     */
    private static void printPartitions(
            int iteration,
            List<Set<String>> partitions
    ) {

        String result = partitions.stream()
                .map(Minimizer::formatSubset)
                .collect(java.util.stream.Collectors.joining(", "));

        System.out.println("R(" + iteration + "): {" + result + "}");
    }
}