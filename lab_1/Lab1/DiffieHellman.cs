namespace Lab1;

public class DiffieHellman
{
    private readonly FastModularExponentiation _fastModular;
    private readonly NumberTheoryFerma _ferma;
    private readonly Random _random = new();

    public DiffieHellman()
    {
        _fastModular = new FastModularExponentiation();
        _ferma = new NumberTheoryFerma(_fastModular);
    }

    /// <summary>
    /// Ввод p, g, Xa, Xb с клавиатуры.
    /// </summary>
    public (long p, long g, long xa, long xb) ReadNumbers()
    {
        long p = _fastModular.ReadNumber("Введите p: ");
        long g = _fastModular.ReadNumber("Введите g: ");
        long xa = _fastModular.ReadNumber("Введите Xa: ");
        long xb = _fastModular.ReadNumber("Введите Xb: ");

        return (p, g, xa, xb);
    }

    /// <summary>
    /// Генерация p, g, Xa, Xb.
    /// </summary>
    public (long p, long g, long xa, long xb) GenerateNumbers(long min = 100, long max = 1000, int iterations = 20)
    {
        long p = GeneratePrime(min, max, iterations);

        long g = GeneratePrimitiveRoot(p);

        long xa = _random.NextInt64(2, p - 1);
        long xb = _random.NextInt64(2, p - 1);

        return (p, g, xa, xb);
    }

    /// <summary>
    /// Режим:
    /// 1 - ввод с клавиатуры
    /// 2 - автоматическая генерация
    /// </summary>
    public (long p, long g, long xa, long xb) GetNumbers(int mode, long min = 100, long max = 1000, int iterations = 20)
    {
        return mode switch
        {
            1 => ReadNumbers(),

            2 => GenerateNumbers(
                min,
                max,
                iterations),

            _ => throw new ArgumentException(
                "Неизвестный режим.")
        };
    }

    /// <summary>
    /// Построение общего ключа Диффи-Хеллмана.
    /// </summary>
    public (long ya, long yb, long keyA, long keyB) GenerateSharedKey(long p, long g, long xa, long xb)
    {
        ValidateParameters(p, g, xa, xb);

        // Открытый ключ абонента A:
        // Ya = g^Xa mod p
        long ya = _fastModular.Solver(g, xa, p);

        // Открытый ключ абонента B:
        // Yb = g^Xb mod p
        long yb = _fastModular.Solver(g, xb, p);

        // Общий ключ A:
        // K = Yb^Xa mod p
        long keyA = _fastModular.Solver(yb, xa, p);

        // Общий ключ B:
        // K = Ya^Xb mod p
        long keyB = _fastModular.Solver(ya, xb, p);

        return (ya, yb, keyA, keyB);
    }

    /// <summary>
    /// Полный алгоритм с автоматической генерацией параметров.
    /// </summary>
    public (long p, long g, long xa, long xb, long ya, long yb, long keyA, long keyB) GenerateSharedKey(long min = 100, long max = 1000, int iterations = 20)
    {
        var parameters = GenerateNumbers(min, max, iterations);

        var result = GenerateSharedKey(parameters.p, parameters.g, parameters.xa, parameters.xb);

        return (parameters.p, parameters.g, parameters.xa, parameters.xb, result.ya, result.yb, result.keyA, result.keyB);
    }

    /// <summary>
    /// Генерация простого числа.
    /// Используется уже существующий класс Ферма.
    /// </summary>
    private long GeneratePrime(long min, long max, int iterations)
    {
        while (true)
        {
            long number =
                _random.NextInt64(min, max + 1);

            if (_ferma.IsPrimeFermat(
                    number,
                    iterations))
            {
                return number;
            }
        }
    }

    /// <summary>
    /// Поиск первообразного корня g по модулю p.
    /// </summary>
    private long GeneratePrimitiveRoot(long p)
    {
        for (long g = 2; g < p; g++)
        {
            if (IsPrimitiveRoot(g, p))
            {
                return g;
            }
        }

        throw new InvalidOperationException(
            "Не удалось найти первообразный корень.");
    }

    /// <summary>
    /// Проверка первообразного корня.
    ///
    /// Для простого p число g является первообразным корнем,
    /// если для каждого простого множителя q числа p - 1:
    ///
    /// g^((p - 1) / q) mod p != 1
    /// </summary>
    private bool IsPrimitiveRoot(long g, long p)
    {
        long phi = p - 1;

        long number = phi;

        for (long q = 2; q * q <= number; q++)
        {
            if (number % q == 0)
            {
                if (_fastModular.Solver(
                        g,
                        phi / q,
                        p) == 1)
                {
                    return false;
                }

                while (number % q == 0)
                {
                    number /= q;
                }
            }
        }

        if (number > 1)
        {
            if (_fastModular.Solver(
                    g,
                    phi / number,
                    p) == 1)
            {
                return false;
            }
        }

        return true;
    }

    /// <summary>
    /// Проверка входных параметров.
    /// </summary>
    private void ValidateParameters(long p, long g, long xa, long xb)
    {
        if (!_ferma.IsPrimeFermat(p))
        {
            throw new ArgumentException(
                "p должно быть простым числом.");
        }

        if (g <= 1 || g >= p)
        {
            throw new ArgumentException(
                "g должно удовлетворять условию 1 < g < p.");
        }

        if (!IsPrimitiveRoot(g, p))
        {
            throw new ArgumentException(
                "g должно быть первообразным корнем по модулю p.");
        }

        if (xa <= 1 || xa >= p)
        {
            throw new ArgumentException(
                "Xa должно удовлетворять условию 1 < Xa < p.");
        }

        if (xb <= 1 || xb >= p)
        {
            throw new ArgumentException(
                "Xb должно удовлетворять условию 1 < Xb < p.");
        }
    }
}