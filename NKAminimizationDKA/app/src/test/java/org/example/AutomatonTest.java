package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.*;

class AutomatonTest {
    @TempDir Path temp;

    private static Set<String> ordered(String... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }

    private static Automaton machine(String[] states, String start, Set<String> finals,
            Map<String, Map<String, Set<String>>> edges) {
        return new Automaton(ordered(states), ordered("0", "1"), start, finals, edges);
    }

    // Independent NFA interpreter: keeps every possible current state.
    private static boolean accepts(Automaton a, String word) {
        Set<String> current = Set.of(a.getInitialState());
        for (int i = 0; i < word.length(); i++) {
            Set<String> next = new HashSet<>();
            for (String state : current) next.addAll(a.getTransitions(state, word.substring(i, i + 1)));
            current = next;
        }
        return !Collections.disjoint(current, a.getFinalStates());
    }

    private static void languages(Predicate<String> expected, Automaton... automata) {
        List<String> words = new ArrayList<>(List.of(""));
        for (int length = 0; length <= 8; length++) {
            List<String> next = new ArrayList<>();
            for (String word : words) {
                for (Automaton a : automata) assertEquals(expected.test(word), accepts(a, word), "word=" + word);
                next.add(word + "0");
                next.add(word + "1");
            }
            words = next;
        }
    }

    private static String target(Automaton a, String state, String symbol) {
        Set<String> destinations = a.getTransitions(state, symbol);
        assertEquals(1, destinations.size());
        return destinations.iterator().next();
    }

    @Test void lectureNfaHasExactOriginalTable() {
        Automaton a = App.createLectureExample();
        assertEquals(ordered("p", "q", "r"), a.getStates());
        assertEquals(Set.of("r"), a.getFinalStates());
        assertEquals("p", a.getInitialState());
        assertEquals(Set.of("p"), a.getTransitions("p", "0"));
        assertEquals(Set.of("p", "q"), a.getTransitions("p", "1"));
        assertEquals(Set.of(), a.getTransitions("q", "0"));
        assertEquals(Set.of("r"), a.getTransitions("q", "1"));
        assertEquals(Set.of(), a.getTransitions("r", "0"));
        assertEquals(Set.of(), a.getTransitions("r", "1"));
    }

    @Test void lectureDeterminizationHasExactTableAndPreservesLanguage() {
        Automaton nfa = App.createLectureExample();
        Automaton dfa = Determinizer.determinize(nfa);
        assertEquals(ordered("A", "B", "C"), dfa.getStates());
        assertEquals("A", dfa.getInitialState());
        assertEquals(Set.of("C"), dfa.getFinalStates());
        assertTrue(dfa.isDeterministic());
        for (String state : dfa.getStates()) assertEquals("A", target(dfa, state, "0"));
        assertEquals("B", target(dfa, "A", "1"));
        assertEquals("C", target(dfa, "B", "1"));
        assertEquals("C", target(dfa, "C", "1"));
        Automaton min = Minimizer.minimize(dfa);
        assertEquals(3, min.getStates().size());
        languages(w -> w.endsWith("11"), nfa, dfa, min);
    }

    @Test void sixStateLectureDfaHasCorrectQuotientAndLanguage() {
        String[][] rows = {{"q1","q2"},{"q4","q2"},{"q3","q0"},{"q5","q2"},{"q5","q5"},{"q4","q4"}};
        Map<String, Map<String, Set<String>>> edges = new LinkedHashMap<>();
        for (int i = 0; i < rows.length; i++) edges.put("q" + i, Map.of("0", Set.of(rows[i][0]), "1", Set.of(rows[i][1])));
        Automaton a = machine(new String[]{"q0","q1","q2","q3","q4","q5"}, "q0", Set.of("q4","q5"), edges);
        Automaton m = Minimizer.minimize(a);
        assertEquals(3, m.getStates().size());
        String start = m.getInitialState(), middle = target(m, start, "0"), end = target(m, middle, "0");
        assertEquals(Set.of(end), m.getFinalStates());
        assertEquals(start, target(m, start, "1"));
        assertEquals(start, target(m, middle, "1"));
        assertEquals(end, target(m, end, "0"));
        assertEquals(end, target(m, end, "1"));
        // Exact homomorphism checks the three expected equivalence classes.
        Map<String,String> quotient = Map.of("q0",start,"q2",start,"q1",middle,"q3",middle,"q4",end,"q5",end);
        for (String state : a.getStates()) for (String symbol : a.getAlphabet())
            assertEquals(quotient.get(target(a,state,symbol)), target(m,quotient.get(state),symbol));
        languages(w -> w.contains("00"), a, m);
    }

    @Test void missingTransitionMergesWithRejectingSink() {
        Automaton a = new Automaton(ordered("s","dead"), Set.of("0"), "s", Set.of(), Map.of("s",Map.of("0",Set.of("dead"))));
        Automaton m = Minimizer.minimize(a);
        assertEquals(1, m.getStates().size());
        assertEquals(m.getInitialState(), target(m,m.getInitialState(),"0"));
        languages(w -> false, a, m);
    }

    @Test void incompleteDfaDistinguishesAcceptingStateAndMergesExplicitDeadState() {
        Automaton a = machine(new String[]{"s","f","dead","Ошибка"}, "s", Set.of("f"), Map.of(
            "s",Map.of("0",Set.of("f"),"1",Set.of("dead")),
            "dead",Map.of("0",Set.of("dead"),"1",Set.of("dead"))));
        Automaton m = Minimizer.minimize(a);
        assertEquals(3,m.getStates().size());
        languages(w -> w.equals("0"),a,m);
    }

    @Test void unreachableAcceptingAndRejectingStatesAreRemoved() {
        Automaton a = machine(new String[]{"s","f","unreachable"},"s",Set.of("f","unreachable"),Map.of(
            "s",Map.of("0",Set.of("s"),"1",Set.of("f")),
            "f",Map.of("0",Set.of("s"),"1",Set.of("f"))));
        Automaton m = Minimizer.minimize(a);
        assertEquals(2,m.getStates().size());
        languages(w -> w.endsWith("1"),a,m);
    }

    @Test void acceptingInitialStateAndMultipleFinalsPreserveEmptyWord() {
        Automaton a = machine(new String[]{"s","f"},"s",Set.of("s","f"),Map.of(
            "s",Map.of("0",Set.of("s","f"),"1",Set.of("f")),
            "f",Map.of("0",Set.of("f"),"1",Set.of("f"))));
        Automaton d = Determinizer.determinize(a), m = Minimizer.minimize(d);
        assertTrue(accepts(m,""));
        assertEquals(1,m.getStates().size());
        languages(w -> true,a,d,m);
    }

    @Test void emptyTransitionsDoNotCreateSubsetState() {
        Automaton a = machine(new String[]{"s"},"s",Set.of("s"),Map.of());
        Automaton d = Determinizer.determinize(a);
        assertEquals(1,d.getStates().size());
        assertTrue(d.getTransitions(d.getInitialState(),"0").isEmpty());
        languages(String::isEmpty,a,d,Minimizer.minimize(d));
    }

    @Test void determinizationVisitsOnlyReachableSubsets() {
        Set<String> states = new LinkedHashSet<>();
        for (int i=0;i<40;i++) states.add("q"+i);
        Automaton a = new Automaton(states,Set.of("0"),"q0",Set.of(),Map.of("q0",Map.of("0",Set.of("q0","q1"))));
        assertTimeout(Duration.ofSeconds(3), () -> assertEquals(2,Determinizer.determinize(a).getStates().size()));
    }

    @Test void minimizationRejectsNondeterminism() {
        assertThrows(IllegalArgumentException.class,()->Minimizer.minimize(App.createLectureExample()));
    }

    @Test void rejectsUnknownInitialState() {
        assertThrows(IllegalArgumentException.class,()->machine(new String[]{"s"},"x",Set.of(),Map.of()));
    }
    @Test void rejectsUnknownFinalState() {
        assertThrows(IllegalArgumentException.class,()->machine(new String[]{"s"},"s",Set.of("x"),Map.of()));
    }
    @Test void rejectsUnknownSourceBeforeNormalization() {
        var error = assertThrows(IllegalArgumentException.class,()->machine(new String[]{"s"},"s",Set.of(),Map.of("x",Map.of())));
        assertTrue(error.getMessage().contains("x"));
    }
    @Test void rejectsUnknownSymbolBeforeNormalization() {
        var error = assertThrows(IllegalArgumentException.class,()->machine(new String[]{"s"},"s",Set.of(),Map.of("s",Map.of("x",Set.of("s")))));
        assertTrue(error.getMessage().contains("x"));
    }
    @Test void rejectsUnknownDestination() {
        assertThrows(IllegalArgumentException.class,()->machine(new String[]{"s"},"s",Set.of(),Map.of("s",Map.of("0",Set.of("x")))));
    }
    @Test void rejectsNullCollectionsRowsAndDestinations() {
        assertThrows(IllegalArgumentException.class,()->new Automaton(null,Set.of("0"),"s",Set.of(),Map.of()));
        Map<String,Map<String,Set<String>>> rows = new HashMap<>(); rows.put("s",null);
        assertThrows(IllegalArgumentException.class,()->machine(new String[]{"s"},"s",Set.of(),rows));
        Map<String,Set<String>> row = new HashMap<>(); row.put("0",null); rows.put("s",row);
        assertThrows(IllegalArgumentException.class,()->machine(new String[]{"s"},"s",Set.of(),rows));
    }
    @Test void rejectsNullNamesInTransitionTable() {
        Map<String, Map<String, Set<String>>> edges = new HashMap<>();
        edges.put(null, Map.of());
        assertThrows(IllegalArgumentException.class, () -> machine(new String[]{"s"}, "s", Set.of(), edges));
        edges.clear();
        Map<String, Set<String>> row = new HashMap<>(); row.put(null, Set.of("s")); edges.put("s", row);
        assertThrows(IllegalArgumentException.class, () -> machine(new String[]{"s"}, "s", Set.of(), edges));
        row.clear(); row.put("0", new HashSet<>(Arrays.asList((String) null)));
        assertThrows(IllegalArgumentException.class, () -> machine(new String[]{"s"}, "s", Set.of(), edges));
    }

    @Test void rejectsEmptyStatesAndAlphabet() {
        assertThrows(IllegalArgumentException.class,()->machine(new String[]{},"s",Set.of(),Map.of()));
        assertThrows(IllegalArgumentException.class,()->new Automaton(Set.of("s"),Set.of(),"s",Set.of(),Map.of()));
    }
    @Test void constructorCopiesCollectionsAndGettersAreUnmodifiable() {
        Set<String> states=ordered("s"), alphabet=ordered("0"), finals=ordered("s"), destination=ordered("s");
        Map<String,Set<String>> row = new HashMap<>(); row.put("0",destination);
        Map<String,Map<String,Set<String>>> edges=new HashMap<>(); edges.put("s",row);
        Automaton a=new Automaton(states,alphabet,"s",finals,edges);
        states.clear(); alphabet.clear(); finals.clear(); destination.clear(); row.clear(); edges.clear();
        assertEquals(Set.of("s"),a.getStates()); assertEquals(Set.of("0"),a.getAlphabet());
        assertEquals(Set.of("s"),a.getFinalStates()); assertEquals(Set.of("s"),a.getTransitions("s","0"));
        assertThrows(UnsupportedOperationException.class,()->a.getStates().clear());
        assertThrows(UnsupportedOperationException.class,()->a.getAlphabet().clear());
        assertThrows(UnsupportedOperationException.class,()->a.getFinalStates().clear());
        assertThrows(UnsupportedOperationException.class,()->a.getTransitions("s","0").clear());
    }
    private static List<Object> snapshot(Automaton a) {
        List<Object> result=new ArrayList<>(List.of(a.getStates(),a.getAlphabet(),a.getInitialState(),a.getFinalStates()));
        for(String state:a.getStates()) for(String symbol:a.getAlphabet()) result.add(Set.copyOf(a.getTransitions(state,symbol)));
        return result;
    }
    @Test void transformationsDoNotMutateOriginals() {
        Automaton n=App.createLectureExample(); var before=snapshot(n);
        Automaton d=Determinizer.determinize(n); var dBefore=snapshot(d);
        Minimizer.minimize(d);
        assertEquals(before,snapshot(n)); assertEquals(dBefore,snapshot(d));
    }
    @Test void dotEscapesNamesAndMarksInitialAndFinalStates() throws Exception {
        String state="q\"\\\n\r\t";
        Automaton a=new Automaton(ordered(state,"__start"),Set.of("\"\\\n"),state,Set.of(state),Map.of(state,Map.of("\"\\\n",Set.of("__start"))));
        Path dot=GraphPrinter.writeDot(a,"Тест \"\n",temp);
        String text=Files.readString(dot);
        assertTrue(text.contains("\"__start_\" [shape=point"));
        assertTrue(text.contains("shape=doublecircle"));
        assertTrue(text.contains("q\\\"\\\\\\n\\r\\t"));
        assertTrue(text.contains("[label=\"\\\"\\\\\\n\"]"));
        assertTrue(text.contains("\"__start_\" -> \"q"));
    }
}
