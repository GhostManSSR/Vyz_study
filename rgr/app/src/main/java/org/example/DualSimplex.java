package org.example;

import java.util.*;

public class DualSimplex {

    private final Matrix table;
    private final int m;
    private final int n;
    private final int zRow;
    private final int[] basis;

    private static final int NOT_SET = -1;
    private static final boolean DEBUG = true;

    public DualSimplex(Matrix table) {
        this.table = table;
        this.m = table.getRows() - 1;
        this.n = table.getCols() - 1;
        this.zRow = m;
        this.basis = new int[m];

        Arrays.fill(basis, NOT_SET);
        findInitialBasis();

        printTable("Входные данные", null);
    }

    // ================= SOLVE =================

    public Result solve() {

        System.out.println("=== Двойственный симплекс-метод ===");

        if (!isDualFeasible()) {
            System.out.println("→ Таблица не является двойственно допустимой");
            return Result.noSolution();
        }

        int step = 0;

        while (true) {

            int pivotRow = findPivotRow();

            // ===== OPTIMAL =====
            if (pivotRow == -1) {

                printTable("Финальная таблица", null);

                System.out.println("\n→ Решение оптимально");

                Fraction[] x = analyzeSolution();
                Fraction z = table.get(zRow, n);

                int alt = findAlternativeEnteringVariable();

                if (alt != -1) {
                    System.out.println("Найден альтернативный оптимум");
                    return buildLambdaSolution();
                }

                return Result.optimal(x, z);
            }

            Fraction[] ratios = calculateRatios(pivotRow);

            printTable("Шаг " + (step + 1), ratios);

            int pivotCol = findPivotColumn(ratios);

            // ===== UNBOUNDED =====
            if (pivotCol == -1) {

                PivotAnalysis info = analyzePivotFailure(pivotRow);

                if (!info.hasNegative) {
                    System.out.println("В разрешающей строке нет отрицательных значений");
                    return Result.throwsSolution();
                }

                if (!info.hasPositive) {

                    System.out.println("Причина: в pivot-строке нет допустимых направлений роста");
                    System.out.println("Все коэффициенты ≤ 0 → невозможно выполнить шаг улучшения");
                    System.out.println("➡ Система несовместна (INFEASIBLE)");

                    return Result.throwsSolution();
                }

                boolean hasNegative = false;

                for (int j = 0; j < n; j++) {
                    if (table.get(pivotRow, j).compareTo(Fraction.ZERO) < 0) {
                        hasNegative = true;
                        break;
                    }
                }

                if (!hasNegative) {
                    System.out.println("➡ Все коэффициенты в строке ≥ 0");
                    System.out.println("➡ Целевая функция НЕОГРАНИЧЕНА (UNBOUNDED)");
                    return Result.unbounded();
                }

                // =========================
                // MIXED CASE (самый важный)
                // =========================
                System.out.println("Причина: отсутствуют допустимые симплекс-отношения");
                System.out.println("Все ratio = null → нет допустимого базиса");
                System.out.println("➡ Ошибка постановки задачи / вырожденность");

                return Result.noSolution();
            }

            System.out.println("\nРазрешающий элемент: " +
                    table.get(pivotRow, pivotCol) +
                    " (row " + (pivotRow + 1) +
                    ", col x" + (pivotCol + 1) + ")");

            pivot(pivotRow, pivotCol);

            step++;
        }
    }

    private Result buildLambdaSolution() {

        Fraction[] x1 = snapshotSolution();
        Fraction z = table.get(zRow, n);

        int entering = -1;

        // 1. ищем альтернативную переменную
        for (int j = 0; j < n; j++) {

            if (table.get(zRow, j).isZero() && findBasisRow(j) == -1) {

                boolean hasPositive = false;

                for (int i = 0; i < m; i++) {
                    if (table.get(i, j).compareTo(Fraction.ZERO) > 0) {
                        hasPositive = true;
                        break;
                    }
                }

                if (hasPositive) {
                    entering = j;
                    break;
                }
            }
        }

        if (entering == -1) {
            return Result.optimal(x1, z);
        }

        // 2. сохраняем состояние
        Matrix backup = copyMatrix(table);
        int[] basisBackup = Arrays.copyOf(basis, basis.length);

        // 3. ищем pivot row (ratio test)
        int pivotRow = -1;
        Fraction best = null;

        for (int i = 0; i < m; i++) {

            Fraction a = table.get(i, entering);

            if (a.compareTo(Fraction.ZERO) > 0) {

                Fraction ratio = table.get(i, n).div(a);

                if (pivotRow == -1 || ratio.compareTo(best) < 0) {
                    best = ratio;
                    pivotRow = i;
                }
            }
        }

        if (pivotRow == -1) {
            restoreMatrix(table, backup);
            return Result.optimal(x1, z);
        }

        // 4. делаем pivot
        pivot(pivotRow, entering);

        System.out.println("\n=== Таблица состояния (X2) ===");
        printTable("X2 состояние", null);

        Fraction[] x2 = snapshotSolution();

        // 5. откат ВСЕГО состояния
        restoreMatrix(table, backup);
        System.arraycopy(basisBackup, 0, basis, 0, basis.length);

        int[] freeVars = new int[]{entering};

        return Result.infinite(x1, x2, freeVars, z);
    }

    private Matrix copyMatrix(Matrix src) {

        Matrix copy = new Matrix(src.getRows(), src.getCols());

        for (int i = 0; i < src.getRows(); i++) {
            for (int j = 0; j < src.getCols(); j++) {
                copy.set(i, j, src.get(i, j));
            }
        }

        return copy;
    }

    private void restoreMatrix(Matrix target, Matrix src) {

        for (int i = 0; i < src.getRows(); i++) {
            for (int j = 0; j < src.getCols(); j++) {
                target.set(i, j, src.get(i, j));
            }
        }
    }

    private PivotAnalysis analyzePivotFailure(int row) {

        boolean hasNegativeCoeff = false;
        boolean hasPositiveCoeff = false;

        for (int j = 0; j < n; j++) {

            Fraction a = table.get(row, j);

            if (a.compareTo(Fraction.ZERO) < 0) {
                hasNegativeCoeff = true;
            } else if (a.compareTo(Fraction.ZERO) > 0) {
                hasPositiveCoeff = true;
            }
        }

        return new PivotAnalysis(hasNegativeCoeff, hasPositiveCoeff);
    }

    private Fraction[] snapshotSolution() {

        Fraction[] res = new Fraction[n];

        for (int j = 0; j < n; j++) {

            int row = findBasisRow(j);

            res[j] = (row != -1)
                    ? table.get(row, n)
                    : Fraction.ZERO;
        }

        return res;
    }

    // ================= FEASIBILITY =================

    private boolean isDualFeasible() {

        boolean ok = true;

        System.out.println("\n=== Проверка двойственной допустимости ===");

        for (int j = 0; j < n; j++) {

            Fraction val = table.get(zRow, j);

            if (val.compareTo(Fraction.ZERO) < 0) {

                System.out.println("Нарушение: Z[" + (j + 1) + "] = " + val +
                        " < 0 (переменная x" + (j + 1) + ")");

                ok = false;
            }
        }

        if (ok) {
            System.out.println("Таблица двойственно допустима");
        } else {
            System.out.println("Таблица НЕ является двойственно допустимой");
            System.out.println("Причина: в Z-строке присутствуют отрицательные коэффициенты.");
        }

        return ok;
    }

    // ================= PIVOT ROW =================

    private int findPivotRow() {

        int row = -1;
        Fraction min = Fraction.ZERO;

        for (int i = 0; i < m; i++) {

            Fraction b = table.get(i, n);

            if (b.compareTo(Fraction.ZERO) < 0) {
                if (row == -1 || b.compareTo(min) < 0) {
                    min = b;
                    row = i;
                }
            }
        }

        return row;
    }

    // ================= RATIOS =================

    private Fraction[] calculateRatios(int row) {

        Fraction[] ratios = new Fraction[n];

        for (int j = 0; j < n; j++) {

            ratios[j] = null;

            Fraction a = table.get(row, j);

            if (a.compareTo(Fraction.ZERO) < 0) {

                Fraction z = table.get(zRow, j);
                ratios[j] = z.abs().div(a.abs());
            }
        }

        return ratios;
    }

    private int findPivotColumn(Fraction[] ratios) {

        int col = -1;
        Fraction best = null;

        for (int j = 0; j < n; j++) {

            if (ratios[j] == null) continue;

            if (col == -1 || ratios[j].compareTo(best) < 0) {
                best = ratios[j];
                col = j;
            }
        }

        return col;
    }

    // ================= PIVOT =================

    private void pivot(int pr, int pc) {

        Fraction p = table.get(pr, pc);

        System.out.println("\n--> Нормализация строки " + (pr + 1));

        for (int j = 0; j <= n; j++) {

            Fraction oldVal = table.get(pr, j);
            Fraction newVal = oldVal.div(p);

            if (DEBUG) {
                System.out.println("R" + pr + "[" + j + "]: " +
                        oldVal + " / " + p + " = " + newVal);
            }

            table.set(pr, j, newVal);
        }

        for (int i = 0; i <= m; i++) {

            if (i == pr) continue;

            Fraction factor = table.get(i, pc);

            if (factor.isZero()) continue;

            System.out.println("\n--> Обнуляем коэффицент в строке " + (i + 1) +
                    " (множитель = " + factor + ")");

            for (int j = 0; j <= n; j++) {

                Fraction oldVal = table.get(i, j);
                Fraction pivotVal = table.get(pr, j);

                Fraction newVal = oldVal.sub(factor.mul(pivotVal));

                if (DEBUG) {
                    System.out.println("R" + i + "[" + j + "]: " +
                            oldVal + " - (" + factor + " * " +
                            pivotVal + ") = " + newVal);
                }

                table.set(i, j, newVal);
            }
        }

        basis[pr] = pc;
    }

    private int findAlternativeEnteringVariable() {

        for (int j = 0; j < n; j++) {

            // reduced cost = 0
            if (!table.get(zRow, j).isZero()) continue;

            // НЕ базисная переменная
            if (findBasisRow(j) != -1) continue;

            // ОБЯЗАТЕЛЬНО: есть допустимое направление
            boolean hasPositive = false;

            for (int i = 0; i < m; i++) {
                if (table.get(i, j).compareTo(Fraction.ZERO) > 0) {
                    hasPositive = true;
                    break;
                }
            }

            if (hasPositive) return j;
        }

        return -1;
    }

    // ================= BASIS =================

    private void findInitialBasis() {

        for (int i = 0; i < m; i++) basis[i] = NOT_SET;

        for (int j = 0; j < n; j++) {

            int oneRow = -1;
            boolean ok = true;

            for (int i = 0; i < m; i++) {

                Fraction v = table.get(i, j);

                if (v.equals(Fraction.ONE)) {

                    if (oneRow == -1) oneRow = i;
                    else ok = false;

                } else if (!v.isZero()) {
                    ok = false;
                }
            }

            if (ok && oneRow != -1 &&
                    table.get(zRow, j).isZero()) {
                basis[oneRow] = j;
            }
        }
    }

    // ================= OUTPUT =================

    private void printTable(String title, Fraction[] ratios) {

        System.out.println("\n=== " + title + " ===");

        int rows = table.getRows();
        int cols = table.getCols();

        int cellW = 7;

        String sep = "-----------------------------------------------------------";

        System.out.println(sep);

        // ===== HEADER =====
        System.out.printf("| %-4s | %-5s |", "БП", "1");

        for (int j = 0; j < n; j++) {
            System.out.printf(" x%-4d |", j + 1);
        }

        System.out.println();
        System.out.println(sep);

        // ===== BODY =====
        for (int i = 0; i < rows; i++) {

            if (i == zRow) {
                System.out.printf("| %-4s | %-5s |", "Z", table.get(i, n));
            } else {
                int b = basis[i];
                String name = (b != NOT_SET) ? "x" + (b + 1) : "?";

                System.out.printf("| %-4s | %-5s |",
                        name, table.get(i, n));
            }

            for (int j = 0; j < n; j++) {
                System.out.printf(" %-6s|", table.get(i, j));
            }

            System.out.println();
        }

        System.out.println(sep);

        // ===== CO ROW =====
        if (ratios != null) {

            System.out.printf("| %-4s | %-5s |", "CO", "-");

            for (int j = 0; j < n; j++) {

                if (ratios[j] != null) {
                    System.out.printf(" %-6s|", ratios[j]);
                } else {
                    System.out.printf(" %-6s|", "-");
                }
            }

            System.out.println();
            System.out.println(sep);
        }
        else{
            System.out.printf("| %-4s | %-5s |", "CO", "-");

            for (int j = 0; j < n; j++) {
                    System.out.printf(" %-6s|", "-");
            }
            System.out.println();
            System.out.println(sep);
        }
    }

    private void printRatios(Fraction[] ratios) {

        System.out.println("\nСимплекс-отношения:");

        for (int j = 0; j < ratios.length; j++) {

            if (ratios[j] != null) {
                System.out.println("x" + (j + 1) + " = " + ratios[j]);
            }
        }
    }

    private void printResult() {

        System.out.println("\nТекущее решение:");

        for (int j = 0; j < n; j++) {

            int row = findBasisRow(j);

            if (row != -1) {
                System.out.println("x" + (j + 1) + " = " +
                        table.get(row, n));
            } else {
                System.out.println("x" + (j + 1) + " = 0");
            }
        }

        System.out.println("Z = " + table.get(zRow, n));
    }

    // ================= ANALYZE =================

    private Fraction[] analyzeSolution() {

        Fraction[] r = new Fraction[n];

        for (int j = 0; j < n; j++) {
            int row = findBasisRow(j);
            r[j] = (row != -1) ? table.get(row, n) : Fraction.ZERO;
        }

        return r;
    }

    private int findBasisRow(int col) {

        for (int i = 0; i < m; i++) {
            if (basis[i] == col) return i;
        }

        return -1;
    }
}