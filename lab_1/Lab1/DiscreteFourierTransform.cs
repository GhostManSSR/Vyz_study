using System;
using System.Numerics;

namespace SortingAlgorithms
{
    public class DiscreteFourierTransform
    {
        /// <summary>
        /// Прямое дискретное преобразование Фурье.
        /// Сложность: O(N^2)
        /// </summary>
        public Complex[] Transform(double[] input)
        {
            int n = input.Length;
            Complex[] output = new Complex[n];

            for (int k = 0; k < n; k++)
            {
                Complex sum = Complex.Zero;

                for (int j = 0; j < n; j++)
                {
                    double angle = -2.0 * Math.PI * k * j / n;

                    Complex w = new Complex(
                        Math.Cos(angle),
                        Math.Sin(angle)
                    );

                    sum += input[j] * w;
                }

                output[k] = sum;
            }

            return output;
        }
    

        /// <summary>
        /// Обратное дискретное преобразование Фурье.
        /// Сложность: O(N^2)
        /// </summary>
        public Complex[] InverseTransform(Complex[] input)
        {
            int n = input.Length;
            Complex[] output = new Complex[n];
            for (int j = 0; j < n; j++)
            {
                Complex sum = Complex.Zero;
                for (int k = 0; k < n; k++)
                {
                    double angle = 2.0 * Math.PI * k * j / n;
                    Complex w = new Complex( Math.Cos(angle), Math.Sin(angle) );
                    sum += input[k] * w;
                } 
                output[j] = sum / n;
            }
            return output;
        }
    }
}