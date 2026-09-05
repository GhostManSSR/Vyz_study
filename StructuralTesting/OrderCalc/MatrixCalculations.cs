namespace OrderCalc;

public class MatrixCalculations
{
    public static int SumEvenAboveSecondaryDiagonal(int[][] matrix)
    {
        // условие 1: пустая матрица
        if (matrix == null || matrix.Length == 0)
            return 0;

        int sum = 0;
        int n = matrix.Length;

        // цикл 1: по строкам
        for (int i = 0; i < n; i++)
        {
            // цикл 2: по столбцам
            for (int j = 0; j < matrix[i].Length; j++)
            {
                // условие 2: выше побочной диагонали
                if (i + j < n - 1)
                {
                    // условие 3: элемент чётный
                    if (matrix[i][j] % 2 == 0)
                    {
                        sum += matrix[i][j];
                    }
                }
            }
        }

        return sum;
    }
}