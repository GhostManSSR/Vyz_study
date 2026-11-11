package org.example;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class AutomatonTest {

    private static Set<String> ordered(String... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    private static Automaton machine(
            String[] states,
            String start,
            Set<String> finals,
            Map<String, Map<String, Set<String>>> edges
    ) {
        return new Automaton(
                ordered(states),
                ordered("0", "1"),
                start,
                finals,
                edges
        );
    }

    /**
     * Независимая проверка принятия слова.
     * Одновременно хранит все возможные текущие состояния НКА.
     */
    private static boolean accepts(Automaton automaton, String word) {
        Set<String> current = Set.of(automaton.getInitialState());

        for (int i = 0; i < word.length(); i++) {
            String symbol = word.substring(i, i + 1);
            Set<String> next = new HashSet<>();

            for (String state : current) {
                next.addAll(automaton.getTransitions(state, symbol));
            }

            current = next;
        }

        return !Collections.disjoint(
                current,
                automaton.getFinalStates()
        );
    }

    /**
     * Проверка языка на всех двоичных словах длиной от 0 до 8.
     */
    private static void languages(
            Predicate<String> expected,
            Automaton... automata
    ) {
        List<String> words = new ArrayList<>(List.of(""));

        for (int length = 0; length <= 8; length++) {
            List<String> next = new ArrayList<>();

            for (String word : words) {
                boolean expectedResult = expected.test(word);

                for (int i = 0; i < automata.length; i++) {
                    assertEquals(
                            expectedResult,
                            accepts(automata[i], word),
                            "automaton=" + i + ", word=\"" + word + "\""
                    );
                }

                if (length < 8) {
                    next.add(word + "0");
                    next.add(word + "1");
                }
            }

            words = next;
        }
    }

    /**
     * Получение единственного назначения перехода ДКА.
     */
    private static String target(
            Automaton automaton,
            String state,
            String symbol
    ) {
        Set<String> destinations =
                automaton.getTransitions(state, symbol);

        assertEquals(
                1,
                destinations.size(),
                "Ожидался единственный переход из "
                        + state + " по символу " + symbol
        );

        return destinations.iterator().next();
    }

    /**
     * Снимок структуры автомата для проверки отсутствия изменений.
     */
    private static List<Object> snapshot(Automaton automaton) {
        List<Object> result = new ArrayList<>();

        result.add(new LinkedHashSet<>(automaton.getStates()));
        result.add(new LinkedHashSet<>(automaton.getAlphabet()));
        result.add(automaton.getInitialState());
        result.add(new LinkedHashSet<>(automaton.getFinalStates()));

        for (String state : automaton.getStates()) {
            for (String symbol : automaton.getAlphabet()) {
                result.add(
                        new LinkedHashSet<>(
                                automaton.getTransitions(state, symbol)
                        )
                );
            }
        }

        return result;
    }

    // ------------------------------------------------------------
    // Исходный пример и детерминизация
    // ------------------------------------------------------------

    @Test
    void lectureNfaHasExactOriginalTable() {
        Automaton automaton = App.createLectureExample();

        assertEquals(ordered("p", "q", "r"), automaton.getStates());
        assertEquals(ordered("0", "1"), automaton.getAlphabet());
        assertEquals("p", automaton.getInitialState());
        assertEquals(Set.of("r"), automaton.getFinalStates());

        assertEquals(
                Set.of("p"),
                automaton.getTransitions("p", "0")
        );

        assertEquals(
                Set.of("p", "q"),
                automaton.getTransitions("p", "1")
        );

        assertEquals(
                Set.of("p"),
                automaton.getTransitions("q", "0")
        );

        assertEquals(
                Set.of("p","q","r"),
                automaton.getTransitions("q", "1")
        );

        assertEquals(
                Set.of("p"),
                automaton.getTransitions("r", "0")
        );
        assertEquals(
                Set.of("p","q","r"),
                automaton.getTransitions("r", "1")
        );

        assertFalse(automaton.isDeterministic());
        assertTrue(automaton.canReachFinalState());
    }

    @Test
    void lectureDeterminizationHasExactTableAndPreservesLanguage() {
        Automaton nfa = App.createLectureExample();
        Automaton dfa = Determinizer.determinize(nfa);

        assertEquals(ordered("A", "B", "C"), dfa.getStates());
        assertEquals(
                List.of("A", "B", "C"),
                new ArrayList<>(dfa.getStates())
        );

        assertEquals("A", dfa.getInitialState());
        assertEquals(Set.of("C"), dfa.getFinalStates());

        assertTrue(dfa.isDeterministic());
        assertTrue(dfa.canReachFinalState());

        for (String state : dfa.getStates()) {
            assertEquals("A", target(dfa, state, "0"));
        }

        assertEquals("B", target(dfa, "A", "1"));
        assertEquals("C", target(dfa, "B", "1"));
        assertEquals("C", target(dfa, "C", "1"));

        Automaton minimal = Minimizer.minimize(dfa);

        assertEquals(3, minimal.getStates().size());
        assertTrue(minimal.isDeterministic());
        assertTrue(minimal.canReachFinalState());

        languages(
                word -> word.endsWith("11"),
                nfa,
                dfa,
                minimal
        );
    }

    @Test
    void determinizationKeepsOnlyReachableSubsetsInResult() {
        Automaton nfa = new Automaton(
                ordered("q0", "q1", "q2", "q3"),
                ordered("0"),
                "q0",
                Set.of("q1"),
                Map.of(
                        "q0", Map.of(
                                "0", Set.of("q0", "q1")
                        )
                )
        );

        Automaton dfa = Determinizer.determinize(nfa);

        // Достижимы только {q0} и {q0, q1}.
        assertEquals(ordered("A", "B"), dfa.getStates());
        assertEquals("A", dfa.getInitialState());
        assertEquals(Set.of("B"), dfa.getFinalStates());

        assertTrue(dfa.isDeterministic());
        assertTrue(dfa.canReachFinalState());

        assertEquals("B", target(dfa, "A", "0"));
        assertEquals("B", target(dfa, "B", "0"));

        assertFalse(accepts(nfa, ""));
        assertFalse(accepts(dfa, ""));

        for (int length = 1; length <= 8; length++) {
            String word = "0".repeat(length);

            assertTrue(accepts(nfa, word), "NFA: word=" + word);
            assertTrue(accepts(dfa, word), "DFA: word=" + word);
        }
    }

    @Test
    void emptyTransitionsDoNotCreateSubsetState() {
        Automaton nfa = machine(
                new String[]{"s"},
                "s",
                Set.of("s"),
                Map.of()
        );

        Automaton dfa = Determinizer.determinize(nfa);

        assertEquals(Set.of("A"), dfa.getStates());
        assertEquals("A", dfa.getInitialState());
        assertEquals(Set.of("A"), dfa.getFinalStates());

        assertTrue(dfa.getTransitions("A", "0").isEmpty());
        assertTrue(dfa.getTransitions("A", "1").isEmpty());

        assertTrue(dfa.isDeterministic());
        assertTrue(dfa.canReachFinalState());

        Automaton minimal = Minimizer.minimize(dfa);

        assertEquals(1, minimal.getStates().size());

        languages(
                String::isEmpty,
                nfa,
                dfa,
                minimal
        );
    }

    @Test
    void acceptingInitialStateAndMultipleFinalsPreserveEmptyWord() {
        Automaton nfa = machine(
                new String[]{"s", "f"},
                "s",
                Set.of("s", "f"),
                Map.of(
                        "s", Map.of(
                                "0", Set.of("s", "f"),
                                "1", Set.of("f")
                        ),
                        "f", Map.of(
                                "0", Set.of("f"),
                                "1", Set.of("f")
                        )
                )
        );

        Automaton dfa = Determinizer.determinize(nfa);
        Automaton minimal = Minimizer.minimize(dfa);

        assertTrue(accepts(nfa, ""));
        assertTrue(accepts(dfa, ""));
        assertTrue(accepts(minimal, ""));

        assertEquals(1, minimal.getStates().size());

        languages(word -> true, nfa, dfa, minimal);
    }

    // ------------------------------------------------------------
    // Минимизация
    // ------------------------------------------------------------

    @Test
    void sixStateLectureDfaHasCorrectQuotientAndLanguage() {
        String[][] rows = {
                {"q1", "q2"},
                {"q4", "q2"},
                {"q3", "q0"},
                {"q5", "q2"},
                {"q5", "q5"},
                {"q4", "q4"}
        };

        Map<String, Map<String, Set<String>>> edges =
                new LinkedHashMap<>();

        for (int i = 0; i < rows.length; i++) {
            edges.put(
                    "q" + i,
                    Map.of(
                            "0", Set.of(rows[i][0]),
                            "1", Set.of(rows[i][1])
                    )
            );
        }

        Automaton original = machine(
                new String[]{"q0", "q1", "q2", "q3", "q4", "q5"},
                "q0",
                Set.of("q4", "q5"),
                edges
        );

        Automaton minimal = Minimizer.minimize(original);

        assertEquals(3, minimal.getStates().size());
        assertTrue(minimal.isDeterministic());
        assertTrue(minimal.canReachFinalState());

        String start = minimal.getInitialState();
        String middle = target(minimal, start, "0");
        String end = target(minimal, middle, "0");

        assertEquals(Set.of(end), minimal.getFinalStates());

        assertEquals(start, target(minimal, start, "1"));
        assertEquals(start, target(minimal, middle, "1"));
        assertEquals(end, target(minimal, end, "0"));
        assertEquals(end, target(minimal, end, "1"));

        Map<String, String> quotient = Map.of(
                "q0", start,
                "q2", start,
                "q1", middle,
                "q3", middle,
                "q4", end,
                "q5", end
        );

        for (String state : original.getStates()) {
            for (String symbol : original.getAlphabet()) {
                String originalDestination =
                        target(original, state, symbol);

                assertEquals(
                        quotient.get(originalDestination),
                        target(
                                minimal,
                                quotient.get(state),
                                symbol
                        )
                );
            }
        }

        languages(
                word -> word.contains("00"),
                original,
                minimal
        );
    }

    @Test
    void incompleteDfaMergesEquivalentExplicitDeadStates() {
        Automaton original = machine(
                new String[]{"s", "f", "dead1", "dead2"},
                "s",
                Set.of("f"),
                Map.of(
                        "s", Map.of(
                                "0", Set.of("f"),
                                "1", Set.of("dead1")
                        ),
                        "dead1", Map.of(
                                "0", Set.of("dead2"),
                                "1", Set.of("dead1")
                        ),
                        "dead2", Map.of(
                                "0", Set.of("dead2"),
                                "1", Set.of("dead2")
                        )
                )
        );

        Automaton minimal = Minimizer.minimize(original);

        assertEquals(3, minimal.getStates().size());
        assertTrue(minimal.isDeterministic());
        assertTrue(minimal.canReachFinalState());

        String start = minimal.getInitialState();
        String accepting = target(minimal, start, "0");
        String dead = target(minimal, start, "1");

        assertEquals(Set.of(accepting), minimal.getFinalStates());

        assertTrue(
                minimal.getTransitions(accepting, "0").isEmpty()
        );
        assertTrue(
                minimal.getTransitions(accepting, "1").isEmpty()
        );

        assertEquals(dead, target(minimal, dead, "0"));
        assertEquals(dead, target(minimal, dead, "1"));

        languages(
                word -> word.equals("0"),
                original,
                minimal
        );
    }

    @Test
    void unreachableAcceptingAndRejectingStatesAreRemoved() {
        Automaton original = machine(
                new String[]{
                        "s",
                        "f",
                        "unreachableFinal",
                        "unreachableRejecting"
                },
                "s",
                Set.of("f", "unreachableFinal"),
                Map.of(
                        "s", Map.of(
                                "0", Set.of("s"),
                                "1", Set.of("f")
                        ),
                        "f", Map.of(
                                "0", Set.of("s"),
                                "1", Set.of("f")
                        ),
                        "unreachableFinal", Map.of(
                                "0", Set.of("unreachableFinal"),
                                "1", Set.of("unreachableFinal")
                        ),
                        "unreachableRejecting", Map.of(
                                "0", Set.of("unreachableRejecting"),
                                "1", Set.of("unreachableRejecting")
                        )
                )
        );

        Automaton minimal = Minimizer.minimize(original);

        assertEquals(2, minimal.getStates().size());
        assertEquals(1, minimal.getFinalStates().size());

        assertTrue(minimal.isDeterministic());
        assertTrue(minimal.canReachFinalState());

        languages(
                word -> word.endsWith("1"),
                original,
                minimal
        );
    }

    @Test
    void minimizationRejectsNondeterminism() {
        Automaton nfa = App.createLectureExample();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> Minimizer.minimize(nfa)
        );

        assertTrue(error.getMessage().contains("не является ДКА"));
    }

    // ------------------------------------------------------------
    // Достижимость конечных состояний
    // ------------------------------------------------------------

    @Test
    void rejectsAutomatonWithoutFinalStates() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new Automaton(
                        ordered("s", "dead"),
                        ordered("0"),
                        "s",
                        Set.of(),
                        Map.of(
                                "s", Map.of(
                                        "0", Set.of("dead")
                                )
                        )
                )
        );

        assertTrue(
                error.getMessage().contains(
                        "невозможно достичь ни одного конечного состояния"
                )
        );
    }

    @Test
    void rejectsAutomatonWithOnlyUnreachableFinalStates() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> machine(
                        new String[]{"s", "dead", "f"},
                        "s",
                        Set.of("f"),
                        Map.of(
                                "s", Map.of(
                                        "0", Set.of("dead"),
                                        "1", Set.of("s")
                                ),
                                "dead", Map.of(
                                        "0", Set.of("dead"),
                                        "1", Set.of("dead")
                                )
                        )
                )
        );

        assertTrue(
                error.getMessage().contains(
                        "невозможно достичь ни одного конечного состояния"
                )
        );
    }

    @Test
    void acceptingInitialStateIsReachableWithoutTransitions() {
        Automaton automaton = machine(
                new String[]{"s"},
                "s",
                Set.of("s"),
                Map.of()
        );

        assertTrue(automaton.canReachFinalState());

        assertTrue(accepts(automaton, ""));
        assertFalse(accepts(automaton, "0"));
        assertFalse(accepts(automaton, "1"));
    }

    @Test
    void finalStateIsReachableThroughSeveralTransitions() {
        Automaton automaton = machine(
                new String[]{"s", "middle", "f"},
                "s",
                Set.of("f"),
                Map.of(
                        "s", Map.of(
                                "0", Set.of("middle")
                        ),
                        "middle", Map.of(
                                "1", Set.of("f")
                        )
                )
        );

        assertTrue(automaton.canReachFinalState());

        languages(word -> word.equals("01"), automaton);
    }

    @Test
    void oneReachableFinalStateIsEnough() {
        Automaton automaton = machine(
                new String[]{"s", "f", "unreachableFinal"},
                "s",
                Set.of("f", "unreachableFinal"),
                Map.of(
                        "s", Map.of(
                                "0", Set.of("f")
                        )
                )
        );

        assertTrue(automaton.canReachFinalState());

        languages(word -> word.equals("0"), automaton);
    }

    @Test
    void cyclesDoNotPreventFindingFinalState() {
        Automaton automaton = machine(
                new String[]{"s", "loop", "f"},
                "s",
                Set.of("f"),
                Map.of(
                        "s", Map.of(
                                "0", Set.of("loop")
                        ),
                        "loop", Map.of(
                                "0", Set.of("s"),
                                "1", Set.of("f")
                        )
                )
        );

        assertTrue(automaton.canReachFinalState());

        assertTrue(accepts(automaton, "01"));
        assertTrue(accepts(automaton, "0001"));
        assertTrue(accepts(automaton, "000001"));

        assertFalse(accepts(automaton, ""));
        assertFalse(accepts(automaton, "1"));
        assertFalse(accepts(automaton, "001"));
    }

    @Test
    void finalStateIsReachableThroughOneNondeterministicBranch() {
        Automaton nfa = machine(
                new String[]{"s", "dead", "f"},
                "s",
                Set.of("f"),
                Map.of(
                        "s", Map.of(
                                "0", Set.of("dead", "f")
                        ),
                        "dead", Map.of(
                                "0", Set.of("dead"),
                                "1", Set.of("dead")
                        )
                )
        );

        assertFalse(nfa.isDeterministic());
        assertTrue(nfa.canReachFinalState());

        Automaton dfa = Determinizer.determinize(nfa);
        Automaton minimal = Minimizer.minimize(dfa);

        assertTrue(dfa.isDeterministic());
        assertTrue(minimal.isDeterministic());

        assertTrue(dfa.canReachFinalState());
        assertTrue(minimal.canReachFinalState());

        languages(
                word -> word.equals("0"),
                nfa,
                dfa,
                minimal
        );
    }

    // ------------------------------------------------------------
    // Валидация структуры автомата
    // ------------------------------------------------------------

    @Test
    void rejectsUnknownInitialState() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> machine(
                        new String[]{"s"},
                        "x",
                        Set.of("s"),
                        Map.of()
                )
        );

        assertTrue(
                error.getMessage().contains(
                        "Начальное состояние отсутствует в Q"
                )
        );
    }

    @Test
    void rejectsUnknownFinalState() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> machine(
                        new String[]{"s"},
                        "s",
                        Set.of("x"),
                        Map.of()
                )
        );

        assertTrue(
                error.getMessage().contains(
                        "Конечные состояния должны входить в Q"
                )
        );
    }

    @Test
    void rejectsUnknownSourceBeforeNormalization() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> machine(
                        new String[]{"s"},
                        "s",
                        Set.of("s"),
                        Map.of(
                                "x", Map.of()
                        )
                )
        );

        assertTrue(
                error.getMessage().contains("Неизвестное состояние")
        );
        assertTrue(error.getMessage().contains("x"));
    }

    @Test
    void rejectsUnknownSymbolBeforeNormalization() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> machine(
                        new String[]{"s"},
                        "s",
                        Set.of("s"),
                        Map.of(
                                "s", Map.of(
                                        "x", Set.of("s")
                                )
                        )
                )
        );

        assertTrue(
                error.getMessage().contains("Неизвестный символ")
        );
        assertTrue(error.getMessage().contains("x"));
    }

    @Test
    void rejectsUnknownDestination() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> machine(
                        new String[]{"s"},
                        "s",
                        Set.of("s"),
                        Map.of(
                                "s", Map.of(
                                        "0", Set.of("x")
                                )
                        )
                )
        );

        assertTrue(
                error.getMessage().contains("неизвестное состояние")
        );
        assertTrue(error.getMessage().contains("x"));
    }

    @Test
    void rejectsEmptyStatesAndAlphabet() {
        IllegalArgumentException statesError = assertThrows(
                IllegalArgumentException.class,
                () -> machine(
                        new String[]{},
                        "s",
                        Set.of(),
                        Map.of()
                )
        );

        assertTrue(
                statesError.getMessage().contains(
                        "Множество состояний не может быть пустым"
                )
        );

        IllegalArgumentException alphabetError = assertThrows(
                IllegalArgumentException.class,
                () -> new Automaton(
                        Set.of("s"),
                        Set.of(),
                        "s",
                        Set.of("s"),
                        Map.of()
                )
        );

        assertTrue(
                alphabetError.getMessage().contains(
                        "Алфавит не может быть пустым"
                )
        );
    }

    @Test
    void missingTransitionsAreStoredAsEmptySets() {
        Automaton automaton = machine(
                new String[]{"s", "f"},
                "s",
                Set.of("f"),
                Map.of(
                        "s", Map.of(
                                "0", Set.of("f")
                        )
                )
        );

        assertEquals(
                Set.of("f"),
                automaton.getTransitions("s", "0")
        );

        assertTrue(automaton.getTransitions("s", "1").isEmpty());
        assertTrue(automaton.getTransitions("f", "0").isEmpty());
        assertTrue(automaton.getTransitions("f", "1").isEmpty());

        assertTrue(automaton.isDeterministic());
        assertTrue(automaton.canReachFinalState());
    }

    // ------------------------------------------------------------
    // Null: ожидания для конструктора без явных null-проверок
    // ------------------------------------------------------------

    @Test
    void rejectsNullCollectionsRowsAndDestinations() {
        assertThrows(
                NullPointerException.class,
                () -> new Automaton(
                        null,
                        ordered("0"),
                        "s",
                        Set.of("s"),
                        Map.of()
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> new Automaton(
                        ordered("s"),
                        null,
                        "s",
                        Set.of("s"),
                        Map.of()
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> machine(
                        new String[]{"s"},
                        "s",
                        null,
                        Map.of()
                )
        );

        assertThrows(
                NullPointerException.class,
                () -> machine(
                        new String[]{"s"},
                        "s",
                        Set.of("s"),
                        null
                )
        );

        Map<String, Map<String, Set<String>>> nullRow =
                new HashMap<>();

        nullRow.put("s", null);

        assertThrows(
                NullPointerException.class,
                () -> machine(
                        new String[]{"s"},
                        "s",
                        Set.of("s"),
                        nullRow
                )
        );

        Map<String, Set<String>> row = new HashMap<>();
        row.put("0", null);

        Map<String, Map<String, Set<String>>> nullDestinations =
                new HashMap<>();

        nullDestinations.put("s", row);

        assertThrows(
                NullPointerException.class,
                () -> machine(
                        new String[]{"s"},
                        "s",
                        Set.of("s"),
                        nullDestinations
                )
        );
    }

    @Test
    void rejectsNullNamesInTransitionTable() {
        Map<String, Map<String, Set<String>>> nullSource =
                new HashMap<>();

        nullSource.put(null, Map.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> machine(
                        new String[]{"s"},
                        "s",
                        Set.of("s"),
                        nullSource
                )
        );

        Map<String, Set<String>> nullSymbolRow = new HashMap<>();
        nullSymbolRow.put(null, Set.of("s"));

        Map<String, Map<String, Set<String>>> nullSymbol =
                new HashMap<>();

        nullSymbol.put("s", nullSymbolRow);

        assertThrows(
                IllegalArgumentException.class,
                () -> machine(
                        new String[]{"s"},
                        "s",
                        Set.of("s"),
                        nullSymbol
                )
        );

        Set<String> destinationsWithNull = new HashSet<>();
        destinationsWithNull.add(null);

        Map<String, Map<String, Set<String>>> nullDestination =
                new HashMap<>();

        nullDestination.put(
                "s",
                Map.of("0", destinationsWithNull)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> machine(
                        new String[]{"s"},
                        "s",
                        Set.of("s"),
                        nullDestination
                )
        );
    }

    // ------------------------------------------------------------
    // Защитное копирование и неизменяемые представления
    // ------------------------------------------------------------

    @Test
    void constructorCopiesCollectionsAndGettersAreUnmodifiable() {
        Set<String> states = ordered("s");
        Set<String> alphabet = ordered("0");
        Set<String> finals = ordered("s");
        Set<String> destinations = ordered("s");

        Map<String, Set<String>> row = new HashMap<>();
        row.put("0", destinations);

        Map<String, Map<String, Set<String>>> edges =
                new HashMap<>();

        edges.put("s", row);

        Automaton automaton = new Automaton(
                states,
                alphabet,
                "s",
                finals,
                edges
        );

        states.clear();
        alphabet.clear();
        finals.clear();
        destinations.clear();
        row.clear();
        edges.clear();

        assertEquals(Set.of("s"), automaton.getStates());
        assertEquals(Set.of("0"), automaton.getAlphabet());
        assertEquals("s", automaton.getInitialState());
        assertEquals(Set.of("s"), automaton.getFinalStates());

        assertEquals(
                Set.of("s"),
                automaton.getTransitions("s", "0")
        );

        assertTrue(automaton.canReachFinalState());

        assertThrows(
                UnsupportedOperationException.class,
                () -> automaton.getStates().clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> automaton.getAlphabet().clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> automaton.getFinalStates().clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> automaton.getTransitions("s", "0").clear()
        );
    }

    @Test
    void transformationsDoNotMutateOriginals() {
        Automaton nfa = App.createLectureExample();
        List<Object> nfaBefore = snapshot(nfa);

        Automaton dfa = Determinizer.determinize(nfa);

        assertEquals(nfaBefore, snapshot(nfa));

        List<Object> dfaBefore = snapshot(dfa);

        Minimizer.minimize(dfa);

        assertEquals(nfaBefore, snapshot(nfa));
        assertEquals(dfaBefore, snapshot(dfa));
    }
}