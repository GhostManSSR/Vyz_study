namespace SortingAlgorithms;

public class TspResult
{
    public List<int> Route { get; set; } = new();

    public int Cost { get; set; }

    public long Operations { get; set; }

    public long Nodes { get; set; }

    public long PrunedBranches { get; set; }

    public TimeSpan ExecutionTime { get; set; }

    public void Print(string algorithmName)
    {
        Console.WriteLine();
        Console.WriteLine($"===== {algorithmName} =====");

        Console.Write("Маршрут: ");

        for (int i = 0; i < Route.Count; i++)
        {
            Console.Write(Route[i] + 1);

            if (i < Route.Count - 1)
                Console.Write(" -> ");
        }

        Console.WriteLine();

        Console.WriteLine($"Стоимость маршрута: {Cost}");
        Console.WriteLine($"Операций: {Operations}");
        Console.WriteLine($"Время: {ExecutionTime.TotalMilliseconds:F4} мс");

        if (Nodes > 0)
            Console.WriteLine($"Посещено вершин дерева: {Nodes}");

        if (PrunedBranches > 0)
            Console.WriteLine($"Отсечено ветвей: {PrunedBranches}");
    }
}