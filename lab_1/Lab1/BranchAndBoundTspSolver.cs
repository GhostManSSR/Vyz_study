using System.Diagnostics;

namespace SortingAlgorithms;

public class BranchAndBoundTspSolver
{
     private readonly Graph _graph;

    private int _bestCost;
    private List<int> _bestRoute = new();

    private long _nodes;
    private long _prunedBranches;
    private long _operations;

    public BranchAndBoundTspSolver(Graph graph)
    {
        _graph = graph;
    }

    public TspResult Solve()
    {
        Stopwatch stopwatch = Stopwatch.StartNew();

        _bestCost = Graph.INF;
        _bestRoute = new List<int>();

        _nodes = 0;
        _prunedBranches = 0;
        _operations = 0;

        int n = _graph.Size;

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
            Nodes = _nodes,
            PrunedBranches = _prunedBranches,
            ExecutionTime = stopwatch.Elapsed
        };
    }

    private void Search(
        int currentVertex,
        bool[] visited,
        List<int> route,
        int currentCost)
    {
        _nodes++;

        // Проверяем нижнюю границу
        int lowerBound = CalculateLowerBound(
            currentVertex,
            visited,
            currentCost
        );

        _operations++;

        // Если даже нижняя граница хуже
        // уже найденного решения,
        // эту ветвь можно отбросить.
        if (lowerBound >= _bestCost)
        {
            _prunedBranches++;
            return;
        }

        // Все вершины посещены
        if (route.Count == _graph.Size)
        {
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
                _bestRoute.Add(0);
            }

            return;
        }

        // Одностороннее ветвление:
        // из текущей вершины пробуем перейти
        // в каждую непосещённую вершину.
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
                _graph.GetDistance(
                    currentVertex,
                    nextVertex
                );

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

    /// <summary>
    /// Расчёт нижней границы стоимости
    /// незавершённого маршрута.
    /// </summary>
    private int CalculateLowerBound(
        int currentVertex,
        bool[] visited,
        int currentCost)
    {
        int lowerBound = currentCost;

        int n = _graph.Size;

        // Формируем множество вершин,
        // из которых ещё нужно сделать выход.
        List<int> remaining = new();

        remaining.Add(currentVertex);

        for (int i = 0; i < n; i++)
        {
            if (!visited[i])
                remaining.Add(i);
        }

        foreach (int from in remaining)
        {
            int minEdge = Graph.INF;

            // Возможные пункты назначения:
            // непосещённые вершины или стартовая вершина.
            for (int to = 0; to < n; to++)
            {
                if (from == to)
                    continue;

                bool allowed =
                    !visited[to] ||
                    to == 0;

                if (!allowed)
                    continue;

                if (!_graph.HasEdge(from, to))
                    continue;

                int distance =
                    _graph.GetDistance(from, to);

                if (distance < minEdge)
                    minEdge = distance;
            }

            // Если из вершины вообще нельзя выйти,
            // маршрут невозможен.
            if (minEdge >= Graph.INF)
                return Graph.INF;

            lowerBound += minEdge;
        }

        return lowerBound;
    }
}