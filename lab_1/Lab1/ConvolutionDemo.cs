using System;
using System.Numerics;

namespace SortingAlgorithms
{
    public class ConvolutionDemo
    {
        public sealed class OperationsCounter
        {
            public long Additions { get; private set; }
            public long Multiplications { get; private set; }
            public long ComplexOperations { get; private set; }

            public long TotalArithmeticOperations =>
                Additions + Multiplications + ComplexOperations;

            public void AddAddition(long count = 1)
            {
                Additions += count;
            }

            public void AddMultiplication(long count = 1)
            {
                Multiplications += count;
            }

            public void AddComplexOperation(long count = 1)
            {
                ComplexOperations += count;
            }

            public void Reset()
            {
                Additions = 0;
                Multiplications = 0;
                ComplexOperations = 0;
            }

            public override string ToString()
            {
                return $"Сложения: {Additions}, " +
                       $"умножения: {Multiplications}, " +
                       $"комплексные операции: {ComplexOperations}, " +
                       $"всего: {TotalArithmeticOperations}";
            }
        }

        /// <summary>
        /// Прямая линейная свёртка.
        /// Сложность: O(n * m).
        /// При n ≈ m ≈ N: O(N²).
        /// </summary>
        public static double[] SimpleConvolve(
            double[] signal,
            double[] kernel,
            OperationsCounter? counter = null)
        {
            ValidateArrays(signal, kernel);

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

                        counter?.AddMultiplication();
                        counter?.AddAddition();
                    }
                }

                result[i] = sum;
            }

            return result;
        }

        /// <summary>
        /// Прямое дискретное преобразование Фурье.
        /// Сложность: O(N²).
        /// Используется для сравнения с FFT.
        /// </summary>
        public static void DFT(
            Complex[] data,
            bool inverse = false,
            OperationsCounter? counter = null)
        {
            int n = data.Length;

            if (n == 0)
            {
                return;
            }

            Complex[] result = new Complex[n];

            double sign = inverse ? 1.0 : -1.0;

            for (int k = 0; k < n; k++)
            {
                Complex sum = Complex.Zero;

                for (int t = 0; t < n; t++)
                {
                    double angle = sign * 2.0 * Math.PI * k * t / n;
                    Complex w = Complex.FromPolarCoordinates(1.0, angle);

                    sum += data[t] * w;

                    counter?.AddComplexOperation(2);
                }

                result[k] = inverse ? sum / n : sum;

                if (inverse)
                {
                    counter?.AddComplexOperation();
                }
            }

            Array.Copy(result, data, n);
        }

        /// <summary>
        /// Свёртка через прямое DFT.
        /// Итоговая сложность: O(N²), где N — fftSize.
        /// </summary>
        public static double[] DFTConvolve(
            double[] signal,
            double[] kernel,
            OperationsCounter? counter = null)
        {
            ValidateArrays(signal, kernel);

            int resultLength = signal.Length + kernel.Length - 1;
            int transformSize = GetNextPowerOfTwo(resultLength);

            Complex[] signalC = new Complex[transformSize];
            Complex[] kernelC = new Complex[transformSize];

            for (int i = 0; i < signal.Length; i++)
            {
                signalC[i] = new Complex(signal[i], 0);
            }

            for (int i = 0; i < kernel.Length; i++)
            {
                kernelC[i] = new Complex(kernel[i], 0);
            }

            DFT(signalC, false, counter);
            DFT(kernelC, false, counter);

            Complex[] product = new Complex[transformSize];

            for (int i = 0; i < transformSize; i++)
            {
                product[i] = signalC[i] * kernelC[i];
                counter?.AddComplexOperation();
            }

            DFT(product, true, counter);

            double[] result = new double[resultLength];

            for (int i = 0; i < resultLength; i++)
            {
                result[i] = product[i].Real;
            }

            return result;
        }

        /// <summary>
        /// Итеративное FFT Cooley-Tukey radix-2.
        /// Длина массива должна быть степенью двойки.
        /// Сложность: O(N log N).
        /// </summary>
        public static void FFT(
            Complex[] data,
            bool inverse = false,
            OperationsCounter? counter = null)
        {
            int n = data.Length;

            if (n <= 1)
            {
                return;
            }

            if (!IsPowerOfTwo(n))
            {
                throw new ArgumentException(
                    "Для FFT длина массива должна быть степенью двойки.");
            }

            int log2n = (int)Math.Log2(n);

            for (int i = 0; i < n; i++)
            {
                int j = ReverseBits(i, log2n);

                if (j > i)
                {
                    Complex temp = data[i];
                    data[i] = data[j];
                    data[j] = temp;
                }
            }

            for (int size = 2; size <= n; size *= 2)
            {
                double angle = 2.0 * Math.PI / size * (inverse ? 1.0 : -1.0);
                Complex wn = Complex.FromPolarCoordinates(1.0, angle);

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

                        counter?.AddComplexOperation(4);
                    }
                }
            }

            if (inverse)
            {
                for (int i = 0; i < n; i++)
                {
                    data[i] /= n;
                    counter?.AddComplexOperation();
                }
            }
        }

        /// <summary>
        /// Свёртка через FFT.
        /// Итоговая сложность: O(N log N).
        /// </summary>
        public static double[] FFTConvolve(
            double[] signal,
            double[] kernel,
            OperationsCounter? counter = null)
        {
            ValidateArrays(signal, kernel);

            int n = signal.Length;
            int m = kernel.Length;
            int resultLength = n + m - 1;

            int fftSize = GetNextPowerOfTwo(resultLength);

            Complex[] signalC = new Complex[fftSize];
            Complex[] kernelC = new Complex[fftSize];

            for (int i = 0; i < n; i++)
            {
                signalC[i] = new Complex(signal[i], 0);
            }

            for (int i = 0; i < m; i++)
            {
                kernelC[i] = new Complex(kernel[i], 0);
            }

            FFT(signalC, false, counter);
            FFT(kernelC, false, counter);

            Complex[] product = new Complex[fftSize];

            for (int i = 0; i < fftSize; i++)
            {
                product[i] = signalC[i] * kernelC[i];
                counter?.AddComplexOperation();
            }

            FFT(product, true, counter);

            double[] result = new double[resultLength];

            for (int i = 0; i < resultLength; i++)
            {
                result[i] = product[i].Real;
            }

            return result;
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

        public static int GetNextPowerOfTwo(int value)
        {
            if (value <= 1)
            {
                return 1;
            }

            int result = 1;

            while (result < value)
            {
                result *= 2;
            }

            return result;
        }

        public static bool IsPowerOfTwo(int value)
        {
            return value > 0 && (value & (value - 1)) == 0;
        }

        public static bool ArraysAlmostEqual(
            double[] first,
            double[] second,
            double epsilon = 1e-6)
        {
            if (first.Length != second.Length)
            {
                return false;
            }

            for (int i = 0; i < first.Length; i++)
            {
                if (Math.Abs(first[i] - second[i]) > epsilon)
                {
                    return false;
                }
            }

            return true;
        }

        public static void PrintArray(string name, double[] arr)
        {
            Console.Write($"{name}: [");

            for (int i = 0; i < arr.Length; i++)
            {
                Console.Write(arr[i].ToString("F4"));

                if (i < arr.Length - 1)
                {
                    Console.Write(", ");
                }
            }

            Console.WriteLine("]");
        }

        private static void ValidateArrays(double[] signal, double[] kernel)
        {
            if (signal == null)
            {
                throw new ArgumentNullException(nameof(signal));
            }

            if (kernel == null)
            {
                throw new ArgumentNullException(nameof(kernel));
            }

            if (signal.Length == 0)
            {
                throw new ArgumentException(
                    "Массив signal не должен быть пустым.",
                    nameof(signal));
            }

            if (kernel.Length == 0)
            {
                throw new ArgumentException(
                    "Массив kernel не должен быть пустым.",
                    nameof(kernel));
            }
        }
    }
}