package org.example;

public class Matrix {
    private Fraction[][] data;

    public Matrix(int rows, int cols) {
        data = new Fraction[rows][cols];
    }

    public int getRows() {
        return data.length;
    }

    public int getCols() {
        return data[0].length;
    }

    public Fraction get(int i, int j) {
        return data[i][j];
    }

    public void set(int i, int j, Fraction val) {
        data[i][j] = val;
    }

    public void swapRows(int r1, int r2) {
        Fraction[] temp = data[r1];
        data[r1] = data[r2];
        data[r2] = temp;
    }

    public void printMatrix(Matrix table, int[] basis, int zRow) {

        int m = table.getRows();
        int n = table.getCols() - 1;

        String sep = "-----------------------------------------------------------";

        System.out.println(sep);

        // ===== HEADER =====
        System.out.print("| БП   | 1     |");

        for (int j = 0; j < n; j++) {
            System.out.printf(" x%-4d |", j + 1);
        }

        System.out.println();
        System.out.println(sep);

        for (int i = 0; i < m; i++) {

            if (i == zRow) {
                System.out.printf("| %-4s | %-5s |", "Z", table.get(i, n));
            } else {
                int b = basis[i];
                String name = (b == -1) ? "?" : "x" + (b + 1);

                System.out.printf("| %-4s | %-5s |", name, table.get(i, n));
            }

            for (int j = 0; j < n; j++) {
                System.out.printf(" %-5s |", table.get(i, j));
            }

            System.out.println();
        }

        System.out.println(sep);

        System.out.print("| CO   | -     |");

        for (int j = 0; j < n; j++) {
                System.out.print(" -     |");
        }

        System.out.println();
        System.out.println(sep);
    }
}
