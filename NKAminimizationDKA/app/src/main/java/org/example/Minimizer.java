package org.example;

import java.util.*;
import java.util.stream.Collectors;

public class Minimizer {

    public static Automaton minimize(Automaton dfa) {

        System.out.println("\n=== МИНИМИЗАЦИЯ ДКА ===");

        if (!dfa.isDeterministic()) {
            throw new IllegalArgumentException(
                    "Минимизация невозможна: автомат не является ДКА"
            );
        }

        printAutomaton(dfa);

        // Достраиваем автомат до полного ДКА.
        dfa = complete(dfa);

        if (dfa.getStates().contains("__dead__")) {
            System.out.println(
                    "\nАвтомат дополнен до полного: добавлено состояние при котором нет перехода __dead__."
            );

            printAutomaton(dfa);
        }

        // Шаг 1. Исключаем недостижимые состояния.
        Set<String> reachable = findReachableStates(dfa);

        Set<String> unreachable =
                new LinkedHashSet<>(dfa.getStates());

        unreachable.removeAll(reachable);

        System.out.println(
                "\nДостижимые состояния: "
                        + formatSubset(reachable)
        );

        System.out.println(
                "Недостижимые состояния: "
                        + formatSubset(unreachable)
        );

        // Шаг 2. R(0) = {F, Q \ F}.
        List<Set<String>> r0 =
                createInitialPartitions(dfa, reachable);

        System.out.println();
        printPartitions(0, r0);

        List<Set<String>> previous = r0;
        int n = 0;

        // Шаг 3. Строим R(n), пока R(n) != R(n - 1).
        while (true) {

            n++;

            List<Set<String>> current =
                    buildNextPartition(dfa, previous);

            System.out.println();
            printPartitions(n, current);

            if (current.equals(previous)) {
                System.out.println(
                        "\nR(" + n + ") = R(" + (n - 1)
                                + "), разбиение стабилизировалось."
                );

                previous = current;
                break;
            }

            previous = current;
        }

        // Шаги 4-5. Строим минимальный автомат по классам R(n).
        return buildMinimalDfa(dfa, previous, reachable);
    }

    /**
     * Достраивает неполный ДКА до полного.
     */
    private static Automaton complete(Automaton dfa) {

        if (dfa.isComplete()) {
            return dfa;
        }

        String dead = "__dead__";

        Set<String> states = new LinkedHashSet<>(dfa.getStates());
        states.add(dead);

        Map<String, Map<String, Set<String>>> transitions =
                new LinkedHashMap<>();

        for (String state : dfa.getStates()) {

            Map<String, Set<String>> row = new LinkedHashMap<>();

            for (String symbol : dfa.getAlphabet()) {

                Set<String> destination =
                        dfa.getTransitions(state, symbol);

                row.put(
                        symbol,
                        destination.isEmpty()
                                ? Set.of(dead)
                                : destination
                );
            }

            transitions.put(state, row);
        }

        Map<String, Set<String>> deadRow = new LinkedHashMap<>();

        for (String symbol : dfa.getAlphabet()) {
            deadRow.put(symbol, Set.of(dead));
        }

        transitions.put(dead, deadRow);

        return new Automaton(
                states,
                dfa.getAlphabet(),
                dfa.getInitialState(),
                dfa.getFinalStates(),
                transitions
        );
    }

    private static Set<String> findReachableStates(Automaton dfa) {

        Set<String> visited = new LinkedHashSet<>();
        Queue<String> queue = new ArrayDeque<>();

        String initialState = dfa.getInitialState();

        queue.add(initialState);
        visited.add(initialState);

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
     * R(0) = {F, Q \ F}.
     */
    private static List<Set<String>> createInitialPartitions(
            Automaton dfa,
            Set<String> reachable
    ) {

        Set<String> finalStates =
                new LinkedHashSet<>(dfa.getFinalStates());

        finalStates.retainAll(reachable);

        Set<String> nonFinalStates =
                new LinkedHashSet<>(reachable);

        nonFinalStates.removeAll(finalStates);

        List<Set<String>> partitions = new ArrayList<>();

        if (!finalStates.isEmpty()) {
            partitions.add(finalStates);
        }

        if (!nonFinalStates.isEmpty()) {
            partitions.add(nonFinalStates);
        }

        return partitions;
    }

    /**
     * Строит R(n) по R(n - 1).
     *
     * Состояния остаются в одном классе, если по каждому символу
     * они переходят в состояния одного класса R(n - 1).
     */
    private static List<Set<String>> buildNextPartition(
            Automaton dfa,
            List<Set<String>> previous
    ) {

        Map<String, Integer> classNumbers =
                createStateClasses(previous);

        List<Set<String>> next = new ArrayList<>();

        for (Set<String> partition : previous) {

            // Конечный класс не разбиваем никогда.
            if (isFinalPartition(dfa, partition)) {
                next.add(new LinkedHashSet<>(partition));
                continue;
            }

            Map<List<Integer>, Set<String>> groups =
                    new LinkedHashMap<>();

            for (String state : partition) {

                List<Integer> signature =
                        getSignature(dfa, state, classNumbers);

                groups.computeIfAbsent(
                        signature,
                        key -> new LinkedHashSet<>()
                ).add(state);
            }

            next.addAll(groups.values());
        }

        return next;
    }

    private static boolean isFinalPartition(
            Automaton dfa,
            Set<String> partition
    ) {
        for (String state : partition) {
            if (dfa.getFinalStates().contains(state)) {
                return true;
            }
        }

        return false;
    }

    private static Map<String, Integer> createStateClasses(
            List<Set<String>> partitions
    ) {

        Map<String, Integer> classes = new HashMap<>();

        for (int i = 0; i < partitions.size(); i++) {
            for (String state : partitions.get(i)) {
                classes.put(state, i);
            }
        }

        return classes;
    }

    private static List<Integer> getSignature(
            Automaton dfa,
            String state,
            Map<String, Integer> classNumbers
    ) {

        List<Integer> signature = new ArrayList<>();

        for (String symbol : dfa.getAlphabet()) {

            Set<String> destinations =
                    dfa.getTransitions(state, symbol);

            if (destinations.size() != 1) {
                throw new IllegalStateException(
                        "Ожидался ровно один переход: δ("
                                + state + ", " + symbol + ")"
                );
            }

            String destination = destinations.iterator().next();

            Integer destinationClass = classNumbers.get(destination);

            if (destinationClass == null) {
                throw new IllegalStateException(
                        "Не найден класс состояния: " + destination
                );
            }

            signature.add(destinationClass);
        }

        return signature;
    }

    /**
     * Каждый класс R(n) становится состоянием минимального ДКА.
     *
     * Класс конечный, если он пересекается с F исходного автомата.
     */
    private static Automaton buildMinimalDfa(
            Automaton dfa,
            List<Set<String>> partitions,
            Set<String> reachable
    ) {

        Map<String, String> stateToClass = new LinkedHashMap<>();
        Set<String> minimalStates = new LinkedHashSet<>();
        Set<String> minimalFinalStates = new LinkedHashSet<>();
        Map<String, Map<String, Set<String>>> minimalTransitions =
                new LinkedHashMap<>();

        int counter = 0;

        System.out.println(
                "\nСоответствие состояний после минимизации:"
        );

        for (Set<String> partition : partitions) {

            String className = "M" + counter++;

            minimalStates.add(className);

            for (String state : partition) {
                stateToClass.put(state, className);
            }

            // Конечность класса определяется по F,
            // а не по переходам из него.
            if (!Collections.disjoint(
                    partition,
                    dfa.getFinalStates()
            )) {
                minimalFinalStates.add(className);
            }

            System.out.println(
                    className + " = " + formatSubset(partition)
                            + (minimalFinalStates.contains(className)
                            ? "  [F]"
                            : "")
            );
        }

        String minimalInitialState =
                stateToClass.get(dfa.getInitialState());

        if (minimalInitialState == null) {
            throw new IllegalStateException(
                    "Не найден класс начального состояния: "
                            + dfa.getInitialState()
            );
        }

        for (Set<String> partition : partitions) {

            String representative = partition.iterator().next();
            String currentClass = stateToClass.get(representative);

            Map<String, Set<String>> transitions =
                    new LinkedHashMap<>();

            for (String symbol : dfa.getAlphabet()) {

                Set<String> destinations =
                        dfa.getTransitions(representative, symbol);

                if (destinations.size() != 1) {
                    throw new IllegalStateException(
                            "Ожидался ровно один переход: δ("
                                    + representative
                                    + ", " + symbol + ")"
                    );
                }

                String destination = destinations.iterator().next();
                String destinationClass =
                        stateToClass.get(destination);

                if (destinationClass == null) {
                    throw new IllegalStateException(
                            "Не найден класс состояния: "
                                    + destination
                    );
                }

                transitions.put(
                        symbol,
                        Set.of(destinationClass)
                );
            }

            minimalTransitions.put(currentClass, transitions);
        }

        System.out.println("\n=== ИТОГОВЫЙ АВТОМАТ ===");
        System.out.println(
                "Количество состояний до минимизации: "
                        + dfa.getStates().size()
        );
        System.out.println(
                "Количество достижимых состояний: "
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

    private static void printAutomaton(Automaton dfa) {

        System.out.println(
                "M = ("
                        + formatSubset(dfa.getStates())
                        + ", "
                        + formatSubset(dfa.getAlphabet())
                        + ", δ, "
                        + dfa.getInitialState()
                        + ", "
                        + formatSubset(dfa.getFinalStates())
                        + ")"
        );

        System.out.println("\nТаблица переходов:");

        StringBuilder header = new StringBuilder("Состояние");

        for (String symbol : dfa.getAlphabet()) {
            header.append(String.format("%15s", symbol));
        }

        System.out.println(header);

        for (String state : dfa.getStates()) {

            StringBuilder row =
                    new StringBuilder(String.format("%-15s", state));

            for (String symbol : dfa.getAlphabet()) {

                Set<String> destinations =
                        dfa.getTransitions(state, symbol);

                row.append(String.format(
                        "%15s",
                        destinations.isEmpty()
                                ? "—"
                                : formatSubset(destinations)
                ));
            }

            System.out.println(row);
        }
    }

    private static String formatSubset(Set<String> subset) {

        if (subset.isEmpty()) {
            return "{}";
        }

        return "{"
                + String.join(",", subset)
                + "}";
    }

    private static void printPartitions(
            int iteration,
            List<Set<String>> partitions
    ) {

        String result = partitions.stream()
                .map(Minimizer::formatSubset)
                .collect(Collectors.joining(", "));

        System.out.println(
                "R(" + iteration + "): {" + result + "}"
        );
    }
}