package org.example;


import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String input;
    private int position;

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char current = input.charAt(position);

            if (Character.isWhitespace(current)) {
                position++;
                continue;
            }

            int start = position;

            if (Character.isDigit(current)) {
                tokens.add(readNumber());
            } else if (Character.isLetter(current) || current == '_') {
                tokens.add(readIdentifier());
            } else {
                switch (current) {
                    case '+':
                        tokens.add(new Token(
                                Type.PLUS, "+", position));
                        position++;
                        break;

                    case '-':
                        tokens.add(new Token(
                                Type.MINUS, "-", position));
                        position++;
                        break;

                    case '*':
                        tokens.add(new Token(
                                Type.MULTIPLY, "*", position));
                        position++;
                        break;

                    case '/':
                        tokens.add(new Token(
                                Type.DIVIDE, "/", position));
                        position++;
                        break;

                    case '(':
                        tokens.add(new Token(
                                Type.LPAREN, "(", position));
                        position++;
                        break;

                    case ')':
                        tokens.add(new Token(
                                Type.RPAREN, ")", position));
                        position++;
                        break;

                    default:
                        throw new IllegalArgumentException(
                                "Ошибка! Недопустимый символ '" + current
                                        + "'. Ожидалось: number, id, '+', '-', '*', '/', '(' или ')'."
                                        + " Позиция: " + (start + 1)
                        );
                }
            }
        }

        tokens.add(new Token(
                Type.EOF, "EOF", input.length()));

        return tokens;
    }

    private Token readNumber() {
        int start = position;

        while (position < input.length()
                && Character.isDigit(input.charAt(position))) {
            position++;
        }

        return new Token(
                Type.NUMBER,
                input.substring(start, position),
                start
        );
    }

    private Token readIdentifier() {
        int start = position;

        while (position < input.length()) {
            char current = input.charAt(position);

            if (Character.isLetterOrDigit(current)
                    || current == '_') {
                position++;
            } else {
                break;
            }
        }

        return new Token(
                Type.ID,
                input.substring(start, position),
                start
        );
    }
}
