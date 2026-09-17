using System;
using System.Collections.Generic;
using System.Diagnostics;

namespace SortingAlgorithms
{
    internal class Program
    {
        static void Main(string[] args)
        {
            Console.OutputEncoding = System.Text.Encoding.UTF8;

            double[] signal = { 1, 2, 3, 4, 5 };
            double[] kernel = { 1, 0, -1 };

            Console.WriteLine("========================================");
            Console.WriteLine("      ДЕМОНСТРАЦИЯ АЛГОРИТМОВ СВЁРТКИ");
            Console.WriteLine("========================================");

            ConvolutionDemo.PrintArray("Signal", signal);
            ConvolutionDemo.PrintArray("Kernel", kernel);

            Console.WriteLine("\n=== 1. Прямая свёртка O(n * m) ===");

            var simpleCounter = new ConvolutionDemo.OperationsCounter();

            Stopwatch stopwatch = Stopwatch.StartNew();
            double[] simpleResult = ConvolutionDemo.SimpleConvolve(
                signal,
                kernel,
                simpleCounter);
            stopwatch.Stop();

            ConvolutionDemo.PrintArray("Simple result", simpleResult);
            PrintStatistics(
                "Прямая свёртка",
                stopwatch.Elapsed,
                simpleCounter);

            Console.WriteLine("\n=== 2. Свёртка через обычное DFT O(N²) ===");

            var dftCounter = new ConvolutionDemo.OperationsCounter();

            stopwatch.Restart();
            double[] dftResult = ConvolutionDemo.DFTConvolve(
                signal,
                kernel,
                dftCounter);
            stopwatch.Stop();

            ConvolutionDemo.PrintArray("DFT result", dftResult);
            PrintStatistics(
                "Свёртка через DFT",
                stopwatch.Elapsed,
                dftCounter);

            Console.WriteLine("\n=== 3. Свёртка через FFT O(N log N) ===");

            var fftCounter = new ConvolutionDemo.OperationsCounter();

            stopwatch.Restart();
            double[] fftResult = ConvolutionDemo.FFTConvolve(
                signal,
                kernel,
                fftCounter);
            stopwatch.Stop();

            ConvolutionDemo.PrintArray("FFT result", fftResult);
            PrintStatistics(
                "Свёртка через FFT",
                stopwatch.Elapsed,
                fftCounter);

            Console.WriteLine("\n=== Проверка совпадения результатов ===");

            bool simpleAndDftMatch = ConvolutionDemo.ArraysAlmostEqual(
                simpleResult,
                dftResult);

            bool simpleAndFftMatch = ConvolutionDemo.ArraysAlmostEqual(
                simpleResult,
                fftResult);

            Console.WriteLine($"Simple == DFT: {simpleAndDftMatch}");
            Console.WriteLine($"Simple == FFT: {simpleAndFftMatch}");

            Console.WriteLine("\n=== Теоретическая трудоёмкость ===");

            int resultLength = signal.Length + kernel.Length - 1;
            int transformSize = ConvolutionDemo.GetNextPowerOfTwo(resultLength);

            Console.WriteLine($"Длина результата: {resultLength}");
            Console.WriteLine($"Размер преобразования N: {transformSize}");
            Console.WriteLine($"Прямая свёртка: O({signal.Length} * {kernel.Length})");
            Console.WriteLine($"DFT-свёртка: O({transformSize}²)");
            Console.WriteLine(
                $"FFT-свёртка: O({transformSize} * log2({transformSize}))");

            Console.WriteLine("\n========================================");
            Console.WriteLine("       БЕНЧМАРК НА СЛУЧАЙНЫХ ДАННЫХ");
            Console.WriteLine("========================================");

            RunConvolutionBenchmark();

            Console.WriteLine("\n========================================");
            Console.WriteLine("      ТЕСТ УМНОЖЕНИЯ МАТРИЦ");
            Console.WriteLine("========================================");

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

            Console.WriteLine("\n========================================");
            Console.WriteLine("      ТЕСТ ФОРДА–БЕЛЛМАНА");
            Console.WriteLine("========================================");

            var edges = new List<Edge>
            {
                new Edge(0, 1, 4),
                new Edge(0, 2, 5),
                new Edge(1, 2, -3),
                new Edge(1, 3, 1),
                new Edge(2, 3, 4)
            };

            int verticesCount = 4;
            int source = 0;

            var fbResult = FordBellman.Run(verticesCount, edges, source);

            FordBellman.PrintResult(
                "Кратчайшие пути из вершины 0",
                fbResult,
                source);
        }

        private static void RunConvolutionBenchmark()
        {
            int[] sizes = { 16, 32, 64, 128, 256, 512, 1024 };
            Random random = new Random(42);

            Console.WriteLine(
                "Размер | Прямая, мс | DFT, мс | FFT, мс | " +
                "Операции direct | Операции DFT | Операции FFT");

            Console.WriteLine(new string('-', 105));

            foreach (int size in sizes)
            {
                double[] signal = CreateRandomArray(size, random);
                double[] kernel = CreateRandomArray(size, random);

                var simpleCounter = new ConvolutionDemo.OperationsCounter();
                var dftCounter = new ConvolutionDemo.OperationsCounter();
                var fftCounter = new ConvolutionDemo.OperationsCounter();

                Stopwatch sw = Stopwatch.StartNew();
                double[] simple = ConvolutionDemo.SimpleConvolve(
                    signal,
                    kernel,
                    simpleCounter);
                sw.Stop();
                double simpleMs = sw.Elapsed.TotalMilliseconds;

                sw.Restart();
                double[] dft = ConvolutionDemo.DFTConvolve(
                    signal,
                    kernel,
                    dftCounter);
                sw.Stop();
                double dftMs = sw.Elapsed.TotalMilliseconds;

                sw.Restart();
                double[] fft = ConvolutionDemo.FFTConvolve(
                    signal,
                    kernel,
                    fftCounter);
                sw.Stop();
                double fftMs = sw.Elapsed.TotalMilliseconds;

                bool dftCorrect = ConvolutionDemo.ArraysAlmostEqual(
                    simple,
                    dft,
                    1e-5);

                bool fftCorrect = ConvolutionDemo.ArraysAlmostEqual(
                    simple,
                    fft,
                    1e-5);

                Console.WriteLine(
                    $"{size,5} | " +
                    $"{simpleMs,11:F3} | " +
                    $"{dftMs,7:F3} | " +
                    $"{fftMs,7:F3} | " +
                    $"{simpleCounter.TotalArithmeticOperations,17} | " +
                    $"{dftCounter.TotalArithmeticOperations,14} | " +
                    $"{fftCounter.TotalArithmeticOperations,14}");

                if (!dftCorrect || !fftCorrect)
                {
                    Console.WriteLine(
                        $"  ВНИМАНИЕ: ошибка проверки результата для размера {size}. " +
                        $"DFT: {dftCorrect}, FFT: {fftCorrect}");
                }
            }

            Console.WriteLine();
            Console.WriteLine("Примечание:");
            Console.WriteLine("- На малых массивах прямая свёртка может быть быстрее.");
            Console.WriteLine("- DFT почти всегда заметно медленнее FFT.");
            Console.WriteLine("- На больших массивах преимущество FFT становится существенным.");
            Console.WriteLine("- Время зависит от CPU, режима Debug/Release и фоновой нагрузки.");
        }

        private static double[] CreateRandomArray(int size, Random random)
        {
            double[] result = new double[size];

            for (int i = 0; i < size; i++)
            {
                result[i] = random.NextDouble() * 20.0 - 10.0;
            }

            return result;
        }

        private static void PrintStatistics(
            string algorithmName,
            TimeSpan elapsed,
            ConvolutionDemo.OperationsCounter counter)
        {
            Console.WriteLine($"{algorithmName}:");
            Console.WriteLine($"Время: {elapsed.TotalMilliseconds:F6} мс");
            Console.WriteLine($"Операции: {counter}");
        }
    }
}