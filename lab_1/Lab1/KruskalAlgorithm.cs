namespace SortingAlgorithms;

public class KruskalAlgorithm
{
     public class Edge
        {
            public int From { get; set; }
            public int To { get; set; }
            public int Weight { get; set; }

            public Edge(int from, int to, int weight)
            {
                From = from;
                To = to;
                Weight = weight;
            }
        }

        private int[] parent;
        private int[] rank;

        public KruskalAlgorithm(int verticesCount)
        {
            parent = new int[verticesCount];
            rank = new int[verticesCount];

            for (int i = 0; i < verticesCount; i++)
            {
                parent[i] = i;
            }
        }

        // Поиск корня множества
        private int Find(int vertex)
        {
            if (parent[vertex] != vertex)
            {
                parent[vertex] = Find(parent[vertex]);
            }

            return parent[vertex];
        }

        // Объединение двух множеств
        private bool Union(int first, int second)
        {
            int rootFirst = Find(first);
            int rootSecond = Find(second);

            // Вершины уже находятся в одном множестве.
            // Значит, ребро образует цикл.
            if (rootFirst == rootSecond)
            {
                return false;
            }

            if (rank[rootFirst] < rank[rootSecond])
            {
                parent[rootFirst] = rootSecond;
            }
            else if (rank[rootFirst] > rank[rootSecond])
            {
                parent[rootSecond] = rootFirst;
            }
            else
            {
                parent[rootSecond] = rootFirst;
                rank[rootFirst]++;
            }

            return true;
        }

        public List<Edge> Solve(
            int verticesCount,
            List<Edge> edges)
        {
            List<Edge> result = new List<Edge>();

            // Сортируем рёбра по возрастанию веса
            edges = edges
                .OrderBy(edge => edge.Weight)
                .ToList();

            foreach (Edge edge in edges)
            {
                if (Union(edge.From, edge.To))
                {
                    result.Add(edge);
                }

                // Для V вершин в MST должно быть V - 1 ребро
                if (result.Count == verticesCount - 1)
                {
                    break;
                }
            }

            return result;
        }
}