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

    public Result solve() {

        System.out.println("=== Двойственный симплекс-метод===");

        if (!isDualFeasible()) {
            System.out.println("→ начальная таблица не двойственно допустима");
            return Result.noSolution();
        }

        int step = 0;

        while (true) {
            System.out.println("\n==============================");
            System.out.println("ШАГ " + (step + 1));
            System.out.println("==============================");

            int pivotRow = findPivotRow();

            if (pivotRow == -1) {

                printTable("Финальная таблица", null);

                System.out.println("\n→ Решение оптимально");

                Fraction[] x = analyzeSolution();
                Fraction z = table.get(zRow, n);

                boolean hasAlternative = false;

                for (int j = 0; j < n; j++) {

                    if (!table.get(zRow, j).isZero() || findBasisRow(j) != -1)
                        continue;

                    boolean canPivot = false;

                    for (int i = 0; i < m; i++) {
                        if (table.get(i, j).compareTo(Fraction.ZERO) > 0) {
                            canPivot = true;
                            break;
                        }
                    }

                    if (!canPivot) {
                        System.out.print("X = (");
                        for (int i = 0; i < x.length; i++) {
                            System.out.print(x[i]);
                            if (i != x.length - 1) System.out.print(", ");
                        }
                        System.out.println(")");

                        System.out.println("Z = " + z);
                        System.out.printf("ЗЛП имеет бесконечно много решений");
                        System.out.println("\n→ найден луч (rc = 0, но нет a_ij > 0)");
                        System.out.printf("Альтернативынй оптимум возникает из-за нулевой оценки у не базисной переменной");
                        System.out.printf("\nНевозможно найти вторую вершину");
                        return Result.unbounded();
                    }

                    hasAlternative = true;
                }

                if (hasAlternative) {
                    System.out.println("→ существует альтернативный оптимум");
                    return buildLambdaSolution();
                }

                return Result.optimal(x, z);
            }

            Fraction[] ratiosCurrent = calculateRatios(pivotRow);
            printTable("Текущая таблица", ratiosCurrent);

            boolean hasNegativeCoeff = false;

            for (int j = 0; j < n; j++) {
                if (table.get(pivotRow, j).compareTo(Fraction.ZERO) < 0) {
                    hasNegativeCoeff = true;
                    break;
                }
            }

            if (!hasNegativeCoeff) {
                System.out.println("➡ b < 0 нет отрицательных коэффициентов в строке");
                return Result.noSolution();
            }

            Fraction[] ratios = new Fraction[n];

            for (int j = 0; j < n; j++) {

                Fraction a = table.get(pivotRow, j);

                if (a.compareTo(Fraction.ZERO) < 0) {

                    Fraction z = table.get(zRow, j);

                    ratios[j] = z.abs().div(a.abs());
                } else {
                    ratios[j] = null;
                }
            }

            int pivotCol = findPivotColumn(ratios);

            if (pivotCol == -1) {

                System.out.println("\n➡ no valid pivot column");

                boolean hasAnyNegative = false;

                for (int j = 0; j < n; j++) {
                    if (table.get(pivotRow, j).compareTo(Fraction.ZERO) < 0) {
                        hasAnyNegative = true;
                        break;
                    }
                }

                if (!hasAnyNegative) {
                    System.out.println("➡ UNBOUNDED");
                    return Result.unbounded();
                }

                System.out.println("➡ infeasible pivot step");
                return Result.noSolution();
            }

            System.out.println("\nРазрешающий элемента:");
            System.out.println("row = " + (pivotRow + 1));
            System.out.println("col = x" + (pivotCol + 1));

            pivot(pivotRow, pivotCol);

            step++;
        }
    }

    private Result buildLambdaSolution() {

        Fraction[] x1 = snapshotSolution();
        Fraction z = table.get(zRow, n);

        int entering = -1;

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

        Matrix backup = copyMatrix(table);
        int[] basisBackup = Arrays.copyOf(basis, basis.length);

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

            return Result.unbounded();
        }

        pivot(pivotRow, entering);

        System.out.println("\n=== Таблица состояния (X2) ===");
        printTable("X2 состояние", null);

        Fraction[] x2 = snapshotSolution();

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

    private void printTable(String title, Fraction[] ratios) {

        System.out.println("\n=== " + title + " ===");

        int rows = table.getRows();
        int cols = table.getCols();

        int cellW = 7;

        String sep = "-----------------------------------------------------------";

        System.out.println(sep);

        System.out.printf("| %-4s | %-5s |", "БП", "1");

        for (int j = 0; j < n; j++) {
            System.out.printf(" x%-4d |", j + 1);
        }

        System.out.println();
        System.out.println(sep);

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

        Fraction[] co = (ratios != null) ? ratios : calculateRatiosForTable();

        System.out.println(sep);

        if (ratios != null) {

            System.out.printf("| %-4s | %-5s |", "CO", "-");

            for (int j = 0; j < n; j++) {
                if (co[j] != null) {
                    System.out.printf(" %-6s|", co[j]);
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

    private Fraction[] calculateRatiosForTable() {

        Fraction[] ratios = new Fraction[n];
        Arrays.fill(ratios, null);

        int pivotRow = findPivotRow();
        if (pivotRow == -1) {
            return ratios;
        }

        for (int j = 0; j < n; j++) {
            Fraction a = table.get(pivotRow, j);

            if (a.compareTo(Fraction.ZERO) < 0) {
                Fraction z = table.get(zRow, j);
                ratios[j] = z.abs().div(a.abs());
            }
        }

        return ratios;
    }

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