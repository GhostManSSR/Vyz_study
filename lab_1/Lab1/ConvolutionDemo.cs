using System.Numerics;

namespace SortingAlgorithms;

public class ConvolutionDemo
{
        public static double[] SimpleConvolve(double[] signal, double[] kernel)
        {
            int n = signal.Length;
            int m = kernel.Length;
            int resultLength = n + m - 1;
            double[] result = new double[resultLength];

            for (int i = 0; i < resultLength; i++)
            {
                double sum = 0;
                for (int j = 0; j < m; j++)
                {
                    int k = i - j;
                    if (k >= 0 && k < n)
                    {
                        sum += signal[k] * kernel[j];
                    }
                }
                result[i] = sum;
            }

            return result;
        }

        // Быстрое преобразование Фурье (FFT)
        public static void FFT(Complex[] data, bool inverse = false)
        {
            int n = data.Length;
            if (n <= 1) return;

            // Bit-reversal permutation
            int log2n = (int)Math.Log2(n);
            for (int i = 0; i < n; i++)
            {
                int j = ReverseBits(i, log2n);
                if (j > i)
                {
                    var temp = data[i];
                    data[i] = data[j];
                    data[j] = temp;
                }
            }

            // Cooley-Tukey FFT
            for (int size = 2; size <= n; size *= 2)
            {
                double angle = 2 * Math.PI / size * (inverse ? 1 : -1);
                Complex wn = Complex.FromPolarCoordinates(1, angle);

                for (int i = 0; i < n; i += size)
                {
                    Complex w = Complex.One;
                    for (int j = 0; j < size / 2; j++)
                    {
                        Complex t = w * data[i + j + size / 2];
                        Complex u = data[i + j];
                        data[i + j] = u + t;
                        data[i + j + size / 2] = u - t;
                        w *= wn;
                    }
                }
            }

            if (inverse)
            {
                for (int i = 0; i < n; i++)
                {
                    data[i] /= n;
                }
            }
        }

        public static int ReverseBits(int x, int bits)
        {
            int result = 0;
            for (int i = 0; i < bits; i++)
            {
                result = (result << 1) | (x & 1);
                x >>= 1;
            }
            return result;
        }

        // Свёртка через FFT (требует длины степени двойки)
        public static double[] FFTConvolve(double[] signal, double[] kernel)
        {
            int n = signal.Length;
            int m = kernel.Length;
            int resultLength = n + m - 1;

            // Находим ближайшую степень двойки
            int fftSize = 1;
            while (fftSize < resultLength)
                fftSize *= 2;

            Complex[] signalC = new Complex[fftSize];
            Complex[] kernelC = new Complex[fftSize];

            for (int i = 0; i < n; i++) signalC[i] = new Complex(signal[i], 0);
            for (int i = 0; i < m; i++) kernelC[i] = new Complex(kernel[i], 0);

            // FFT обеих последовательностей
            FFT(signalC, false);
            FFT(kernelC, false);

            // Поэлементное умножение в частотной области
            Complex[] product = new Complex[fftSize];
            for (int i = 0; i < fftSize; i++)
            {
                product[i] = signalC[i] * kernelC[i];
            }

            // Обратное FFT
            FFT(product, true);

            // Берём действительную часть нужной длины
            double[] result = new double[resultLength];
            for (int i = 0; i < resultLength; i++)
            {
                result[i] = product[i].Real;
            }

            return result;
        }

        public static void PrintArray(string name, double[] arr)
        {
            Console.Write($"{name}: [");
            for (int i = 0; i < arr.Length; i++)
            {
                Console.Write(arr[i].ToString("F4"));
                if (i < arr.Length - 1) Console.Write(", ");
            }
            Console.WriteLine("]");
        }
    
}
