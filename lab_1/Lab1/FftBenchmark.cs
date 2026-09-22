using System.Diagnostics;
using System.Numerics;

namespace SortingAlgorithms;

public class FftBenchmark
{
    private readonly FastFourierTransform _fft =
        new FastFourierTransform();

    public void Run(int power)
    {
        if (power < 1 || power > 30)
            throw new ArgumentOutOfRangeException(nameof(power));

        int n = 1 << power;

        Console.WriteLine($"N = {n:N0} элементов");
        Console.WriteLine($"log2(N) = {power}");

        long butterflies =
            (long)n * power;

        Console.WriteLine(
            $"Оценка бабочек = {butterflies:N0}");

        Complex[] data = new Complex[n];

        for (int i = 0; i < n; i++)
        {
            data[i] = new Complex(
                Math.Sin(2 * Math.PI * i / n),
                0
            );
        }

        GC.Collect();
        GC.WaitForPendingFinalizers();
        GC.Collect();

        long memoryBefore =
            GC.GetTotalMemory(true);

        Stopwatch stopwatch =
            Stopwatch.StartNew();

        Complex[] result =
            _fft.Forward(data);

        stopwatch.Stop();

        long memoryAfter =
            GC.GetTotalMemory(false);

        Console.WriteLine(
            $"Время FFT: {stopwatch.Elapsed.TotalSeconds:F3} сек");

        Console.WriteLine(
            $"Память: {(memoryAfter - memoryBefore) / 1024.0 / 1024.0:F2} MB");

        Console.WriteLine(
            $"Память массива: {(long)n * 16 / 1024.0 / 1024.0:F2} MB");

        Console.WriteLine(
            $"FFT[0] = {result[0]}");

        Console.WriteLine();
    }
}