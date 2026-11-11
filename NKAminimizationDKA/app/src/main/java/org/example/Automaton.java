package org.example;

import java.util.*;

public class Automaton {

    private final Set<String> states;
    private final Set<String> alphabet;
    private final String initialState;
    private final Set<String> finalStates;
    private final Map<String, Map<String, Set<String>>> transitions;

    public Automaton(
            Set<String> states,
            Set<String> alphabet,
            String initialState,
            Set<String> finalStates,
            Map<String, Map<String, Set<String>>> transitions
    ) {
        this.states = new LinkedHashSet<>(states);
        this.alphabet = new LinkedHashSet<>(alphabet);
        this.initialState = initialState;
        this.finalStates = new LinkedHashSet<>(finalStates);
        this.transitions = new LinkedHashMap<>();

        if (this.states.isEmpty()) {
            throw new IllegalArgumentException(
                    "Множество состояний не может быть пустым"
            );
        }

        if (this.alphabet.isEmpty()) {
            throw new IllegalArgumentException(
                    "Алфавит не может быть пустым"
            );
        }

        if (!this.states.contains(initialState)) {
            throw new IllegalArgumentException(
                    "Начальное состояние отсутствует в Q"
            );
        }

        if (!this.states.containsAll(this.finalStates)) {
            throw new IllegalArgumentException(
                    "Конечные состояния должны входить в Q"
            );
        }

        for (String state : this.states) {
            Map<String, Set<String>> stateTransitions = new LinkedHashMap<>();

            for (String symbol : this.alphabet) {
                stateTransitions.put(symbol, new LinkedHashSet<>());
            }

            this.transitions.put(state, stateTransitions);
        }

        for (var stateEntry : transitions.entrySet()) {
            String state = stateEntry.getKey();

            if (!this.states.contains(state)) {
                throw new IllegalArgumentException(
                        "Неизвестное состояние: " + state
                );
            }

            for (var transitionEntry : stateEntry.getValue().entrySet()) {
                String symbol = transitionEntry.getKey();

                if (!this.alphabet.contains(symbol)) {
                    throw new IllegalArgumentException(
                            "Неизвестный символ алфавита: " + symbol
                    );
                }

                Set<String> destinations = new LinkedHashSet<>();

                for (String destination : transitionEntry.getValue()) {
                    if (!this.states.contains(destination)) {
                        throw new IllegalArgumentException(
                                "Переход из " + state
                                        + " по символу " + symbol
                                        + " ведет в неизвестное состояние: "
                                        + destination
                        );
                    }

                    destinations.add(destination);
                }

                this.transitions.get(state).put(symbol, destinations);
            }
        }

//        if (!canReachFinalState()) {
//            throw new IllegalArgumentException(
//                    "Из начального состояния " + initialState
//                            + " невозможно достичь ни одного конечного состояния"
//            );
//        }
    }

    private void validate() {
        if (states.isEmpty()) {
            throw new IllegalArgumentException(
                    "Множество состояний не может быть пустым"
            );
        }

        if (alphabet.isEmpty()) {
            throw new IllegalArgumentException(
                    "Алфавит не может быть пустым"
            );
        }

        if (!states.contains(initialState)) {
            throw new IllegalArgumentException(
                    "Начальное состояние отсутствует в Q"
            );
        }

        if (!states.containsAll(finalStates)) {
            throw new IllegalArgumentException(
                    "Конечные состояния должны входить в Q"
            );
        }

        for (var stateEntry : transitions.entrySet()) {
            if (!states.contains(stateEntry.getKey())) {
                throw new IllegalArgumentException(
                        "Неизвестное состояние: " + stateEntry.getKey()
                );
            }

            for (var transitionEntry : stateEntry.getValue().entrySet()) {
                if (!alphabet.contains(transitionEntry.getKey())) {
                    throw new IllegalArgumentException(
                            "Неизвестный символ алфавита: "
                                    + transitionEntry.getKey()
                    );
                }

                if (!states.containsAll(transitionEntry.getValue())) {
                    throw new IllegalArgumentException(
                            "Переход ведет в неизвестное состояние"
                    );
                }
            }
        }
    }

    public boolean isComplete() {
        for (String state : getStates()) {
            for (String symbol : getAlphabet()) {
                if (getTransitions(state, symbol).size() != 1) {
                    return false;
                }
            }
        }

        return true;
    }

    public Set<String> getStates() {
        return Collections.unmodifiableSet(states);
    }

    public Set<String> getAlphabet() {
        return Collections.unmodifiableSet(alphabet);
    }

    public String getInitialState() {
        return initialState;
    }

    public Set<String> getFinalStates() {
        return Collections.unmodifiableSet(finalStates);
    }

    public Set<String> getTransitions(String state, String symbol) {
        return Collections.unmodifiableSet(
                transitions
                        .getOrDefault(state, Collections.emptyMap())
                        .getOrDefault(symbol, Collections.emptySet())
        );
    }

    private String formatSet(Collection<String> values) {
        return "{" + String.join(", ", values) + "}";
    }

    private String formatTransition(Collection<String> values) {
        if (values.isEmpty()) {
            return "—";
        }

        return formatSet(values);
    }

    /**
     * Математическое описание:
     * M = (Q, Σ, δ, q0, F)
     */
    public void printDefinition() {
        System.out.println(
                "M = ("
                        + formatSet(states) + ", "
                        + formatSet(alphabet) + ", "
                        + "δ, "
                        + initialState + ", "
                        + formatSet(finalStates)
                        + ")"
        );
    }

    public boolean isDeterministic() {
        for (String state : states) {
            for (String symbol : alphabet) {
                if (getTransitions(state, symbol).size() > 1) {
                    return false;
                }
            }
        }

        return true;
    }

    public void print() {
        printDefinition();

        System.out.println("\nТаблица переходов:");

        System.out.printf("%-15s", "Состояние");

        for (String symbol : alphabet) {
            System.out.printf("%-20s", symbol);
        }

        System.out.println();

        for (String state : states) {
            System.out.printf("%-15s", state);

            for (String symbol : alphabet) {
                System.out.printf(
                        "%-20s",
                        formatTransition(getTransitions(state, symbol))
                );
            }

            System.out.println();
        }
    }

    public boolean canReachFinalState() {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();

        visited.add(initialState);
        queue.add(initialState);

        while (!queue.isEmpty()) {
            String state = queue.remove();

            if (finalStates.contains(state)) {
                return true;
            }

            for (Set<String> destinations
                    : transitions.get(state).values()) {

                for (String destination : destinations) {
                    if (visited.add(destination)) {
                        queue.add(destination);
                    }
                }
            }
        }

        return false;
    }

    public void checkAcceptingStates() {

        // Проверяем, достижимо ли хотя бы одно конечное состояние.
        if (!canReachFinalState()) {

            System.out.println(
                    "Внимание: ни одно конечное состояние "
                            + "недостижимо из начального состояния."
            );

            System.out.println(
                    "Автомат не принимает ни одного слова."
            );

            return;
        }

        System.out.println(
                "Из начального состояния достижимо хотя бы одно "
                        + "конечное состояние."
        );
    }
}
