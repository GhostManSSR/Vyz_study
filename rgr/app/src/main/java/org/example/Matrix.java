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

    public void print() {
        for (Fraction[] row : data) {
            for (Fraction val : row) {
                System.out.print(val + "\t");
            }
            System.out.println();
        }
    }
}
