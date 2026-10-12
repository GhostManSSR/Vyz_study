package org.example;

import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int position;

    private boolean hasError;
    private String errorMessage;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        this.hasError = false;
        this.errorMessage = null;
    }

    public boolean hasError() {
        return hasError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private Token currentToken() {
        return tokens.get(position);
    }

    private Token match(Type expected) {
        Token token = currentToken();

        if (hasError) {
            return token;
        }

        if (token.getType() != expected) {
            fail("Ожидалось: " + getExpectedName(expected));
            return token;
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

    private void fail(String message) {
        if (hasError) {
            return;
        }

        hasError = true;

        Token token = currentToken();

        errorMessage = "Ошибка! " + message
                + ". Текущий токен: "
                + (token.getType() == Type.EOF
                ? "EOF (конец ввода)"
                : "'" + token.getValue() + "'")
                + ". Позиция: "
                + (token.getPosition() + 1);
    }

    // S -> E
    public Node parseS() {
        Node node = new Node("S");

        node.addChild(parseE());

        if (!hasError && currentToken().getType() != Type.EOF) {
            fail("Ожидался конец выражения");
        }

        return node;
    }

    // E -> T E'
    private Node parseE() {
        Node node = new Node("E");

        if (hasError) {
            node.addChild(new Node("?"));
            return node;
        }

        node.addChild(parseT());
        node.addChild(parseEPrime());

        return node;
    }

    // E' -> + T E' | - T E' | ε
    private Node parseEPrime() {
        Node node = new Node("E'");

        if (hasError) {
            node.addChild(new Node("?"));
            return node;
        }

        if (currentToken().getType() == Type.PLUS) {
            node.addChild(new Node(match(Type.PLUS).getValue()));
            node.addChild(parseT());
            node.addChild(parseEPrime());

        } else if (currentToken().getType() == Type.MINUS) {
            node.addChild(new Node(match(Type.MINUS).getValue()));
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

        if (hasError) {
            node.addChild(new Node("?"));
            return node;
        }

        node.addChild(parseF());
        node.addChild(parseTPrime());

        return node;
    }

    // T' -> * F T' | / F T' | ε
    private Node parseTPrime() {
        Node node = new Node("T'");

        if (hasError) {
            node.addChild(new Node("?"));
            return node;
        }

        if (currentToken().getType() == Type.MULTIPLY) {
            node.addChild(new Node(match(Type.MULTIPLY).getValue()));
            node.addChild(parseF());
            node.addChild(parseTPrime());

        } else if (currentToken().getType() == Type.DIVIDE) {
            node.addChild(new Node(match(Type.DIVIDE).getValue()));
            node.addChild(parseF());
            node.addChild(parseTPrime());

        } else {
            node.addChild(new Node("ε"));
        }

        return node;
    }

    // F -> - G | ( E ) | number | id
    private Node parseF() {
        Node node = new Node("F");

        if (hasError) {
            node.addChild(new Node("?"));
            return node;
        }

        Token token = currentToken();

        if (token.getType() == Type.MINUS) {
            node.addChild(new Node(match(Type.MINUS).getValue()));
            node.addChild(parseG());
            return node;
        }

        if (token.getType() == Type.LPAREN) {
            node.addChild(new Node(match(Type.LPAREN).getValue()));
            node.addChild(parseE());
            node.addChild(new Node(match(Type.RPAREN).getValue()));

        } else if (token.getType() == Type.NUMBER) {
            node.addChild(new Node(match(Type.NUMBER).getValue()));

        } else if (token.getType() == Type.ID) {
            node.addChild(new Node(match(Type.ID).getValue()));

        } else {
            fail("Ожидалось: '-', '(', number или id");
            node.addChild(new Node("?"));
        }

        return node;
    }

    // G -> ( E ) | number | id
    private Node parseG() {
        Node node = new Node("G");

        if (hasError) {
            node.addChild(new Node("?"));
            return node;
        }

        Token token = currentToken();

        if (token.getType() == Type.LPAREN) {
            node.addChild(new Node(match(Type.LPAREN).getValue()));
            node.addChild(parseE());
            node.addChild(new Node(match(Type.RPAREN).getValue()));

        } else if (token.getType() == Type.NUMBER) {
            node.addChild(new Node(match(Type.NUMBER).getValue()));

        } else if (token.getType() == Type.ID) {
            node.addChild(new Node(match(Type.ID).getValue()));

        } else {
            fail("После '-' ожидалось '(', number или id");
            node.addChild(new Node("?"));
        }

        return node;
    }
}