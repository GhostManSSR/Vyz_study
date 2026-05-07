package org.example;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class App {

    public static void main(String[] args) throws Exception {

        System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));

        InputStream is = App.class
                .getClassLoader()
                .getResourceAsStream("input_4.txt");

        if (is == null) {
            throw new RuntimeException("input.txt not found in resources");
        }

        Matrix matrix = FileReaderUtil.readMatrix(is);

        DualSimplex solver = new DualSimplex(matrix);
        Result result = solver.solve();

        result.print();
    }
}
