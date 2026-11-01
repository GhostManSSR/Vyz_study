package org.example;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AutomatonTest {

    @Test
    void lectureExampleShouldRemainMinimal() {

        Set<String> states = new LinkedHashSet<>(
                Arrays.asList("A", "B", "C")
        );

        Set<String> alphabet = new LinkedHashSet<>(
                Arrays.asList("0", "1")
        );

        Map<String, Map<String, Set<String>>> transitions =
                new LinkedHashMap<>();

        transitions.put("A", Map.of(
                "0", Set.of("A"),
                "1", Set.of("B")
        ));

        transitions.put("B", Map.of(
                "0", Set.of("A"),
                "1", Set.of("C")
        ));

        transitions.put("C", Map.of(
                "0", Set.of("A"),
                "1", Set.of("C")
        ));

        Automaton automaton = new Automaton(
                states,
                alphabet,
                "A",
                Set.of("C"),
                transitions
        );

        assertTrue(automaton.isDeterministic());

        Automaton minimized = Minimizer.minimize(automaton);

        assertEquals(3, minimized.getStates().size());

        assertEquals(1, minimized.getFinalStates().size());

        assertTrue(minimized.isDeterministic());
    }

    @Test
    void nondeterministicAutomatonShouldBecomeDeterministic() {

        Set<String> states = new LinkedHashSet<>(
                Arrays.asList("A", "B", "C")
        );

        Set<String> alphabet = new LinkedHashSet<>(
                Arrays.asList("0", "1")
        );

        Map<String, Map<String, Set<String>>> transitions =
                new LinkedHashMap<>();

        transitions.put("A", Map.of(
                "0", Set.of("A", "B"),
                "1", Set.of("A")
        ));

        transitions.put("B", Map.of(
                "0", Set.of("C"),
                "1", Set.of("B")
        ));

        transitions.put("C", Map.of(
                "0", Set.of("C"),
                "1", Set.of("C")
        ));

        Automaton nfa = new Automaton(
                states,
                alphabet,
                "A",
                Set.of("C"),
                transitions
        );

        assertFalse(nfa.isDeterministic());

        Automaton dfa = Determinizer.determinize(nfa);

        assertTrue(dfa.isDeterministic());

        Automaton minimized = Minimizer.minimize(dfa);

        assertTrue(minimized.isDeterministic());

        assertTrue(
                minimized.getStates().contains(
                        minimized.getInitialState()
                )
        );
    }

    @Test
    void unreachableStatesShouldBeRemoved() {

        Map<String, Map<String, Set<String>>> transitions =
                new LinkedHashMap<>();

        transitions.put("A", Map.of(
                "0", Set.of("A"),
                "1", Set.of("B")
        ));

        transitions.put("B", Map.of(
                "0", Set.of("A"),
                "1", Set.of("B")
        ));

        transitions.put("C", Map.of(
                "0", Set.of("C"),
                "1", Set.of("C")
        ));

        Automaton automaton = new Automaton(
                new LinkedHashSet<>(Arrays.asList("A", "B", "C")),
                new LinkedHashSet<>(Arrays.asList("0", "1")),
                "A",
                Set.of("B"),
                transitions
        );

        Automaton minimized = Minimizer.minimize(automaton);

        assertEquals(2, minimized.getStates().size());
    }
}