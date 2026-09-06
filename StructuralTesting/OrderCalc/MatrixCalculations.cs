namespace OrderCalc;

public class MatrixCalculations
{
    public static int SumOddAboveMainDiagonal(int[][] matrix)
    {
        if (matrix == null || matrix.Length == 0)
            return 0;

        int sum = 0;
        for (int i = 0; i < matrix.Length; i++)
        {
            for (int j = 0; j < matrix[i].Length; j++)
            {
                if (i < j)
                {
                    if (matrix[i][j] % 2 != 0)
                    {
                        sum += matrix[i][j];
                    }
                }
            }
        }
        return sum;
    }
}