namespace SortingAlgorithms;

public class UnboundedKnapsack
{
    /// <summary>
    /// Решает задачу неограниченного рюкзака.
    /// Каждый предмет можно брать неограниченное количество раз.
    /// </summary>
    /// <param name="weights">Массив весов предметов</param>
    /// <param name="values">Массив стоимостей предметов</param>
    /// <param name="capacity">Вместимость рюкзака</param>
    /// <returns>Максимальная стоимость</returns>
    public int Solve(int[] weights, int[] values, int capacity)
    {
        if (weights == null || values == null)
            throw new ArgumentNullException();

        if (weights.Length != values.Length)
            throw new ArgumentException(
                "Количество весов и стоимостей должно совпадать.");

        if (capacity < 0)
            throw new ArgumentException(
                "Вместимость рюкзака не может быть отрицательной.");

        int[] dp = new int[capacity + 1];

        for (int w = 1; w <= capacity; w++)
        {
            for (int i = 0; i < weights.Length; i++)
            {
                if (weights[i] <= w)
                {
                    dp[w] = Math.Max(
                        dp[w],
                        dp[w - weights[i]] + values[i]
                    );
                }
            }
        }

        return dp[capacity];
    }
}
