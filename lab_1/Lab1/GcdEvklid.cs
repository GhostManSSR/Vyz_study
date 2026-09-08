namespace Lab1;

public class GcdEvklid
{
    private readonly Random _random = new();
    
    private readonly FastModularExponentiation _fastModular;

    public GcdEvklid(FastModularExponentiation fastModular)
    {
        _fastModular = fastModular;
    }

    public long Gcd(long a, long b)
    {
        a = Math.Abs(a);
        b = Math.Abs(b);

        while (b != 0)
        {
            (a, b) = (b, a % b);
        }

        return a;
    }

    public long ExtendedGcd(long a, long b, out long x, out long y)
    {
        long oldR = Math.Abs(a);
        long r = Math.Abs(b);

        long oldX = 1;
        long currentX = 0;

        long oldY = 0;
        long currentY = 1;

        while (r != 0)
        {
            long q = oldR / r;

            (oldR, r) = (r, oldR - q * r);
            (oldX, currentX) = (currentX, oldX - q * currentX);
            (oldY, currentY) = (currentY, oldY - q * currentY);
        }

        x = a < 0 ? -oldX : oldX;
        y = b < 0 ? -oldY : oldY;

        return oldR;
    }

    public (long gcd, long x, long y) SolveExtendedGcd(long a, long b)
    {
        long gcd = ExtendedGcd(a, b, out long x, out long y);

        return (gcd, x, y);
    }

    public long GenerateNumber(long min, long max)
    {
        if (min > max)
            throw new ArgumentException(
                "Минимальное значение не может быть больше максимального.");

        return _random.NextInt64(min, max + 1);
    }

    public (long a, long b) GenerateNumbers(long min, long max)
    {
        return (
            GenerateNumber(min, max),
            GenerateNumber(min, max)
        );
    }

    public (long a, long b) GenerateCoprimeNumbers(long min, long max)
    {
        long a;
        long b;

        do
        {
            a = GenerateNumber(min, max);
            b = GenerateNumber(min, max);
        }
        while (Gcd(a, b) != 1);

        return (a, b);
    }

    public long ReadNumber(string message)
    {
        while (true)
        {
            Console.Write(message);

            if (long.TryParse(Console.ReadLine(), out long number))
                return number;

            Console.WriteLine(
                "Ошибка: необходимо ввести целое число.");
        }
    }
    
    
    public (long a, long b) GetNumbers(int mode, long min = 2, long max = 1000, int iterations = 20)
    {
        return mode switch
        {
            1 => ReadNumbers(),

            2 => GenerateNumbers(
                min,
                max),

            3 => GeneratePrimeNumbers(
                min,
                max,
                iterations),

            _ => throw new ArgumentException(
                "Неизвестный режим генерации.")
        };
    }
    
    public (long a, long b) GeneratePrimeNumbers(
        long min,
        long max,
        int iterations = 20)
    {
        long a = GeneratePrime(
            min,
            max,
            iterations);

        long b = GeneratePrime(
            min,
            max,
            iterations);

        return (a, b);
    }
    
    
    public long GeneratePrime(long min, long max, int iterations = 20)
    {
        if (min < 2)
        {
            min = 2;
        }

        if (min > max)
        {
            throw new ArgumentException(
                "Минимальное значение не может быть больше максимального.");
        }

        while (true)
        {
            long number = GenerateNumber(min, max);

            if (IsPrimeFermat(number, iterations))
            {
                return number;
            }
        }
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
    
    public long RandomLong(long min, long max)
    {
        if (min > max)
        {
            throw new ArgumentException(
                "Минимальное значение не может быть больше максимального.");
        }

        if (min == max)
        {
            return min;
        }

        return _random.NextInt64(min, max + 1);
    }

    public (long a, long b) ReadNumbers()
    {
        return (
            ReadNumber("Введите a: "),
            ReadNumber("Введите b: ")
        );
    }
}