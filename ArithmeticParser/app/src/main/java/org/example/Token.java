package org.example;

public class Token {
    private final Type type;
    private final String value;
    private final int position;

    public Token(Type type, String value, int position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return type + "('" + value + "')";
    }
}
