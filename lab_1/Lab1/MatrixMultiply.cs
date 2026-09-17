namespace SortingAlgorithms;

public class MatrixMultiply
{
    public static double[,] MultiplyNaive(double[,] a, double[,] b)
        {
            int n1 = a.GetLength(0);
            int m1 = a.GetLength(1);
            int n2 = b.GetLength(0);
            int m2 = b.GetLength(1);

            if (m1 != n2)
                throw new ArgumentException("Размеры матриц несовместимы для умножения.");

            double[,] c = new double[n1, m2];

            for (int i = 0; i < n1; i++)
            {
                for (int j = 0; j < m2; j++)
                {
                    double sum = 0;
                    for (int k = 0; k < m1; k++)
                    {
                        sum += a[i, k] * b[k, j];
                    }
                    c[i, j] = sum;
                }
            }

            return c;
        }

        public static double[,] MultiplyStrassen(double[,] a, double[,] b)
        {
            int n = a.GetLength(0);
            int m = a.GetLength(1);
            int n2 = b.GetLength(0);
            int m2 = b.GetLength(1);

            if (n != m || n2 != m2 || n != n2)
                throw new ArgumentException("Алгоритм Штрассена реализован для квадратных матриц одинакового размера.");

            int size = 1;
            while (size < n) size <<= 1;

            double[,] aPadded = new double[size, size];
            double[,] bPadded = new double[size, size];

            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                {
                    aPadded[i, j] = a[i, j];
                    bPadded[i, j] = b[i, j];
                }

            double[,] cPadded = StrassenRecursive(aPadded, bPadded, size);

            double[,] c = new double[n, n];
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    c[i, j] = cPadded[i, j];

            return c;
        }

        private static double[,] StrassenRecursive(double[,] a, double[,] b, int size)
        {
            if (size <= 64)
                return MultiplyNaiveBlock(a, b, size);

            int half = size >> 1;

            var (a11, a12, a21, a22) = GetBlocks(a, half, size);
            var (b11, b12, b21, b22) = GetBlocks(b, half, size);

            double[,] m1 = StrassenRecursive(Add(a11, a22), Add(b11, b22), half);
            double[,] m2 = StrassenRecursive(Add(a21, a22), b11, half);
            double[,] m3 = StrassenRecursive(a11, Subtract(b12, b22), half);
            double[,] m4 = StrassenRecursive(a22, Subtract(b21, b11), half);
            double[,] m5 = StrassenRecursive(Add(a11, a12), b22, half);
            double[,] m6 = StrassenRecursive(Subtract(a21, a11), Add(b11, b12), half);
            double[,] m7 = StrassenRecursive(Subtract(a12, a22), Add(b21, b22), half);

            double[,] c11 = Add(Subtract(Add(m1, m4), m5), m7);
            double[,] c12 = Add(m3, m5);
            double[,] c21 = Add(m2, m4);
            double[,] c22 = Add(Subtract(Add(m1, m3), m2), m6);

            return CombineBlocks(c11, c12, c21, c22, half, size);
        }

        private static double[,] MultiplyNaiveBlock(double[,] a, double[,] b, int size)
        {
            double[,] c = new double[size, size];
            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++)
                {
                    double sum = 0;
                    for (int k = 0; k < size; k++)
                        sum += a[i, k] * b[k, j];
                    c[i, j] = sum;
                }
            return c;
        }

        private static (double[,], double[,], double[,], double[,]) GetBlocks(
            double[,] m, int half, int size)
        {
            double[,] m11 = new double[half, half];
            double[,] m12 = new double[half, half];
            double[,] m21 = new double[half, half];
            double[,] m22 = new double[half, half];

            for (int i = 0; i < half; i++)
            for (int j = 0; j < half; j++)
            {
                m11[i, j] = m[i, j];
                m12[i, j] = m[i, j + half];
                m21[i, j] = m[i + half, j];
                m22[i, j] = m[i + half, j + half];
            }

            return (m11, m12, m21, m22);
        }

        private static double[,] CombineBlocks(
            double[,] c11, double[,] c12, double[,] c21, double[,] c22,
            int half, int size)
        {
            double[,] c = new double[size, size];
            for (int i = 0; i < half; i++)
            for (int j = 0; j < half; j++)
            {
                c[i, j] = c11[i, j];
                c[i, j + half] = c12[i, j];
                c[i + half, j] = c21[i, j];
                c[i + half, j + half] = c22[i, j];
            }
            return c;
        }

        private static double[,] Add(double[,] a, double[,] b)
        {
            int n = a.GetLength(0);
            int m = a.GetLength(1);
            double[,] c = new double[n, m];
            for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                c[i, j] = a[i, j] + b[i, j];
            return c;
        }

        private static double[,] Subtract(double[,] a, double[,] b)
        {
            int n = a.GetLength(0);
            int m = a.GetLength(1);
            double[,] c = new double[n, m];
            for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                c[i, j] = a[i, j] - b[i, j];
            return c;
        }

        public static void PrintMatrix(string name, double[,] m)
        {
            int n = m.GetLength(0);
            int cols = m.GetLength(1);
            Console.WriteLine($"{name}:");
            for (int i = 0; i < n; i++)
            {
                for (int j = 0; j < cols; j++)
                {
                    Console.Write($"{m[i, j],8:F2}");
                }
                Console.WriteLine();
            }
            Console.WriteLine();
        }
}