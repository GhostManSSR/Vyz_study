namespace SortingAlgorithms;

public class MatrixChainMultiplication
{
    private int[,] dp;
        private int[,] split;

        /// <summary>
        /// Возвращает минимальное количество операций
        /// умножения матриц.
        /// </summary>
        public int Solve(int[] dimensions)
        {
            int matrixCount = dimensions.Length - 1;

            dp = new int[matrixCount, matrixCount];
            split = new int[matrixCount, matrixCount];

            // Длина цепочки
            for (int length = 2;
                 length <= matrixCount;
                 length++)
            {
                // Начало цепочки
                for (int i = 0;
                     i < matrixCount - length + 1;
                     i++)
                {
                    int j = i + length - 1;

                    dp[i, j] = int.MaxValue;

                    // Пробуем все места для разделения
                    for (int k = i; k < j; k++)
                    {
                        int operations =
                            dp[i, k]
                            + dp[k + 1, j]
                            + dimensions[i]
                            * dimensions[k + 1]
                            * dimensions[j + 1];

                        if (operations < dp[i, j])
                        {
                            dp[i, j] = operations;
                            split[i, j] = k;
                        }
                    }
                }
            }

            return dp[0, matrixCount - 1];
        }

        /// <summary>
        /// Возвращает оптимальную расстановку скобок.
        /// </summary>
        public string GetOptimalOrder()
        {
            return BuildOrder(
                0,
                split.GetLength(0) - 1
            );
        }

        private string BuildOrder(int i, int j)
        {
            if (i == j)
            {
                return $"A{i + 1}";
            }

            int k = split[i, j];

            string left = BuildOrder(i, k);
            string right = BuildOrder(k + 1, j);

            return $"({left} × {right})";
        }
}