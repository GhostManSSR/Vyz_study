import java.util.*;
import java.io.*;

public class Jardan {
    private Fraction[][] matrix;
    private int m, n;
    private ArrayList<Integer> activeRows;
    private static final double EPS = 1e-10;

    public Jardan(String filename) throws IOException {
        readMatrix(filename);
        activeRows = new ArrayList<>();
        for (int i = 0; i < m; i++) activeRows.add(i);
        System.out.println("Загружена система: " + m + " уравнений, " + n + " неизвестных");
    }

    private void readMatrix(String filename) throws IOException {
        Scanner sc = new Scanner(new File(filename));
        m = sc.nextInt();
        n = sc.nextInt();
        matrix = new Fraction[m][n + 1];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j <= n; j++) {
                double val = sc.nextDouble();
                matrix[i][j] = new Fraction(val);
            }
        }
        sc.close();
    }

    public void solve() {
        printMatrix("Исходная матрица");

        // Прямой ход Гаусса с вычеркиванием нулевых строк
        for (int col = 0; col < Math.min(m, n); col++) {
            if (pivotAndCheckZeroRow(col)) continue;
            eliminateForward(col);
            printMatrix("После исключения столбца " + col);
            checkAndRemoveZeroRows();
        }

        // Обратный ход Жордана
        for (int col = Math.min(m, n) - 1; col >= 0; col--) {
            if (!hasNonZeroPivot(col)) continue;
            normalizeRow(col);
            eliminateBackward(col);
            printMatrix("После Жордана столбец " + col);
        }

        printMatrix("ИТОГОВАЯ МАТРИЦА");
        analyzeSolution();
    }

    private boolean pivotAndCheckZeroRow(int col) {
        int pivotRow = findPivotRow(col);
        if (pivotRow == -1) {
            System.out.println("❌ Нулевой столбец " + col + " — переходим к следующему");
            return true;
        }

        moveRowToPosition(pivotRow, col);
        printMatrix("После перестановки столбца " + col);

        if (matrix[col][col].isNearZero(EPS)) {
            System.out.println("❌ Вычеркнута нулевая строка " + col);
            activeRows.remove(Integer.valueOf(col));
            return true;
        }
        return false;
    }

    private int findPivotRow(int col) {
        int maxRow = -1;
        Fraction maxVal = new Fraction(0);

        for (int idx : activeRows) {
            if (idx < col) continue;
            Fraction absVal = matrix[idx][col].abs();
            if (absVal.compareTo(maxVal) > 0) {
                maxVal = absVal;
                maxRow = idx;
            }
        }
        return maxRow;
    }

    private void moveRowToPosition(int fromRow, int toRow) {
        if (fromRow != toRow) {
            System.out.println("🔄 Перестановка строк: " + toRow + " <-> " + fromRow);
            Fraction[] temp = matrix[toRow];
            matrix[toRow] = matrix[fromRow];
            matrix[fromRow] = temp;
        }
    }

    private void checkAndRemoveZeroRows() {
        ArrayList<Integer> newActiveRows = new ArrayList<>();
        for (int idx : activeRows) {
            boolean isZeroRow = true;
            for (int j = 0; j < n; j++) {
                if (!matrix[idx][j].isNearZero(EPS)) {
                    isZeroRow = false;
                    break;
                }
            }
            if (!isZeroRow || !matrix[idx][n].isNearZero(EPS)) {
                newActiveRows.add(idx);
            } else {
                System.out.println("✂️ ВЫЧЕРКНУТА нулевая строка " + (idx + 1));
            }
        }
        activeRows = newActiveRows;
        System.out.println("Осталось активных строк: " + activeRows.size());
    }

    private boolean hasNonZeroPivot(int col) {
        return activeRows.contains(col) && !matrix[col][col].isNearZero(EPS);
    }

    private void eliminateForward(int col) {
        for (int rowIdx : activeRows) {
            if (rowIdx <= col) continue;
            Fraction factor = matrix[rowIdx][col].divide(matrix[col][col]);
            for (int j = col; j <= n; j++) {
                matrix[rowIdx][j] = matrix[rowIdx][j].subtract(
                        factor.multiply(matrix[col][j]));
            }
        }
    }

    private void normalizeRow(int col) {
        Fraction diag = matrix[col][col];
        for (int j = col; j <= n; j++) {
            matrix[col][j] = matrix[col][j].divide(diag);
        }
    }

    private void eliminateBackward(int col) {
        for (int rowIdx : activeRows) {
            if (rowIdx >= col) continue;
            Fraction factor = matrix[rowIdx][col];
            for (int j = col; j <= n; j++) {
                matrix[rowIdx][j] = matrix[rowIdx][j].subtract(
                        factor.multiply(matrix[col][j]));
            }
        }
    }

    private boolean isNearZero(Fraction f) {
        return Math.abs(f.toDouble()) < EPS;
    }

    private void printMatrix(String label) {
        System.out.println("\n=== " + label + " (активных строк: " + activeRows.size() + ") ===");
        System.out.printf("%-4s", "№");
        for (int j = 0; j < n; j++) {
            System.out.printf("| x%-2d", j + 1);
        }
        System.out.printf("|  b%n");
        System.out.println("----+------------------------------------------------");

        int rowNum = 1;
        for (int idx : activeRows) {
            System.out.printf("%-4d", rowNum++);
            for (int j = 0; j <= n; j++) {
                System.out.printf("| %12s", matrix[idx][j].toMixedString());
            }
            System.out.println("|");
        }
        System.out.println();
    }


    private void analyzeSolution() {
        int rank = activeRows.size();
        boolean consistent = true;

        // Проверка совместности
        for (int idx : activeRows) {
            boolean zeroRow = true;
            for (int j = 0; j < n; j++) {
                if (!isNearZero(matrix[idx][j])) {
                    zeroRow = false;
                    break;
                }
            }
            if (zeroRow && !isNearZero(matrix[idx][n])) {
                consistent = false;
            }
        }

        System.out.println("\n=== АНАЛИЗ РЕШЕНИЯ ===");
        System.out.println("Ранг матрицы: " + rank + ", Неизвестных: " + n);

        if (!consistent) {
            System.out.println("❌ СИСТЕМА НЕ ИМЕЕТ РЕШЕНИЙ");
            return;
        }

        if (rank == n) {
            System.out.println("✅ ЕДИНСТВЕННОЕ РЕШЕНИЕ:");
            for (int col = 0; col < n; col++) {
                if (activeRows.contains(col)) {
                    System.out.printf("x%d = %15s%n", col + 1, matrix[col][n].toMixedString());
                }
            }
        } else {
            System.out.println("♾️ БЕСКОНЕЧНО МНОГО РЕШЕНИЙ");
            System.out.println("Общее решение (свободные переменные x" + (rank+1) + ", ..., x" + n + "):");

            for (int i = 0; i < rank && i < activeRows.size(); i++) {
                int pivotRow = activeRows.get(i);
                if (pivotRow != i) continue;

                System.out.printf("x%d = ", i + 1);
                boolean firstTerm = true;

                // ✅ ИСПРАВЛЕННАЯ ЛОГИКА ЗНАКОВ
                for (int freeVar = rank; freeVar < n; freeVar++) {
                    if (!isNearZero(matrix[pivotRow][freeVar])) {
                        // 1. Берем ОРИГИНАЛЬНЫЙ коэффициент из матрицы
                        Fraction origCoef = matrix[pivotRow][freeVar];
                        // 2. Для x_i = b - Σ a_ij*x_j нужен -a_ij
                        Fraction displayCoef = origCoef.negate();

                        // 3. Определяем знак для вывода
                        boolean coefPositive = displayCoef.getNumerator() > 0;
                        String coefStr = displayCoef.abs().toMixedString();

                        if (firstTerm) {
                            // Первый член: если отрицательный — ставим минус
                            if (!coefPositive) {
                                System.out.print("-");
                            }
                        } else {
                            // Последующие члены
                            if (coefPositive) {
                                System.out.print(" - ");
                            } else {
                                System.out.print(" + ");
                            }
                        }

                        // Коэффициент (1 и -1 не печатаем)
                        if (!coefStr.equals("1")) {
                            System.out.print(coefStr + "·");
                        }
                        System.out.print("x" + (freeVar + 1));
                        firstTerm = false;
                    }
                }

                // ✅ Свободный член (ПОЛОЖИТЕЛЬНЫЙ!)
                if (!isNearZero(matrix[pivotRow][n])) {
                    if (!firstTerm) {
                        System.out.print(" + ");
                    }
                    System.out.print(matrix[pivotRow][n].toMixedString());
                }

                if (firstTerm) {
                    System.out.print("0");
                }
                System.out.println();
            }

            System.out.print("где ");
            for (int i = rank; i < n; i++) {
                if (i > rank) System.out.print(", ");
                System.out.print("x" + (i + 1));
            }
            System.out.println(" — произвольные параметры.");
        }
    }

}
