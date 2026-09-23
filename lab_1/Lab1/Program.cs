using System;
using System.Collections.Generic;
using System.Diagnostics;

namespace SortingAlgorithms
{
    internal class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("------------------ UnboundedKnapsack -----------------------");
            int[] weights = { 3, 4, 2 };
            int[] values = { 4, 5, 3 };

            int capacity = 10;

            UnboundedKnapsack knapsack = new UnboundedKnapsack();

            int result = knapsack.Solve(
                weights,
                values,
                capacity
            );

            Console.WriteLine(
                $"Максимальная стоимость: {result}"
            );
            
            Console.WriteLine("------------------ Kraskal -----------------------");
            List<KruskalAlgorithm.Edge> edges = new()
            {
                new(0, 1, 10),
                new(0, 2, 6),
                new(0, 3, 5),
                new(1, 3, 15),
                new(2, 3, 4)
            };

            KruskalAlgorithm kruskal =
                new KruskalAlgorithm(4);

            List<KruskalAlgorithm.Edge> resultCraskal =
                kruskal.Solve(4, edges);

            int totalWeight = 0;

            Console.WriteLine("Минимальное остовное дерево:");

            foreach (var edge in resultCraskal)
            {
                Console.WriteLine(
                    $"{edge.From} -- {edge.To} : {edge.Weight}");

                totalWeight += edge.Weight;
            }

            Console.WriteLine(
                $"Общий вес: {totalWeight}");
            
            
            Console.WriteLine("------------------ Deikstra ----------------------");
            List<DijkstraAlgorithm.Edge>[] graph =
                new List<DijkstraAlgorithm.Edge>[5];

            for (int i = 0; i < graph.Length; i++)
            {
                graph[i] = new List<DijkstraAlgorithm.Edge>();
            }

            graph[0].Add(new(1, 10));
            graph[0].Add(new(2, 3));

            graph[1].Add(new(2, 1));
            graph[1].Add(new(3, 2));

            graph[2].Add(new(1, 4));
            graph[2].Add(new(3, 8));
            graph[2].Add(new(4, 2));

            graph[3].Add(new(4, 7));

            graph[4].Add(new(3, 9));

            DijkstraAlgorithm dijkstra =
                new DijkstraAlgorithm();

            int[] distances =
                dijkstra.Solve(graph, 0);

            Console.WriteLine(
                "Кратчайшие расстояния от вершины 0:");

            for (int i = 0; i < distances.Length; i++)
            {
                Console.WriteLine(
                    $"0 -> {i} = {distances[i]}");
            }
            
            Console.WriteLine("------------------ Skobki rastanovka ----------------------");
            
            int[] dimensions =
            {
                10,
                20,
                30,
                40,
                30
            };

            MatrixChainMultiplication algorithm =
                new MatrixChainMultiplication();

            int operations =
                algorithm.Solve(dimensions);

            string order =
                algorithm.GetOptimalOrder();

            Console.WriteLine(
                $"Минимальное количество операций: {operations}");

            Console.WriteLine(
                $"Оптимальная расстановка: {order}");
            
            // Console.WriteLine("----------- Ftp 1000000000000 elements");
            // FftBenchmark benchmark =
            //     new FftBenchmark();
            //
            // benchmark.Run(20);
            // benchmark.Run(22);
            // benchmark.Run(40);
            
            Console.WriteLine("-----------------------RGR variant 9------------------");
            // b = бесконечность / отсутствие дуги
        int I = Graph.INF;

        int[,] matrix =
        {
            { I, 13, 7, 5, 2, 9 },
            { 8, I, 4, 6, 5, I },
            { 8, 4, I, 3, 6, 2 },
            { 5, 6, 1, I, 0, 1 },
            { I, 6, 1, 4, I, 9 },
            { 10, 0, 8, 3, 7, I }
        };

        Graph graph_1 = new Graph(matrix);

        graph_1.Print();

        // ==========================================
        // 1. Прямой перебор
        // ==========================================

        BruteForceTspSolver bruteForce =
            new BruteForceTspSolver(graph_1);

        TspResult bruteResult =
            bruteForce.Solve();

        bruteResult.Print(
            "МЕТОД ПРЯМОГО ПЕРЕБОРА"
        );

        // ==========================================
        // 2. Ветви и границы
        // ==========================================

        BranchAndBoundTspSolver branchAndBound =
            new BranchAndBoundTspSolver(graph_1);

        TspResult branchResult =
            branchAndBound.Solve();

        branchResult.Print(
            "МЕТОД ВЕТВЕЙ И ГРАНИЦ"
        );

        // ==========================================
        // Сравнение
        // ==========================================

        Console.WriteLine();
        Console.WriteLine("===== СРАВНЕНИЕ =====");

        Console.WriteLine(
            $"Прямой перебор:       " +
            $"{bruteResult.ExecutionTime.TotalMilliseconds:F4} мс"
        );

        Console.WriteLine(
            $"Ветви и границы:      " +
            $"{branchResult.ExecutionTime.TotalMilliseconds:F4} мс"
        );

        Console.WriteLine();

        Console.WriteLine(
            $"Стоимость перебора:   {bruteResult.Cost}"
        );

        Console.WriteLine(
            $"Стоимость В&Г:        {branchResult.Cost}"
        );

        Console.WriteLine();

        Console.WriteLine(
            $"У прямого перебора операций: " +
            $"{bruteResult.Operations}"
        );

        Console.WriteLine(
            $"У В&Г операций:              " +
            $"{branchResult.Operations}"
        );

        Console.WriteLine(
            $"Отсечено ветвей:             " +
            $"{branchResult.PrunedBranches}"
        );
        }
    }
}