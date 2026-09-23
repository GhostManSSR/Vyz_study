using System.Diagnostics;

namespace SortingAlgorithms;

public class BruteForceTspSolver
{
    private readonly Graph _graph;

    private int _bestCost;
    private List<int> _bestRoute = new();

    private long _operations;

    public BruteForceTspSolver(Graph graph)
    {
        _graph = graph;
    }

    public TspResult Solve()
    {
        Stopwatch stopwatch = Stopwatch.StartNew();

        _bestCost = Graph.INF;
        _bestRoute = new List<int>();
        _operations = 0;

        int n = _graph.Size;

        // Начинаем всегда с вершины 0 (вершина 1 в человеческой нумерации)
        bool[] visited = new bool[n];
        visited[0] = true;

        List<int> route = new()
        {
            0
        };

        Search(
            currentVertex: 0,
            visited: visited,
            route: route,
            currentCost: 0
        );

        stopwatch.Stop();

        return new TspResult
        {
            Route = _bestRoute,
            Cost = _bestCost,
            Operations = _operations,
            ExecutionTime = stopwatch.Elapsed
        };
    }

    private void Search(
        int currentVertex,
        bool[] visited,
        List<int> route,
        int currentCost)
    {
        _operations++;

        // Все вершины посещены
        if (route.Count == _graph.Size)
        {
            // Возвращаемся в начальную вершину
            if (!_graph.HasEdge(currentVertex, 0))
                return;

            int finalCost =
                currentCost +
                _graph.GetDistance(currentVertex, 0);

            _operations++;

            if (finalCost < _bestCost)
            {
                _bestCost = finalCost;
                _bestRoute = new List<int>(route);

                // Добавляем начальную вершину в конец
                _bestRoute.Add(0);
            }

            return;
        }

        // Пробуем каждую непосещённую вершину
        for (int nextVertex = 0;
             nextVertex < _graph.Size;
             nextVertex++)
        {
            if (visited[nextVertex])
                continue;

            if (!_graph.HasEdge(currentVertex, nextVertex))
                continue;

            visited[nextVertex] = true;
            route.Add(nextVertex);

            int newCost =
                currentCost +
                _graph.GetDistance(currentVertex, nextVertex);

            Search(
                nextVertex,
                visited,
                route,
                newCost
            );

            route.RemoveAt(route.Count - 1);
            visited[nextVertex] = false;
        }
    }
}