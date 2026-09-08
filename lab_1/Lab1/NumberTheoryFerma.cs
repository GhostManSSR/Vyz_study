namespace Lab1;

public class NumberTheoryFerma
{
    private readonly FastModularExponentiation _fastModular;

    public NumberTheoryFerma(FastModularExponentiation fastModular)
    {
        _fastModular = fastModular;
    }

    public bool IsPrimeFermat(long n, int iterations = 20)
    {
        if (n < 2)
            return false;

        if (n == 2 || n == 3)
            return true;

        if (n % 2 == 0)
            return false;

        Random random = new();

        for (int i = 0; i < iterations; i++)
        {
            long a = random.NextInt64(2, n - 1);

            if (Gcd(a, n) != 1)
                return false;

            if (_fastModular.Solver(a, n - 1, n) != 1)
                return false;
        }

        return true;
    }

    private static long Gcd(long a, long b)
    {
        a = Math.Abs(a);
        b = Math.Abs(b);

        while (b != 0)
        {
            (a, b) = (b, a % b);
        }

        return a;
    }
}