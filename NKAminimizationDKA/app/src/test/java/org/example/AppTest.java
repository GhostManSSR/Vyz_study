package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @TempDir Path temp;

    private String runApp(String input) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classes = Path.of(App.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        Path output = temp.resolve("console.txt");
        Process process = new ProcessBuilder(java, "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8", "-cp", classes, "org.example.App")
                .directory(temp.toFile()).redirectErrorStream(true).redirectOutput(output.toFile()).start();
        try {
            try (var stdin = process.getOutputStream()) { stdin.write(input.getBytes(StandardCharsets.UTF_8)); }
            assertTrue(process.waitFor(15, TimeUnit.SECONDS), "Приложение не завершилось");
            String text = Files.readString(output);
            assertEquals(0, process.exitValue(), text);
            for (String name : new String[]{"исходный_нка", "дка_перед_минимизацией", "минимизированный_дка"}) {
                String dot = Files.readString(temp.resolve("graphs").resolve(name + ".dot"));
                assertTrue(dot.contains("shape=point"));
            }
            return text;
        } finally {
            process.destroyForcibly();
        }
    }

    @Test void manualInputRunsEntirePipeline() throws Exception {
        assertTrue(runApp("2\ns,dead\n0\ns\n-\ndead\n-\n")
                .contains("Количество состояний минимального ДКА: 1"));
    }

    @Test void lectureExampleRunsEntirePipeline() throws Exception {
        assertTrue(runApp("1\n").contains("Количество состояний минимального ДКА: 3"));
    }
}
