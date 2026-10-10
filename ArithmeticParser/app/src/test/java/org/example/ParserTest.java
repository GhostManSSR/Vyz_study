package org.example;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private void assertValid(String expression) {
        Lexer lexer = new Lexer(expression);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens);
        assertNotNull(parser.parseS());
    }

    private RuntimeException assertInvalid(String expression) {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> {
                    Lexer lexer = new Lexer(expression);
                    Parser parser = new Parser(lexer.tokenize());
                    parser.parseS();
                }
        );

        assertTrue(
                exception.getMessage().contains("Ошибка!"),
                "Сообщение должно содержать описание ошибки"
        );

        assertTrue(
                exception.getMessage().contains("Позиция:"),
                "Сообщение должно содержать позицию ошибки"
        );

        return exception;
    }

    // Корректные выражения

    @Test
    void shouldParseSimpleAddition() {
        assertValid("2 + 3");
    }

    @Test
    void shouldParseOperatorPrecedence() {
        assertValid("2 + 3 * 4");
    }

    @Test
    void shouldParseIdentifiersAndParentheses() {
        assertValid("a * (b - 10)");
    }

    @Test
    void shouldParseNestedParentheses() {
        assertValid("((2 + 3) * (4 - 1))");
    }

    @Test
    void shouldParseDivision() {
        assertValid("(7 + 3) / 2");
    }

    @Test
    void shouldParseIdentifiersWithDigits() {
        assertValid("value1 + x_2");
    }

    // Некорректные выражения

    @Test
    void shouldRejectTwoOperatorsInARow() {
        RuntimeException exception = assertInvalid("5 + + 3");

        assertTrue(
                exception.getMessage().contains(
                        "number, id или '('"
                )
        );
    }

    @Test
    void shouldRejectMissingClosingParenthesis() {
        RuntimeException exception = assertInvalid("(7 * 2");

        assertTrue(
                exception.getMessage().contains("')'")
        );
    }

    @Test
    void shouldRejectUnexpectedClosingParenthesis() {
        RuntimeException exception = assertInvalid("2 * )");

        assertTrue(
                exception.getMessage().contains(
                        "number, id или '('"
                )
        );
    }

    @Test
    void shouldRejectTwoNumbersWithoutOperator() {
        RuntimeException exception = assertInvalid("2 3");

        assertTrue(
                exception.getMessage().contains(
                        "конец выражения"
                )
        );
    }

    @Test
    void shouldRejectEmptyExpression() {
        RuntimeException exception = assertInvalid("");

        assertTrue(
                exception.getMessage().contains(
                        "number, id или '('"
                )
        );
    }

    @Test
    void shouldRejectInvalidCharacter() {
        RuntimeException exception = assertInvalid("2 @ 3");

        assertTrue(
                exception.getMessage().contains(
                        "Недопустимый символ"
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "Ожидалось:"
                )
        );
    }

    @Test
    void shouldRejectTrailingOperator() {
        RuntimeException exception = assertInvalid("10 +");

        assertTrue(
                exception.getMessage().contains(
                        "number, id или '('"
                )
        );
    }

    @Test
    void shouldRejectUnclosedNestedParentheses() {
        assertInvalid("((2 + 3)");
    }
}

