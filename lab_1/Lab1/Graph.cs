namespace SortingAlgorithms;

public class Graph
{
    public int[,] Matrix { get; }

    public int Size => Matrix.GetLength(0);

    public const int INF = int.MaxValue / 4;

    public Graph(int[,] matrix)
    {
        Matrix = matrix;
    }

    public int GetDistance(int from, int to)
    {
        return Matrix[from, to];
    }

    public bool HasEdge(int from, int to)
    {
        return Matrix[from, to] < INF;
    }

    public void Print()
    {
        Console.WriteLine("Матрица расстояний:");

        for (int i = 0; i < Size; i++)
        {
            for (int j = 0; j < Size; j++)
            {
                if (Matrix[i, j] >= INF)
                    Console.Write("  ∞ ");
                else
                    Console.Write($"{Matrix[i, j],3} ");
            }

            Console.WriteLine();
        }
    }
}