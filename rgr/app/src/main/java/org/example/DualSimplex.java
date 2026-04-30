package org.example;

public class DualSimplex {

    private final Matrix table;
    private final int zRow;
    private static final boolean DEBUG = true;

    public DualSimplex(Matrix table) {
        this.table = table;
        this.zRow = table.getRows() - 1;

//        normalizeZRow();
    }

    public Result solve() {
        int step = 0;

        System.out.println("=== DUAL SIMPLEX START ===");

        if (!isDualFeasible()) {
            System.out.println("→ Таблица не является двойственно допустимой");
            return Result.noSolution();
        }

        while (true) {
            printTable("Step " + step);
            int row = findMostNegativeBRow();

            if (row == -1) {
                System.out.println("→ OPTIMAL");
                Result result = buildResult();

                // После достижения оптимальности проверить бесконечности
                if (checkInfiniteSolutions()) {
                    result.status = Result.Status.INFINITE;
                } else {
                    result.status = Result.Status.OPTIMAL;
                }
                return result;
            }

            int col = findDualPivotColumn(row);
            if (col == -1) {
                System.out.println("→ Нет допустимого разрешающего столбца");
                return Result.noSolution();
            }

            pivot(row, col);
            step++;
        }
    }

    private boolean isDualFeasible() {

        for (int j = 0; j < table.getCols() - 1; j++) {

            Fraction reducedCost = table.get(zRow, j);

            if (reducedCost.compareTo(Fraction.ZERO) < 0) {
                return false;
            }
        }

        return true;
    }

    private int findMostNegativeBRow() {
        int row = -1;
        Fraction min = Fraction.ZERO;
        for (int i = 0; i < table.getRows(); i++) {
            if (i == zRow) continue;
            Fraction b = table.get(i, table.getCols() - 1);
            if (b.compareTo(Fraction.ZERO) < 0) {
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

            if (a.compareTo(Fraction.ZERO) >= 0) continue; // только отрицательные

            Fraction reduced = table.get(zRow, j);

            // ratio = reduced / a (a < 0)
            Fraction ratio = reduced.div(a).abs();

            if (col == -1 || ratio.compareTo(best) < 0) {
                best = ratio;
                col = j;
            }
        }

        return col;
    }

    private void normalizeZRow() {
        for (int j = 0; j < table.getCols(); j++) {
            Fraction v = table.get(zRow, j);
            table.set(zRow, j, v.neg());
        }
    }

    private void pivot(int pr, int pc) {
        Fraction p = table.get(pr, pc);
        if (p.isZero()) {
            throw new RuntimeException("Pivot = 0");
        }
        System.out.println("Ведущая строка = " + pr);
        System.out.println("Ведущая коллонка = " + pc);
        System.out.println("Ведущий элемент = " + p);
        System.out.println("--> Нормализованная строка " + pr);
        for (int j = 0; j < table.getCols(); j++) {
            Fraction oldVal = table.get(pr, j);
            Fraction newVal = oldVal.div(p);
            if (DEBUG) {
                System.out.println("R" + pr + "[" + j + "]: " +
                        oldVal + " / " + p + " = " + newVal);
            }
            table.set(pr, j, newVal);
        }
        for (int i = 0; i < table.getRows(); i++) {
            if (i == pr) continue;
            Fraction factor = table.get(i, pc);
            if (factor.isZero()) continue;
            System.out.println("\n--> Приводим строку " + i + " используя множитель " + factor);
            for (int j = 0; j < table.getCols(); j++) {
                Fraction oldVal = table.get(i, j);
                Fraction pivotVal = table.get(pr, j);
                Fraction newVal = oldVal.sub(factor.mul(pivotVal));
                if (DEBUG) {
                    System.out.println(
                            "R" + i + "[" + j + "]: " +
                                    oldVal + " - (" + factor + " * " + pivotVal + ") = " + newVal
                    );
                }
                table.set(i, j, newVal);
            }
        }
    }

    private void printTable(String title) {
        System.out.println("\n=== " + title + " ===");
        int rows = table.getRows();
        int cols = table.getCols();
        System.out.print("б.п | ");
        for (int j = 0; j < cols - 1; j++) {
            System.out.printf("x%-4d", j + 1);
        }
        System.out.println("| 1");
        System.out.println("----+--------------------------------");
        for (int i = 0; i < rows; i++) {
            if (i == zRow) {
                System.out.print("Z   | ");
            } else {
                int basic = findBasicColumn(i);
                System.out.printf((basic != -1 ? "x%-3d| " : "?   | "), basic + 1);
            }
            for (int j = 0; j < cols; j++) {
                System.out.printf("%-6s", table.get(i, j));
            }
            System.out.println();
        }
    }

    private int findBasicColumn(int row) {
        for (int j = 0; j < table.getCols() - 1; j++) {
            if (!table.get(row, j).equals(Fraction.ONE)) continue;
            boolean ok = true;
            for (int i = 0; i < table.getRows(); i++) {
                if (i == row || i == zRow) continue;
                if (!table.get(i, j).isZero()) {
                    ok = false;
                    break;
                }
            }
            if (ok) return j;
        }
        return -1;
    }

    private Result buildResult() {

        Result r = new Result();

        int vars = table.getCols() - 1;
        r.solution = new Fraction[vars];

        boolean infinite = false;

        for (int j = 0; j < vars; j++) {

            int row = findBasicRow(j);

            if (row != -1) {
                r.solution[j] = table.get(row, table.getCols() - 1);
            } else {
                r.solution[j] = Fraction.ZERO;

                // правильный критерий:
                if (table.get(zRow, j).compareTo(Fraction.ZERO) == 0) {
                    infinite = true;
                }
            }
        }

        r.status = infinite
                ? Result.Status.INFINITE
                : Result.Status.OPTIMAL;

        return r;
    }

    private int findBasicRow(int col) {
        for (int i = 0; i < table.getRows(); i++) {
            if (i == zRow) continue;
            if (!table.get(i, col).equals(Fraction.ONE)) continue;
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

    /**
     * Проверяет, есть ли признаки бесконечности решений.
     * Если найдена переменная, которая не участвует в ограничениях или
     * её соответствующий столбец не содержит активных ограничений,
     * то решений бесконечно много.
     */
    private boolean checkInfiniteSolutions() {
        int vars = table.getCols() - 1;
        int rows = table.getRows();

        for (int j = 0; j < vars; j++) {
            int basicRow = findBasicRow(j);
            if (basicRow == -1) {
                // Переменная не находится в базисе, смотрим её столбец
                boolean allZeros = true;
                for (int i = 0; i < rows; i++) {
                    if (i == zRow) continue;
                    if (!table.get(i, j).isZero()) {
                        allZeros = false;
                        break;
                    }
                }
                // Если столбец свободен (все нули), значит переменная свободна
                // и решение бесконечно
                if (allZeros) {
                    System.out.println("Обнаружена свободная переменная, решение бесконечно");
                    return true;
                }
            }
        }
        return false;
    }
}
