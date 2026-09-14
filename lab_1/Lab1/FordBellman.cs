namespace SortingAlgorithms;

 public class Edge
    {
        public int From { get; }
        public int To { get; }
        public double Weight { get; }

        public Edge(int from, int to, double weight)
        {
            From = from;
            To = to;
            Weight = weight;
        }
    }

    public class FordBellmanResult
    {
        public double[] Distances { get; }
        public int[] Previous { get; }
        public bool HasNegativeCycle { get; }

        public FordBellmanResult(double[] distances, int[] previous, bool hasNegativeCycle)
        {
            Distances = distances;
            Previous = previous;
            HasNegativeCycle = hasNegativeCycle;
        }
    }

    public static class FordBellman
    {
        /// <param name="n">Количество вершин (0..n-1)</param>
        /// <param name="edges">Список рёбер графа</param>
        /// <param name="source">Номер стартовой вершины</param>
        public static FordBellmanResult Run(int n, IEnumerable<Edge> edges, int source)
        {
            const double INF = 1e18;

            double[] dist = new double[n];
            int[] prev = new int[n];

            for (int i = 0; i < n; i++)
            {
                dist[i] = INF;
                prev[i] = -1;
            }

            dist[source] = 0;

            var edgeList = new List<Edge>(edges);

            // n-1 итерация релаксации
            for (int i = 0; i < n - 1; i++)
            {
                bool changed = false;
                foreach (var e in edgeList)
                {
                    if (dist[e.From] == INF) continue;

                    double newDist = dist[e.From] + e.Weight;
                    if (newDist < dist[e.To])
                    {
                        dist[e.To] = newDist;
                        prev[e.To] = e.From;
                        changed = true;
                    }
                }

                if (!changed) break;
            }

            // Проверка на отрицательный цикл
            bool hasNegativeCycle = false;
            foreach (var e in edgeList)
            {
                if (dist[e.From] == INF) continue;

                if (dist[e.From] + e.Weight < dist[e.To])
                {
                    hasNegativeCycle = true;
                    break;
                }
            }

            return new FordBellmanResult(dist, prev, hasNegativeCycle);
        }

        // Восстановление пути от source до target
        public static List<int> GetPath(FordBellmanResult result, int target)
        {
            if (result.Previous[target] == -1 && target != Array.IndexOf(result.Distances, 0))
                return new List<int>(); // пути нет

            var path = new List<int>();
            int v = target;

            while (v != -1)
            {
                path.Add(v);
                v = result.Previous[v];
            }

            path.Reverse();
            return path;
        }

        // Вспомогательный метод для печати результата
        public static void PrintResult(string name, FordBellmanResult result, int source)
        {
            Console.WriteLine($"{name}:");
            Console.WriteLine($"Отрицательный цикл: {result.HasNegativeCycle}");

            for (int i = 0; i < result.Distances.Length; i++)
            {
                if (i == source)
                {
                    Console.WriteLine($"Вершина {i}: dist = 0 (source)");
                    continue;
                }

                if (double.IsPositiveInfinity(result.Distances[i]) || result.Distances[i] > 1e17)
                {
                    Console.WriteLine($"Вершина {i}: недостижима");
                }
                else
                {
                    var path = GetPath(result, i);
                    Console.WriteLine(
                        $"Вершина {i}: dist = {result.Distances[i]:F2}, путь = {string.Join(" -> ", path)}");
                }
            }

            Console.WriteLine();
        }
    }