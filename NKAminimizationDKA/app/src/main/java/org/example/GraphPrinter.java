package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class GraphPrinter {

    public static void printGraph(Automaton automaton, String title) {
        writeDot(automaton, title);
    }

    static void writeDot(
            Automaton automaton,
            String title
    ) {
        writeDot(automaton, title, Paths.get("graphs"));
    }

    static Path writeDot(Automaton automaton, String title, Path directory) {
        try {
            Files.createDirectories(directory);

            String fileName = title
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-zа-яё0-9_-]", "_");

            Path file = directory.resolve(fileName + ".dot");

            StringBuilder dot = new StringBuilder();

            dot.append("digraph Automaton {\n");
            dot.append("    rankdir=LR;\n");
            dot.append("    graph [label=\"")
                    .append(escape(title))
                    .append("\", labelloc=t];\n");
            dot.append("    node [shape=circle];\n");

            String startMarker = "__start";
            while (automaton.getStates().contains(startMarker)) startMarker += "_";
            dot.append("    \"").append(escape(startMarker)).append("\" [shape=point, label=\"\"];\n");
            dot.append("    \"").append(escape(startMarker)).append("\" -> \"")
                    .append(escape(automaton.getInitialState())).append("\";\n");

            for (String state : automaton.getStates()) {
                dot.append("    \"")
                        .append(escape(state))
                        .append("\" [label=\"")
                        .append(escape(state))
                        .append("\"");

                if (automaton.getFinalStates().contains(state)) {
                    dot.append(", shape=doublecircle");
                }

                dot.append("];\n");
            }

            for (String state : automaton.getStates()) {
                for (String symbol : automaton.getAlphabet()) {
                    for (String destination :
                            automaton.getTransitions(state, symbol)) {

                        dot.append("    \"")
                                .append(escape(state))
                                .append("\" -> \"")
                                .append(escape(destination))
                                .append("\" [label=\"")
                                .append(escape(symbol))
                                .append("\"];\n");
                    }
                }
            }

            dot.append("}\n");

            Files.writeString(
                    file,
                    dot.toString(),
                    StandardCharsets.UTF_8
            );

            return file;

        } catch (IOException e) {
            throw new java.io.UncheckedIOException("Не удалось сохранить граф", e);
        }
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
