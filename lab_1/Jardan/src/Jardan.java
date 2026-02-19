import java.io.File;
import java.io.IOException;
import java.util.*;

public class Jardan {

    private Fraction[][] matrix;
    private int m, n;
    private int foundBasisCount = 0;

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
                    if (maxAbs == null || currentAbs.compareTo(maxAbs) > 0) {
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

    private Fraction[][] copyMatrix(Fraction[][] original) {
        int rows = original.length;
        int cols = original[0].length;
        Fraction[][] copy = new Fraction[rows][cols];
        for (int i = 0; i < rows; i++) {
            copy[i] = new Fraction[cols];
            for (int j = 0; j < cols; j++) {
                copy[i][j] = new Fraction(original[i][j].getNumerator(), original[i][j].getDenominator());
            }
        }
        return copy;
    }

    private void analyzeSolution(int rank, int[] pivotColumnForRow) {
        System.out.println("\n=== АНАЛИЗ РЕШЕНИЯ (МЕТОД ПРЯМОУГОЛЬНИКА) ===");
        System.out.println("Ранг матрицы: " + rank);

        // Проверка совместности
        for (int i = 0; i < m; i++) {
            boolean zeroRow = true;
            for (int j = 0; j < n; j++) {
                if (!matrix[i][j].isZero()) {
                    zeroRow = false;
                    break;
                }
            }
            if (zeroRow && !matrix[i][n].isZero()) {
                System.out.println("СИСТЕМА НЕСОВМЕСТНА: 0 = " + matrix[i][n]);
                return;
            }
        }

        printCurrentBasis(pivotColumnForRow, rank);

        if (rank == n) {
            System.out.println("\nЕДИНСТВЕННОЕ РЕШЕНИЕ:");
            for (int i = 0; i < n; i++) {
                System.out.println("x" + (i + 1) + " = " + matrix[i][n]);
            }
        } else if (rank < n) {
            System.out.println("\nБесконечно много решений. Ищем все базисные решения...");
            findAllPossibleBases(rank, pivotColumnForRow);
        } else {
            System.out.println("Полный ранг = число строк");
        }
    }



    private void printCurrentBasis(int[] pivotColumns, int rank) {
        System.out.print("Текущий базис: ");
        for (int i = 0; i < rank; i++) {
            if (pivotColumns[i] >= 0) {
                System.out.print("x" + (pivotColumns[i] + 1) + " ");
            }
        }
        System.out.println();
    }

    private void findAllPossibleBases(int currentRank, int[] currentPivots) {
        System.out.println("\n==============================");
        System.out.println("ВСЕ БАЗИСНЫЕ РЕШЕНИЯ СИСТЕМЫ");
        System.out.println("==============================");

        foundBasisCount = 0;

        List<List<Integer>> allPossibleBases = new ArrayList<>();
        generateCombinations(0, new ArrayList<>(), allPossibleBases, currentRank);

        for (List<Integer> basisColumns : allPossibleBases) {
            if (checkBasisValid(basisColumns)) {
                foundBasisCount++;
                Fraction[] basicSolution = computeBasicSolution(basisColumns);
                boolean isCurrent = isCurrentBasis(basisColumns, currentPivots);
                printBasicSolution(basisColumns, basicSolution, isCurrent);
            }
        }

        System.out.println("\n==============================");
        System.out.println("Всего базисных решений: " + foundBasisCount);
        System.out.println("==============================");
    }

    private void generateCombinations(int start, List<Integer> current,
                                      List<List<Integer>> result, int targetSize) {
        if (current.size() == targetSize) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < n; i++) {
            current.add(i);
            generateCombinations(i + 1, current, result, targetSize);
            current.remove(current.size() - 1);
        }
    }

    private Fraction[] computeBasicSolution(List<Integer> basisColumns) {
        int rank = basisColumns.size();
        Fraction[] solution = new Fraction[n];

        for (int i = 0; i < n; i++) {
            solution[i] = new Fraction(0);
        }

        Fraction[][] basisMatrix = new Fraction[rank][rank + 1];
        for (int i = 0; i < rank; i++) {
            for (int j = 0; j < rank; j++) {
                int basisCol = basisColumns.get(j);
                Fraction original = matrix[i][basisCol];
                basisMatrix[i][j] = new Fraction(
                        original.getNumerator(),
                        original.getDenominator()
                );
            }
            Fraction freeTerm = matrix[i][n];
            basisMatrix[i][rank] = new Fraction(
                    freeTerm.getNumerator(),
                    freeTerm.getDenominator()
            );
        }

        gaussSolve(basisMatrix);

        for (int i = 0; i < rank; i++) {
            solution[basisColumns.get(i)] = basisMatrix[i][rank];
        }

        return solution;
    }


    private void gaussSolve(Fraction[][] mat) {
        int size = mat.length;
        int rank = 0;

        for (int col = 0; col < size && rank < size; col++) {
            int pivotRow = -1;
            for (int row = rank; row < size; row++) {
                if (!mat[row][col].isZero()) {
                    pivotRow = row;
                    break;
                }
            }

            if (pivotRow == -1) continue;

            if (pivotRow != rank) {
                Fraction[] temp = mat[rank];
                mat[rank] = mat[pivotRow];
                mat[pivotRow] = temp;
            }

            Fraction pivot = mat[rank][col];
            for (int j = col; j <= size; j++) {
                mat[rank][j] = mat[rank][j].divide(pivot);
            }

            for (int row = 0; row < size; row++) {
                if (row != rank && !mat[row][col].isZero()) {
                    Fraction factor = mat[row][col];
                    for (int j = col; j <= size; j++) {
                        mat[row][j] = mat[row][j].subtract(factor.multiply(mat[rank][j]));
                    }
                }
            }
            rank++;
        }
    }

    private void printBasicSolution(List<Integer> basisColumns, Fraction[] solution, boolean isCurrent) {
        String marker = isCurrent ? " ОПОРНОЕ" : "     ";

        System.out.println("\n" + marker + " БАЗИСНОЕ РЕШЕНИЕ #" + foundBasisCount + ":");
        System.out.print("Базис: {");
        for (int i = 0; i < basisColumns.size(); i++) {
            System.out.print("x" + (basisColumns.get(i) + 1));
            if (i < basisColumns.size() - 1) System.out.print(", ");
        }
        System.out.println("}");

        System.out.print("Решение: (");
        for (int i = 0; i < n; i++) {
            System.out.print("x" + (i + 1) + "=" + solution[i]);
            if (i < n - 1) System.out.print(", ");
        }
        System.out.println(")");

        System.out.print("Свободные: ");
        for (int i = 0; i < n; i++) {
            if (solution[i].isZero()) {
                System.out.print("x" + (i + 1) + " ");
            }
        }
        System.out.println();
    }

    private List<List<Integer>> findAllBases() {
        List<List<Integer>> bases = new ArrayList<>();

        for (int i1 = 0; i1 < n - 2; i1++) {
            for (int i2 = i1 + 1; i2 < n - 1; i2++) {
                for (int i3 = i2 + 1; i3 < n; i3++) {
                    List<Integer> basis = Arrays.asList(i1, i2, i3);
                    if (checkBasisValid(basis)) {
                        bases.add(basis);
                    }
                }
            }
        }
        return bases;
    }

    private boolean checkBasisValid(List<Integer> basisColumns) {
        int numCols = basisColumns.size();
        Fraction[][] subMatrix = new Fraction[m][numCols];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < numCols; j++) {
                int colIndex = basisColumns.get(j);
                subMatrix[i][j] = new Fraction(
                        matrix[i][colIndex].getNumerator(),
                        matrix[i][colIndex].getDenominator()
                );
            }
        }

        int subRank = computeRank(subMatrix);
        return subRank == basisColumns.size();
    }

    private int computeRank(Fraction[][] subMatrix) {
        int rows = subMatrix.length;
        int cols = subMatrix[0].length;
        Fraction[][] working = copyMatrix(subMatrix);
        int rank = 0;

        for (int col = 0; col < cols && rank < rows; col++) {
            int pivotRow = -1;
            for (int row = rank; row < rows; row++) {
                if (!working[row][col].isZero()) {
                    pivotRow = row;
                    break;
                }
            }

            if (pivotRow == -1) continue;

            if (pivotRow != rank) {
                Fraction[] temp = working[rank];
                working[rank] = working[pivotRow];
                working[pivotRow] = temp;
            }

            Fraction pivot = working[rank][col];
            for (int j = col; j < cols; j++) {
                working[rank][j] = working[rank][j].divide(pivot);
            }

            for (int row = 0; row < rows; row++) {
                if (row != rank && !working[row][col].isZero()) {
                    Fraction factor = working[row][col];
                    for (int j = col; j < cols; j++) {
                        working[row][j] = working[row][j].subtract(factor.multiply(working[rank][j]));
                    }
                }
            }
            rank++;
        }
        return rank;
    }

    private boolean isCurrentBasis(List<Integer> basis, int[] currentPivots) {
        Set<Integer> basisSet = new HashSet<>(basis);
        for (int pivot : currentPivots) {
            if (pivot >= 0 && !basisSet.contains(pivot)) {
                return false;
            }
        }
        return true;
    }

    private void printBasis(List<Integer> basisColumns, int number) {
        System.out.print("БАЗИС #" + number + ": ");
        for (int col : basisColumns) {
            System.out.print("x" + (col + 1) + " ");
        }
        System.out.println();
    }

}
