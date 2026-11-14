package org.example;

import java.util.*;

public class PushdownAutomaton {

    private final Set<String> states;
    private final Set<Character> inputAlphabet;
    private final Set<Character> stackAlphabet;
    private final Set<String> finalStates;

    private final String initialState;
    private final char initialStackSymbol;

    private final Map<TransitionKey, Transition> transitions =
            new HashMap<>();

    private static final int MAX_STEPS = 100000;

    public PushdownAutomaton(Set<String> states, Set<Character> inputAlphabet, Set<Character> stackAlphabet, String initialState, char initialStackSymbol, Set<String> finalStates) {
        this.states = new HashSet<>(states);
        this.inputAlphabet = new HashSet<>(inputAlphabet);
        this.stackAlphabet = new HashSet<>(stackAlphabet);
        this.initialState = initialState;
        this.initialStackSymbol = initialStackSymbol;
        this.finalStates = new HashSet<>(finalStates);

        if (!this.states.contains(initialState)) {
            throw new IllegalArgumentException(
                    "Начальное состояние не существует");
        }

        if (!this.stackAlphabet.contains(initialStackSymbol)) {
            throw new IllegalArgumentException(
                    "Начальный символ отсутствует в алфавите стека");
        }

        if (this.finalStates.isEmpty()
                || !this.states.containsAll(this.finalStates)) {
            throw new IllegalArgumentException(
                    "Некорректное множество конечных состояний");
        }

    }

    // Добавление правила перехода
    public void addTransition(Transition transition) {

        String state = transition.currentState();
        String nextState = transition.nextState();
        Character input = transition.inputSymbol();
        char stackTop = transition.stackSymbol();
        String replacement = transition.replacement();

        if (!states.contains(state) || !states.contains(nextState)) {
            throw new IllegalArgumentException(
                    "Переход содержит неизвестное состояние");
        }

        if (!stackAlphabet.contains(stackTop)) {
            throw new IllegalArgumentException(
                    "Неизвестный символ стека: " + stackTop);
        }

        if (input != null && !inputAlphabet.contains(input)) {
            throw new IllegalArgumentException(
                    "Символ отсутствует во входном алфавите: " + input);
        }

        for (char c : replacement.toCharArray()) {
            if (!stackAlphabet.contains(c)) {
                throw new IllegalArgumentException(
                        "Неизвестный символ в замене стека: " + c);
            }
        }

        TransitionKey key = new TransitionKey(
                state, input, stackTop);

        if (transitions.containsKey(key)) {
            throw new IllegalArgumentException(
                    "Нарушена детерминированность: "
                            + "для одной конфигурации задано несколько переходов");
        }

        // Для ДМПА lambda-переход не может сосуществовать
        // с обычными переходами при том же состоянии и вершине стека.
        for (Transition existing : transitions.values()) {

            if (!existing.currentState().equals(
                    transition.currentState())) {
                continue;
            }

            if (existing.stackSymbol() != transition.stackSymbol()) {
                continue;
            }

            if (existing.inputSymbol() == null
                    || transition.inputSymbol() == null) {

                throw new IllegalArgumentException(
                        "Нарушена детерминированность!\n"
                                + "Конфликтующие переходы:\n"
                                + existing + "\n"
                                + transition
                );
            }

            if (existing.inputSymbol().equals(
                    transition.inputSymbol())) {

                throw new IllegalArgumentException(
                        "Для одной конфигурации задано несколько переходов:\n"
                                + existing + "\n"
                                + transition
                );
            }
        }

        transitions.put(key, transition);
    }

    // Проверка входной цепочки
    public boolean accepts(String input) {

        System.out.println("\nПроверка цепочки: "
                + (input.isEmpty() ? "lambda" : input));

        // Проверка входного алфавита
        for (char c : input.toCharArray()) {
            if (!inputAlphabet.contains(c)) {
                System.out.println(
                        "ОТКЛОНЕНА: символ '" + c
                                + "' отсутствует во входном алфавите.");
                return false;
            }
        }

        String currentState = initialState;
        String stack = String.valueOf(initialStackSymbol);

        int position = 0;
        int steps = 0;

        Set<String> visited = new HashSet<>();

        while (true) {

            // Проверяем достижение конечного состояния
            // после полного прочтения входной цепочки.
            if (position == input.length()
                    && finalStates.contains(currentState)) {

                System.out.println("Конфигурация: ("
                        + currentState + ", lambda, "
                        + (stack.isEmpty() ? "lambda" : stack) + ")");

                System.out.println("ЦЕПОЧКА ПРИНЯТА.");
                return true;
            }

            // Проверка ограничения количества тактов
            if (steps >= MAX_STEPS) {
                System.out.println(
                        "ОТКЛОНЕНА: превышено максимальное "
                                + "количество тактов. Возможно, "
                                + "автомат зациклился.");
                return false;
            }

            // Проверка повторения конфигурации
            String configKey = currentState + "|"
                    + position + "|" + stack;

            if (!visited.add(configKey)) {
                System.out.println(
                        "ОТКЛОНЕНА: обнаружено зацикливание "
                                + "автомата. Повторилась конфигурация.");
                return false;
            }

            System.out.println(
                    "Конфигурация: ("
                            + currentState + ", "
                            + input.substring(position)
                            + ", "
                            + (stack.isEmpty() ? "lambda" : stack)
                            + ")");

            // Если стек пуст, дальнейшая работа невозможна
            if (stack.isEmpty()) {
                System.out.println(
                        "ОТКЛОНЕНА: магазин пуст, "
                                + "но цепочка не принята.");
                return false;
            }

            char stackTop = stack.charAt(0);

            Transition transition = null;

            // Сначала ищем обычный переход
            if (position < input.length()) {

                char symbol = input.charAt(position);

                transition = transitions.get(
                        new TransitionKey(
                                currentState, symbol, stackTop));
            }

            // Если обычный переход отсутствует,
            // ищем lambda-переход
            if (transition == null) {

                transition = transitions.get(
                        new TransitionKey(
                                currentState, null, stackTop));
            }

            // Если переход не найден, автомат останавливается
            if (transition == null) {

                if (position < input.length()) {
                    System.out.println(
                            "ОТКЛОНЕНА: отсутствует переход "
                                    + "для состояния "
                                    + currentState
                                    + ", входного символа '"
                                    + input.charAt(position)
                                    + "' и вершины стека '"
                                    + stackTop + "'.");
                } else {
                    System.out.println(
                            "ОТКЛОНЕНА: входная цепочка прочитана, "
                                    + "но автомат не находится "
                                    + "в конечном состоянии.");
                }

                return false;
            }

            // Выполнение перехода
            System.out.println("Применяется: " + transition);

            // Удаляем верхний символ и заменяем его строкой
            stack = transition.replacement()
                    + stack.substring(1);

            currentState = transition.nextState();

            // При lambda-переходе входная позиция не меняется
            if (transition.inputSymbol() != null) {
                position++;
            }

            steps++;
        }
    }
}