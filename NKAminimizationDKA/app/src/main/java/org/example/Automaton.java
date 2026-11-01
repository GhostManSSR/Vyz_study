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

        for (String state : states) {
            Map<String, Set<String>> stateTransitions =
                    new LinkedHashMap<>();

            for (String symbol : alphabet) {
                Set<String> destinations = new LinkedHashSet<>(
                        transitions
                                .getOrDefault(state, Collections.emptyMap())
                                .getOrDefault(symbol, Collections.emptySet())
                );

                stateTransitions.put(symbol, destinations);
            }

            this.transitions.put(state, stateTransitions);
        }

        validate();
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
}
