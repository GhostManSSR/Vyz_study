package org.example;


import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int position;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
    }

    private Token currentToken() {
        return tokens.get(position);
    }

    private Token match(Type expected) {
        Token token = currentToken();

        if (token.getType() != expected) {
            throw error("Ожидалось: " + getExpectedName(expected));
        }

        position++;
        return token;
    }

    private String getExpectedName(Type type) {
        switch (type) {
            case NUMBER:
                return "number";
            case ID:
                return "id";
            case PLUS:
                return "'+'";
            case MINUS:
                return "'-'";
            case MULTIPLY:
                return "'*'";
            case DIVIDE:
                return "'/'";
            case LPAREN:
                return "'('";
            case RPAREN:
                return "')'";
            case EOF:
                return "конец выражения";
            default:
                return "неизвестный токен";
        }
    }

    // S -> E
    public Node parseS() {
        Node node = new Node("S");

        node.addChild(parseE());

        if (currentToken().getType() != Type.EOF) {
            throw error("Ожидался конец выражения");
        }

        return node;
    }

    // E -> T E'
    private Node parseE() {
        Node node = new Node("E");

        node.addChild(parseT());
        node.addChild(parseEPrime());

        return node;
    }

    // E' -> + T E' | - T E' | epsilon
    private Node parseEPrime() {
        Node node = new Node("E'");

        if (currentToken().getType() == Type.PLUS) {
            node.addChild(new Node(
                    match(Type.PLUS).getValue()
            ));

            node.addChild(parseT());
            node.addChild(parseEPrime());

        } else if (currentToken().getType() == Type.MINUS) {
            node.addChild(new Node(
                    match(Type.MINUS).getValue()
            ));

            node.addChild(parseT());
            node.addChild(parseEPrime());

        } else {
            node.addChild(new Node("ε"));
        }

        return node;
    }

    // T -> F T'
    private Node parseT() {
        Node node = new Node("T");

        node.addChild(parseF());
        node.addChild(parseTPrime());

        return node;
    }

    // T' -> * F T' | / F T' | epsilon
    private Node parseTPrime() {
        Node node = new Node("T'");

        if (currentToken().getType() == Type.MULTIPLY) {
            node.addChild(new Node(
                    match(Type.MULTIPLY).getValue()
            ));

            node.addChild(parseF());
            node.addChild(parseTPrime());

        } else if (currentToken().getType() == Type.DIVIDE) {
            node.addChild(new Node(
                    match(Type.DIVIDE).getValue()
            ));

            node.addChild(parseF());
            node.addChild(parseTPrime());

        } else {
            node.addChild(new Node("ε"));
        }

        return node;
    }

    // F -> ( E ) | number | id
    private Node parseF() {
        Node node = new Node("F");

        Token token = currentToken();

        if (token.getType() == Type.NUMBER) {
            node.addChild(new Node(
                    match(Type.NUMBER).getValue()
            ));

        } else if (token.getType() == Type.ID) {
            node.addChild(new Node(
                    match(Type.ID).getValue()
            ));

        } else if (token.getType() == Type.LPAREN) {
            node.addChild(new Node(
                    match(Type.LPAREN).getValue()
            ));

            node.addChild(parseE());

            node.addChild(new Node(
                    match(Type.RPAREN).getValue()
            ));

        } else {
            throw error(
                    "Ожидалось: number, id или '('"
            );
        }

        return node;
    }

    private RuntimeException error(String message) {
        Token token = currentToken();

        return new RuntimeException(
                "Ошибка! " + message
                        + ". Текущий токен: "
                        + (token.getType() == Type.EOF
                        ? "EOF (конец ввода)"
                        : "'" + token.getValue() + "'")
                        + ". Позиция: "
                        + (token.getPosition() + 1)
        );
    }
}

