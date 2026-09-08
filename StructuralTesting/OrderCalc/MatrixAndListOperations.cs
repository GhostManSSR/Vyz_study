namespace OrderCalc;

public static class MatrixAndListOperations
{
    public static double MaxPriceEvenValueEvenIndex(List<double> prices)
    {
        if (prices == null)
        {
            throw new ArgumentNullException(nameof(prices), "List cannot be null");
        }

        bool found = false;
        double maxPrice = 0.0;

        for (int i = 0; i < prices.Count; ++i)
        {
            if (i % 2 == 0 && prices[i] % 2 == 0)
            {
                if (!found || prices[i] > maxPrice)
                {
                    maxPrice = prices[i];
                    found = true;
                }
            }
        }

        return maxPrice;
    }

    public static int SumOddBelowMainDiagonal(int[][] A)
    {
        if (A == null || A.Length == 0)
        {
            return 0;
        }

        int sum = 0;
        int rows = A.Length;

        for (int i = 0; i < rows; i++)
        {
            int cols = (A[i] != null) ? A[i].Length : 0;
            for (int j = 0; j < i && j < cols; j++)
            {
                if ((A[i][j] & 1) != 0)
                {
                    sum += A[i][j];
                }
            }
        }

        return sum;
    }
}