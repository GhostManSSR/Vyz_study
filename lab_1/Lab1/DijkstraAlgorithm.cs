namespace SortingAlgorithms;

public class DijkstraAlgorithm
{
     public class Edge
        {
            public int To { get; set; }
            public int Weight { get; set; }

            public Edge(int to, int weight)
            {
                To = to;
                Weight = weight;
            }
        }

        public int[] Solve(
            List<Edge>[] graph,
            int startVertex)
        {
            int verticesCount = graph.Length;

            // Расстояния от стартовой вершины
            int[] distance = new int[verticesCount];

            // Посещена ли вершина
            bool[] visited = new bool[verticesCount];

            // Сначала считаем все расстояния бесконечными
            for (int i = 0; i < verticesCount; i++)
            {
                distance[i] = int.MaxValue;
            }

            // Расстояние от вершины до самой себя = 0
            distance[startVertex] = 0;

            for (int i = 0; i < verticesCount; i++)
            {
                int currentVertex = -1;

                // Ищем непосещённую вершину
                // с минимальным расстоянием
                for (int j = 0; j < verticesCount; j++)
                {
                    if (!visited[j] &&
                        distance[j] != int.MaxValue &&
                        (currentVertex == -1 ||
                         distance[j] < distance[currentVertex]))
                    {
                        currentVertex = j;
                    }
                }

                // Если подходящей вершины больше нет
                if (currentVertex == -1)
                {
                    break;
                }

                visited[currentVertex] = true;

                // Проверяем соседей
                foreach (Edge edge in graph[currentVertex])
                {
                    int newDistance =
                        distance[currentVertex] + edge.Weight;

                    if (newDistance < distance[edge.To])
                    {
                        distance[edge.To] = newDistance;
                    }
                }
            }

            return distance;
        }
}