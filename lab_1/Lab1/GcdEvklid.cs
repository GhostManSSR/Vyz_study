namespace Lab1;

public class GcdEvklid
{
    private readonly Random _random = new Random();

    private readonly NumberTheoryFerma _ferma;
    
    public GcdEvklid(FastModularExponentiation fastModularExponentiation)
    {
        _ferma = new NumberTheoryFerma(fastModularExponentiation, this);
    }
    
    public long Gcd(long a, long b)
    {
        long U = Math.Abs(a);
        long V = Math.Abs(b);
        long T;
        long q;

        while (V != 0)
        {
            q = U / V;

            T = U % V;

            U = V;
            V = T;
        }

        return U;
    }

    public long ExtendedGcd(
        long a,
        long b,
        out long x,
        out long y)
    {
        long originalA = a;
        long originalB = b;

        long U = Math.Abs(a);
        long V = Math.Abs(b);

        long T;
        long q;

        long Ux = 1;
        long Uy = 0;

        long Vx = 0;
        long Vy = 1;

        while (V != 0)
        {
            q = U / V;

            T = U % V;

            long Tx = Ux - q * Vx;
            long Ty = Uy - q * Vy;

            U = V;
            Ux = Vx;
            Uy = Vy;
            V = T;
            Vx = Tx;
            Vy = Ty;
        }

        x = originalA < 0 ? -Ux : Ux;
        y = originalB < 0 ? -Uy : Uy;

        return U;
    }

    public long ModularInverse(long a, long modulus)
    {
        if (modulus <= 1)
            throw new ArgumentException(
                "Модуль должен быть больше 1.");

        long gcd = ExtendedGcd(
            a,
            modulus,
            out long x,
            out _);

        if (gcd != 1)
        {
            throw new ArgumentException(
                "Обратного элемента не существует, " +
                "так как числа не являются взаимно простыми.");
        }

        // Приводим x к диапазону [0, modulus - 1]
        return ((x % modulus) + modulus) % modulus;
    }

    public long GenerateNumber(long min, long max)
    {
        return RandomLong(min, max);
    }

    public (long a, long b) GenerateNumbers(
        long min,
        long max)
    {
        long a = GenerateNumber(min, max);
        long b = GenerateNumber(min, max);

        return (a, b);
    }

    public long RandomLong(long min, long max)
    {
        if (min > max)
        {
            throw new ArgumentException(
                "Минимальное значение не может быть больше максимального.");
        }

        return _random.NextInt64(min, max + 1);
    }
}