using System;
using System.Diagnostics;

namespace SortingAlgorithms
{
    internal class Program
    {
        static void Main(string[] args)
        {
            int[] sizes =
            {
                100,
                500,
                1_000,
                5_000,
                10_000,
                20_000,
                50_000,
                100_000
            };

            SelectSort selectSort = new SelectSort();
            BubbleSort bubbleSort = new BubbleSort();
            MergeSort mergeSort = new MergeSort();

            Random random = new Random(42);

            Console.WriteLine("Сравнение времени сортировок");
            Console.WriteLine();

            Console.WriteLine(
                "{0,-10} {1,-20} {2,-20} {3,-20}",
                "Размер",
                "Selection Sort",
                "Bubble Sort",
                "Merge Sort"
            );

            Console.WriteLine(new string('-', 72));

            foreach (int size in sizes)
            {
                int[] sourceArray = GenerateArray(size, random);

                long selectTime = Measure(
                    () => selectSort.SortAscending(sourceArray)
                );

                long bubbleTime = Measure(
                    () => bubbleSort.SortAscending(sourceArray)
                );

                long mergeTime = Measure(
                    () => mergeSort.SortAscending(sourceArray)
                );

                Console.WriteLine(
                    "{0,-10} {1,-20} {2,-20} {3,-20}",
                    size,
                    FormatTime(selectTime),
                    FormatTime(bubbleTime),
                    FormatTime(mergeTime)
                );
            }

            Console.WriteLine();
            Console.WriteLine("Нажмите Enter для выхода...");
            Console.ReadLine();
        }

        static int[] GenerateArray(int size, Random random)
        {
            int[] array = new int[size];

            for (int i = 0; i < size; i++)
            {
                array[i] = random.Next(0, 1_000_000);
            }

            return array;
        }

        static long Measure(Action action)
        {
            Stopwatch stopwatch = Stopwatch.StartNew();

            action();

            stopwatch.Stop();

            return stopwatch.ElapsedTicks;
        }

        static string FormatTime(long ticks)
        {
            double milliseconds =
                (double)ticks / Stopwatch.Frequency * 1000;

            if (milliseconds < 1)
            {
                return $"{milliseconds:F4} ms";
            }

            return $"{milliseconds:F2} ms";
        }
    }
}