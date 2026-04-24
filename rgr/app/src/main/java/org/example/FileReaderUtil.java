package org.example;

import java.io.*;
import java.util.*;

public class FileReaderUtil {
    public static Matrix readMatrix(InputStream is) throws Exception {
        List<Fraction[]> rows = new ArrayList<>();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;

        while ((line = br.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");
            Fraction[] row = new Fraction[parts.length];

            for (int i = 0; i < parts.length; i++) {
                row[i] = parseFraction(parts[i]);
            }

            rows.add(row);
        }

        int m = rows.size();
        int n = rows.get(0).length;

        Matrix matrix = new Matrix(m, n);

        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                matrix.set(i, j, rows.get(i)[j]);

        return matrix;
    }

    private static Fraction parseFraction(String s) {
        if (s.contains("/")) {
            String[] parts = s.split("/");
            return new Fraction(
                    Long.parseLong(parts[0]),
                    Long.parseLong(parts[1])
            );
        }
        return new Fraction(Long.parseLong(s));
    }
}

