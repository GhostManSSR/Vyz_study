using System.Numerics;

namespace SortingAlgorithms;

public class FastFourierTransform
{
     private const double TwoPi = 2.0 * Math.PI;

        /// <summary>
        /// Прямое быстрое преобразование Фурье.
        /// Входной размер должен быть степенью двойки.
        /// Сложность: O(N log N)
        /// </summary>
        public Complex[] Forward(Complex[] input)
        {
            if (input == null)
                throw new ArgumentNullException(nameof(input));

            Complex[] data = (Complex[])input.Clone();

            Transform(data, false);

            return data;
        }

        /// <summary>
        /// Обратное быстрое преобразование Фурье.
        /// </summary>
        public Complex[] Inverse(Complex[] input)
        {
            if (input == null)
                throw new ArgumentNullException(nameof(input));

            Complex[] data = (Complex[])input.Clone();

            Transform(data, true);

            return data;
        }

        /// <summary>
        /// Основной итеративный алгоритм FFT.
        /// </summary>
        private static void Transform(
            Complex[] data,
            bool inverse)
        {
            int n = data.Length;

            ValidateSize(n);

            // -------------------------------------------------
            // 1. Битовая перестановка
            // -------------------------------------------------

            for (int i = 1, j = 0; i < n; i++)
            {
                int bit = n >> 1;

                while ((j & bit) != 0)
                {
                    j ^= bit;
                    bit >>= 1;
                }

                j ^= bit;

                if (i < j)
                {
                    (data[i], data[j]) =
                        (data[j], data[i]);
                }
            }

            // -------------------------------------------------
            // 2. Итеративные стадии FFT
            // -------------------------------------------------

            for (int length = 2;
                 length <= n;
                 length <<= 1)
            {
                double angle =
                    TwoPi / length *
                    (inverse ? 1.0 : -1.0);

                Complex wLength =
                    new Complex(
                        Math.Cos(angle),
                        Math.Sin(angle)
                    );

                int halfLength = length >> 1;

                // Обрабатываем каждый блок
                for (int start = 0;
                     start < n;
                     start += length)
                {
                    Complex w = Complex.One;

                    for (int j = 0;
                         j < halfLength;
                         j++)
                    {
                        int evenIndex = start + j;
                        int oddIndex =
                            evenIndex + halfLength;

                        Complex even =
                            data[evenIndex];

                        Complex odd =
                            data[oddIndex] * w;

                        data[evenIndex] =
                            even + odd;

                        data[oddIndex] =
                            even - odd;

                        w *= wLength;
                    }
                }
            }

            // -------------------------------------------------
            // 3. Нормализация обратного FFT
            // -------------------------------------------------

            if (inverse)
            {
                double scale = 1.0 / n;

                for (int i = 0; i < n; i++)
                {
                    data[i] *= scale;
                }
            }
        }

        private static void ValidateSize(int n)
        {
            if (n == 0 || (n & (n - 1)) != 0)
            {
                throw new ArgumentException(
                    "Размер массива должен быть степенью двойки."
                );
            }
        }
}