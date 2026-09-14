using System;
using System.Diagnostics;
using System.Numerics;

namespace SortingAlgorithms
{
    internal class Program
    {
        static void Main(string[] args)
        {
            double[] signal = { 1, 2, 3, 4, 5 };
            double[] kernel = { 1, 0, -1 }; // простой дифференцирующий фильтр

            Console.WriteLine("=== Простая свёртка ===");
            double[] simpleResult = ConvolutionDemo.SimpleConvolve(signal, kernel);
            ConvolutionDemo.PrintArray("Signal", signal);
            ConvolutionDemo.PrintArray("Kernel", kernel);
            ConvolutionDemo.PrintArray("Simple Convolution Result", simpleResult);

            Console.WriteLine("\n=== Свёртка через FFT ===");
            double[] fftResult = ConvolutionDemo.FFTConvolve(signal, kernel);
            ConvolutionDemo.PrintArray("FFT Convolution Result", fftResult);

            Console.WriteLine("\n=== Проверка совпадения результатов ===");
            bool match = true;
            if (simpleResult.Length != fftResult.Length)
            {
                match = false;
            }
            else
            {
                for (int i = 0; i < simpleResult.Length; i++)
                {
                    if (Math.Abs(simpleResult[i] - fftResult[i]) > 1e-6)
                    {
                        match = false;
                        break;
                    }
                }
            }

            Console.WriteLine($"Результаты совпадают: {match}");
            
            
            // === Тест умножения матриц ===
            Console.WriteLine("=== Тест умножения матриц ===");

            double[,] a = 
            {
                { 1, 2, 3 },
                { 4, 5, 6 },
                { 7, 8, 9 }
            };

            double[,] b = 
            {
                { 9, 8, 7 },
                { 6, 5, 4 },
                { 3, 2, 1 }
            };

            double[,] cNaive = MatrixMultiply.MultiplyNaive(a, b);
            double[,] cStrassen = MatrixMultiply.MultiplyStrassen(a, b);

            MatrixMultiply.PrintMatrix("A", a);
            MatrixMultiply.PrintMatrix("B", b);
            MatrixMultiply.PrintMatrix("Naive A*B", cNaive);
            MatrixMultiply.PrintMatrix("Strassen A*B", cStrassen);

// === Тест Форда–Беллмана ===
            Console.WriteLine("=== Тест Форда–Беллмана ===");

// Граф: 4 вершины, рёбра с весами
            var edges = new List<Edge>
            {
                new Edge(0, 1, 4),
                new Edge(0, 2, 5),
                new Edge(1, 2, -3),
                new Edge(1, 3, 1),
                new Edge(2, 3, 4)
            };

            int n = 4;
            int source = 0;

            var fbResult = FordBellman.Run(n, edges, source);
            FordBellman.PrintResult("Кратчайшие пути из вершины 0", fbResult, source);
        }
    }
}