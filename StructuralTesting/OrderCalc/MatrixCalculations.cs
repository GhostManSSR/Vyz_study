namespace OrderCalc;

public class MatrixCalculations
{
    public static int SumEvenAboveSecondaryDiagonal(int[][] matrix)
    {
        if (matrix == null || matrix.Length == 0)
            return 0;

        int sum = 0;
        int n = matrix.Length;

        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < matrix[i].Length; j++)
            {
                if (i + j < n - 1)
                {
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