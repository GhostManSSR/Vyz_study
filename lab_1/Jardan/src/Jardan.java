import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Jardan {

    private Fraction[][] matrix;
    private int m, n;

    public Jardan(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists())
            throw new IOException("ОШИБКА: Файл не найден");
        if (!file.canRead())
            throw new IOException("ОШИБКА: Нет прав на чтение файла");

        Scanner sc = new Scanner(file);
        try {
            m = sc.nextInt();
            n = sc.nextInt();
            if (m <= 0 || n <= 0)
                throw new IOException("ОШИБКА: m и n должны быть положительными");

            matrix = new Fraction[m][n + 1];
            for (int i = 0; i < m; i++) {
                for (int j = 0; j <= n; j++) {
                    double val = sc.nextDouble();
                    matrix[i][j] = new Fraction(val);
                }
            }
        } finally {
            sc.close();
        }
        System.out.println("Загружено: " + m + " уравнений, " + n + " неизвестных");
    }

    private void printOperation(String op) {
        System.out.println(">>> " + op);
    }

    public void solve() {
        printMatrix("Исходная матрица");

        int pivotRow = 0;
        int[] pivotColumnForRow = new int[m];
        Arrays.fill(pivotColumnForRow, -1);

        for (int col = 0; col < n && pivotRow < m; col++) {
            int bestRow = -1;
            Fraction maxAbs = null;
            for (int r = pivotRow; r < m; r++) {
                if (!matrix[r][col].isZero()) {
                    Fraction currentAbs = matrix[r][col].abs();
                    if (maxAbs == null || currentAbs.compareTo(maxAbs) >= 0) {
                        maxAbs = currentAbs;
                        bestRow = r;
                    }
                }
            }

            if (bestRow == -1) {
                printOperation("Столбец x" + (col + 1) + " пропускается (нулевой)");
                continue;
            }

            if (bestRow != pivotRow) {
                printOperation("R" + (pivotRow + 1) + " <-> R" + (bestRow + 1));
                swapRows(bestRow, pivotRow);
                printMatrix("После перестановки строк");
            }

            Fraction diag = matrix[pivotRow][col];
            printOperation("R" + (pivotRow + 1) + " = R" + (pivotRow + 1) + " / " + diag);
            for (int j = col; j <= n; j++) {
                matrix[pivotRow][j] = matrix[pivotRow][j].divide(diag);
            }
            printMatrix("После нормализации pivot строки " + (pivotRow + 1));

            for (int r = 0; r < m; r++) {
                if (r != pivotRow && !matrix[r][col].isZero()) {
                    Fraction factor = matrix[r][col];
                    printOperation("R" + (r + 1) + " = R" + (r + 1) + " - " + factor + " * R" + (pivotRow + 1));
                    for (int j = col; j <= n; j++) {
                        matrix[r][j] = matrix[r][j].subtract(factor.multiply(matrix[pivotRow][j]));
                    }
                }
            }
            printMatrix("После прямоугольника в столбце x" + (col + 1));

            pivotColumnForRow[pivotRow] = col;
            pivotRow++;
        }

        printMatrix("ИТОГОВАЯ МАТРИЦА МЕТОДА ПРЯМОУГОЛЬНИКА");

        analyzeSolution(pivotRow, pivotColumnForRow);
    }

    private void swapRows(int r1, int r2) {
        Fraction[] temp = matrix[r1];
        matrix[r1] = matrix[r2];
        matrix[r2] = temp;
    }

    private void printMatrix(String label) {
        System.out.println("\n=== " + label + " ===");
        for (int i = 0; i < m; i++) {
            for (int j = 0; j <= n; j++) {
                System.out.printf("%12s ", matrix[i][j]);
            }
            System.out.println();
        }
    }

    private void analyzeSolution(int rank, int[] pivotColumnForRow) {
        System.out.println("\n=== АНАЛИЗ РЕШЕНИЯ (МЕТОД ПРЯМОУГОЛЬНИКА) ===");
        System.out.println("Ранг матрицы: " + rank);

        for (int i = 0; i < m; i++) {
            boolean zeroRow = true;
            for (int j = 0; j < n; j++) {
                if (!matrix[i][j].isZero()) {
                    zeroRow = false;
                    break;
                }
            }
            if (zeroRow && !matrix[i][n].isZero()) {
                System.out.println("СИСТЕМА НЕСОВМЕСТИМА (0 = " + matrix[i][n] + ")");
                return;
            }
        }

        if (rank == n) {
            System.out.println("ЕДИНСТВЕННОЕ РЕШЕНИЕ:");
            for (int i = 0; i < n; i++) {
                System.out.println("x" + (i + 1) + " = " + matrix[i][n]);
            }
        } else {
            System.out.println("БЕСКОНЕЧНО МНОГО РЕШЕНИЙ");
            boolean[] isPivot = new boolean[n];
            for (int i = 0; i < rank; i++) {
                isPivot[pivotColumnForRow[i]] = true;
            }

            System.out.print("БАЗИСНЫЕ ПЕРЕМЕННЫЕ: ");
            for (int i = 0; i < n; i++) {
                if (isPivot[i]) System.out.print("x" + (i + 1) + " ");
            }
            System.out.println();

            System.out.print("СВОБОДНЫЕ ПЕРЕМЕННЫЕ: ");
            for (int i = 0; i < n; i++) {
                if (!isPivot[i]) System.out.print("x" + (i + 1) + " ");
            }
            System.out.println();

            for (int i = 0; i < rank; i++) {
                int col = pivotColumnForRow[i];
                System.out.print("x" + (col + 1) + " = " + matrix[i][n]);
                for (int j = 0; j < n; j++) {
                    if (!isPivot[j] && !matrix[i][j].isZero()) {
                        System.out.print(" - " + matrix[i][j] + "*x" + (j + 1));
                    }
                }
                System.out.println();
            }
        }
    }
}
