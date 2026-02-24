import java.util.ArrayList;
import java.util.List;

public class BazisFinder {

    public static long combinations(int n, int k) {
        if (k < 0 || k > n) return 0;
        k = Math.min(k, n - k);

        long result = 1;
        for (int i = 1; i <= k; i++) {
            result = result * (n - k + i) / i;
        }
        return result;
    }

    public static List<SimpleDrobi[]> findAllBasicSolutions(SimpleDrobi[][] originalMatrix, int rank) {
        List<SimpleDrobi[]> solutions = new ArrayList<>();
        int cols = originalMatrix[0].length;
        int variables = cols - 1;

        List<int[]> combinations = generateCombinations(variables, rank);

        System.out.println("\n перебор всех решений");

        for (int[] basicVars : combinations) {
            System.out.print("\nПробуем базис: ");
            for (int var : basicVars) {
                System.out.print("x" + (var + 1) + " ");
            }
            System.out.println();

            try {
                SimpleDrobi[][] workMatrix = MatrixUtils.copyMatrix(originalMatrix);
                System.out.println("Исходная матрица:");
                MatrixUtils.printMatrix(workMatrix);

                boolean success = transformToBasis(workMatrix, basicVars, rank);

                if (success) {
                    SimpleDrobi[] solution = extractBasicSolution(workMatrix, basicVars, variables);
                    solutions.add(solution);

                    System.out.print("НАЙДЕНО: (");
                    for (int j = 0; j < solution.length; j++) {
                        System.out.print(solution[j]);
                        if (j < solution.length - 1) {
                            System.out.print(", ");
                        }
                    }
                    System.out.println(")");
                } else {
                    System.out.println("Невозможно составить базис");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }

        return solutions;
    }

    private static boolean transformToBasis(SimpleDrobi[][] matrix, int[] basicVars, int rank) {
        System.out.println("Преобразование к базису");

        for (int i = 0; i < rank; i++) {
            int targetCol = basicVars[i];
            System.out.println("Шаг " + (i + 1) + ": делаем x" + (targetCol + 1) + " базисной");

            boolean success = MatrixUtils.gaussJordanStep(matrix, i, targetCol);
            if (!success) {
                System.out.println("Нет ненулевого элемента в столбце");
                return false;
            }
        }

        return true;
    }

    private static SimpleDrobi[] extractBasicSolution(SimpleDrobi[][] matrix, int[] basicVars, int variables) {
        SimpleDrobi[] solution = new SimpleDrobi[variables];

        for (int i = 0; i < variables; i++) {
            solution[i] = new SimpleDrobi(0);
        }

        int lastCol = matrix[0].length - 1;
        for (int i = 0; i < basicVars.length; i++) {
            int varIndex = basicVars[i];
            solution[varIndex] = matrix[i][lastCol];
        }

        return solution;
    }

    public static List<int[]> generateCombinations(int n, int k) {
        List<int[]> combinations = new ArrayList<>();
        if (k > n || k < 0) return combinations;

        int[] combination = new int[k];

        for (int i = 0; i < k; i++) {
            combination[i] = i;
        }

        while (true) {
            combinations.add(combination.clone());

            int p = k - 1;
            while (p >= 0 && combination[p] == n - k + p) {
                p--;
            }

            if (p < 0) break;

            combination[p]++;
            for (int i = p + 1; i < k; i++) {
                combination[i] = combination[i - 1] + 1;
            }
        }

        return combinations;
    }
}