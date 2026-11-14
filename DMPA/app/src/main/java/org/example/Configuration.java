package org.example;

public class Configuration {

    private final String state;
    private final int inputPosition;
    private final String stack;

    public Configuration(String state, int inputPosition, String stack) {
        this.state = state;
        this.inputPosition = inputPosition;
        this.stack = stack;
    }

    public String getState() {
        return state;
    }

    public int getInputPosition() {
        return inputPosition;
    }

    public String getStack() {
        return stack;
    }

    @Override
    public String toString() {
        return "(" + state + ", позиция="
                + inputPosition + ", стек="
                + (stack.isEmpty() ? "lambda" : stack) + ")";
    }
}