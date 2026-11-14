package org.example;

public record Transition(String currentState, Character inputSymbol, char stackSymbol, String nextState,
                         String replacement) {

    @Override
    public String toString() {
        String input = inputSymbol == null
                ? "lambda" : inputSymbol.toString();

        String replace = replacement.isEmpty()
                ? "lambda" : replacement;

        return "δ(" + currentState + ", " + input
                + ", " + stackSymbol + ") = ("
                + nextState + ", " + replace + ")";
    }
}
