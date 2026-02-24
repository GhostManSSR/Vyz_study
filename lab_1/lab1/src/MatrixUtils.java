public class MatrixUtils {

    public static void printMatrix(SimpleDrobi[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.printf("%12s",matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    public static SimpleDrobi[][] copyMatrix(SimpleDrobi[][] original) {
        int rows = original.length;
        int cols = original[0].length;
        SimpleDrobi[][] copy = new SimpleDrobi[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                copy[i][j] = new SimpleDrobi(
                        original[i][j].getChislitel(),
                        original[i][j].getZnamenatel()
                );
            }
        }
        return copy;
    }

    public static void swapRows(SimpleDrobi[][] matrix, int row1, int row2) {
        SimpleDrobi[] temp = matrix[row1];
        matrix[row1] = matrix[row2];
        matrix[row2] = temp;
    }

    public static void normalizeRow(SimpleDrobi[][] matrix, int row, int col) {
        SimpleDrobi pivot = matrix[row][col];
        for (int j = 0; j < matrix[0].length; j++) {
            matrix[row][j] = matrix[row][j].del(pivot);
        }
    }

       public static boolean gaussJordanStep(SimpleDrobi[][] matrix, int stepRow, int targetCol) {
        int pivotRow = findPivotRow(matrix, stepRow, targetCol);

        if (pivotRow == -1) {
            return false;
        }

        if (pivotRow != stepRow) {
            System.out.println("Замена рядов " + stepRow + " и " + pivotRow);
            swapRows(matrix, stepRow, pivotRow);
            printMatrix(matrix);
        }

        SimpleDrobi pivot = matrix[stepRow][targetCol];
        System.out.println("Деление ряда " + stepRow + " на " + pivot);
        normalizeRow(matrix, stepRow, targetCol);
        printMatrix(matrix);

        System.out.println("Зануление столбца");
        eliminateColumn(matrix, stepRow, targetCol);
        printMatrix(matrix);

        return true;
    }

    public static void eliminateColumn(SimpleDrobi[][] matrix, int pivotRow, int pivotCol) {
        for (int r = 0; r < matrix.length; r++) {
            if (r != pivotRow && matrix[r][pivotCol].getChislitel() != 0) {
                SimpleDrobi factor = matrix[r][pivotCol];
                for (int j = 0; j < matrix[0].length; j++) {
                    matrix[r][j] = matrix[r][j].minus(
                            matrix[pivotRow][j].umn(factor)
                    );
                }
            }
        }
    }

    public static int findPivotRow(SimpleDrobi[][] matrix, int startRow, int col) {
        SimpleDrobi maxAbs = new SimpleDrobi(0);
        int pivotRow = -1;

        for (int r = startRow; r < matrix.length; r++) {
            SimpleDrobi currentAbs = matrix[r][col].abs();
            if (currentAbs.compareTo(maxAbs) > 0) {
                maxAbs = currentAbs;
                pivotRow = r;
            }
        }

        return pivotRow;
    }

    public static boolean isZeroRow(SimpleDrobi[][] matrix, int row, int upToCol) {
        for (int j = 0; j < upToCol; j++) {
            if (matrix[row][j].getChislitel() != 0) {
                return false;
            }
        }
        return true;
    }
}