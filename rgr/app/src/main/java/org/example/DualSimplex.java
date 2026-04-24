package org.example;

public class DualSimplex {

    private final Matrix table;
    private int zRow;

    public DualSimplex(Matrix table) {
        this.table = table;
        this.zRow = table.getRows() - 1;
    }

    public Result solve() {

        int step = 0;

        System.out.println("=== START ===");

        normalizeZRow();

        // =========================
        // DUAL SIMPLEX
        // =========================
        while (true) {

            printTable("Dual Simplex Step " + step);

            int row = findMostNegativeBRow();

            if (row == -1) {
                System.out.println("→ Все b >= 0 → допустимый базис найден");
                break;
            }

            int col = findDualPivotColumn(row);

            if (col == -1) {
                return Result.noSolution();
            }

            pivot(row, col);
            step++;
        }

        System.out.println("\n=== PRIMAL SIMPLEX ===");

        step = 0;

        // =========================
        // PRIMAL SIMPLEX
        // =========================
        while (true) {

            printTable("Primal Simplex Step " + step);

            int col = findPrimalPivotColumn();

            if (col == -1) {
                System.out.println("→ OPTIMUM");
                break;
            }

            int row = findPrimalPivotRow(col);

            if (row == -1) {
                return Result.noSolution();
            }

            pivot(row, col);
            step++;
        }

        return buildResult();
    }

    // =========================
    // Z
    // =========================
    private void normalizeZRow() {
        zRow = table.getRows() - 1;
    }

    // =========================
    // DUAL SIMPLEX
    // =========================
    private int findMostNegativeBRow() {

        int row = -1;
        Fraction min = new Fraction(0);

        for (int i = 0; i < table.getRows(); i++) {

            if (i == zRow) continue;

            Fraction b = table.get(i, table.getCols() - 1);

            if (b.compareTo(new Fraction(0)) < 0) {
                if (row == -1 || b.compareTo(min) < 0) {
                    min = b;
                    row = i;
                }
            }
        }
        return row;
    }

    private int findDualPivotColumn(int row) {

        int col = -1;
        Fraction best = null;

        for (int j = 0; j < table.getCols() - 1; j++) {

            Fraction a = table.get(row, j);

            if (a.compareTo(new Fraction(0)) < 0) {

                Fraction ratio = table.get(zRow, j).div(a);

                if (col == -1 || best == null || ratio.compareTo(best) < 0) {
                    best = ratio;
                    col = j;
                }
            }
        }

        return col;
    }

    // =========================
    // PRIMAL SIMPLEX
    // =========================
    private int findPrimalPivotColumn() {

        int col = -1;
        Fraction min = null;

        for (int j = 0; j < table.getCols() - 1; j++) {

            Fraction c = table.get(zRow, j);

            if (c.compareTo(new Fraction(0)) < 0) {

                if (col == -1 || min == null || c.compareTo(min) < 0) {
                    min = c;
                    col = j;
                }
            }
        }

        return col;
    }

    private int findPrimalPivotRow(int col) {

        int row = -1;
        Fraction best = null;

        for (int i = 0; i < table.getRows(); i++) {

            if (i == zRow) continue;

            Fraction a = table.get(i, col);

            if (a.compareTo(new Fraction(0)) > 0) {

                Fraction ratio = table.get(i, table.getCols() - 1).div(a);

                if (row == -1 || best == null || ratio.compareTo(best) < 0) {
                    best = ratio;
                    row = i;
                }
            }
        }

        return row;
    }

    // =========================
    // PIVOT (ИСПРАВЛЕН + Z ОБНОВЛЯЕТСЯ)
    // =========================
    private void pivot(int pr, int pc) {

        Fraction p = table.get(pr, pc);

        if (p.isZero()) {
            throw new RuntimeException("Pivot = 0");
        }

        // нормализация pivot строки
        for (int j = 0; j < table.getCols(); j++) {
            table.set(pr, j, table.get(pr, j).div(p));
        }

        // остальные строки включая Z
        for (int i = 0; i < table.getRows(); i++) {

            if (i == pr) continue;

            Fraction factor = table.get(i, pc);

            if (factor.isZero()) continue;

            for (int j = 0; j < table.getCols(); j++) {

                Fraction newVal =
                        table.get(i, j)
                                .sub(factor.mul(table.get(pr, j)));

                table.set(i, j, newVal);
            }
        }
    }

    // =========================
    // Z correction (КЛЮЧЕВАЯ ЧАСТЬ)
    // =========================
    private void recalculateZ() {

        // пересчёт Z как линейной комбинации ограничений
        for (int i = 0; i < table.getRows(); i++) {

            if (i == zRow) continue;

            Fraction factor = table.get(i, zRow);

            for (int j = 0; j < table.getCols(); j++) {

                table.set(zRow, j,
                        table.get(zRow, j)
                                .sub(factor.mul(table.get(i, j))));
            }
        }
    }

    // =========================
    // PRINT
    // =========================
    private void printTable(String title) {

        System.out.println("\n=== " + title + " ===");

        for (int i = 0; i < table.getRows(); i++) {

            if (i == zRow)
                System.out.print("Z  | ");
            else
                System.out.print("R" + i + " | ");

            for (int j = 0; j < table.getCols(); j++) {
                System.out.print(table.get(i, j) + "\t");
            }
            System.out.println();
        }
    }

    // =========================
    // RESULT
    // =========================
    private Result buildResult() {

        Result r = new Result();

        int cols = table.getCols();

        r.solution = new Fraction[cols - 1];

        for (int j = 0; j < cols - 1; j++) {

            int row = findBasicRow(j);

            if (row != -1)
                r.solution[j] = table.get(row, cols - 1);
            else
                r.solution[j] = new Fraction(0);
        }

        r.status = Result.Status.OPTIMAL;
        return r;
    }

    private int findBasicRow(int col) {

        int rowIndex = -1;

        for (int i = 0; i < table.getRows(); i++) {

            if (i == zRow) continue;

            if (!table.get(i, col).equals(new Fraction(1))) continue;

            boolean ok = true;

            for (int k = 0; k < table.getRows(); k++) {

                if (k == i || k == zRow) continue;

                if (!table.get(k, col).isZero()) {
                    ok = false;
                    break;
                }
            }

            if (ok) return i;
        }

        return -1;
    }
}
