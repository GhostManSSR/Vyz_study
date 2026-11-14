package org.example;

import java.util.Objects;

public class TransitionKey {

    private final String state;
    private final Character input;
    private final char stackTop;

    public TransitionKey(String state, Character input, char stackTop) {
        this.state = state;
        this.input = input;
        this.stackTop = stackTop;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof TransitionKey)) return false;

        TransitionKey other = (TransitionKey) obj;

        return Objects.equals(state, other.state)
                && Objects.equals(input, other.input)
                && stackTop == other.stackTop;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, input, stackTop);
    }
}