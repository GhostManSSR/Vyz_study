import java.util.List;

public class JordanGauss {
    public static SimpleDrobi[] solver(SimpleDrobi[][] matrix) throws ArithmeticException {
        int rows = matrix.length;
        int cols = matrix[0].length;

        SimpleDrobi[][] workMatrix = MatrixUtils.copyMatrix(matrix);
        int rank = 0;
        int col = 0;

        for (int row = 0; row < rows && col < cols - 1; row++) {
            int pivotRow = MatrixUtils.findPivotRow(workMatrix, row, col);

            if (pivotRow == -1) {
                col++;
                row--;
                continue;
            }

            boolean success = MatrixUtils.gaussJordanStep(workMatrix, row, col);
            if (success) {
                rank++;
                col++;
            }
        }

        for (int i = 0; i < rows; i++) {
            if (MatrixUtils.isZeroRow(workMatrix, i, cols - 1) &&
                    workMatrix[i][cols - 1].getChislitel() != 0) {
                throw new ArithmeticException("Система не имеет решений!");
            }
        }

        if (rank < cols - 1) {
            System.out.println("Система имеет бесконечно много решений!");
            System.out.println("Ранг системы: " + rank);
            System.out.println("Количество переменных: " + (cols - 1));
            System.out.println("Максимальное число базисных решений: " +
                    BazisFinder.combinations(cols - 1, rank));

            List<SimpleDrobi[]> basicSolutions = BazisFinder.findAllBasicSolutions(workMatrix, rank);

            if (basicSolutions.isEmpty()) {
                System.out.println("Не удалось найти ни одного базисного решения!");
            } else {
                System.out.println("\nНайденные базисные решения:");
                for (int i = 0; i < basicSolutions.size(); i++) {
                    SimpleDrobi[] solution = basicSolutions.get(i);
                    System.out.print("Решение " + (i + 1) + ": (");
                    for (int j = 0; j < solution.length; j++) {
                        System.out.print(solution[j]);
                        if (j < solution.length - 1) {
                            System.out.print(", ");
                        }
                    }
                    System.out.println(")");
                }
            }

            return null;
        }

        SimpleDrobi[] solution = new SimpleDrobi[rows];
        for (int i = 0; i < rows; i++) {
            solution[i] = workMatrix[i][cols - 1];
        }

        return solution;
    }

    public static void printSolution(SimpleDrobi[] solution) {
        if (solution == null) {
            return;
        }
        System.out.println("\nРешение системы:");
        for (int i = 0; i < solution.length; i++) {
            System.out.printf("x%d = %s\n", i + 1, solution[i]);
        }
    }
}