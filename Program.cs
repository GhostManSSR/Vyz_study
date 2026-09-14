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
            double[] simpleResult = SimpleConvolve(signal, kernel);
            PrintArray("Signal", signal);
            PrintArray("Kernel", kernel);
            PrintArray("Simple Convolution Result", simpleResult);

            Console.WriteLine("\n=== Свёртка через FFT ===");
            double[] fftResult = FFTConvolve(signal, kernel);
            PrintArray("FFT Convolution Result", fftResult);

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
        }
    }
}