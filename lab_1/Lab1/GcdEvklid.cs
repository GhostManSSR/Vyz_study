namespace Lab1;

public class GcdEvklid
{
    private readonly Random _random = new();
    private readonly NumberTheoryFerma _ferma;

    public GcdEvklid(FastModularExponentiation fastModularExponentiation)
    {
        _ferma = new NumberTheoryFerma(
            fastModularExponentiation,
            this);
    }

    /// <summary>
    /// Обычный алгоритм Евклида.
    /// Возвращает НОД(a, b).
    /// </summary>
    public long Gcd(long a, long b)
    {
        long u = Math.Abs(a);
        long v = Math.Abs(b);

        while (v != 0)
        {
            long temp = u % v;
            u = v;
            v = temp;
        }

        return u;
    }

    public long ExtendedGcd(
        long a,
        long b,
        out long x,
        out long y)
    {
        if (a == 0 && b == 0)
        {
            x = 0;
            y = 0;
            return 0;
        }

        long originalA = a;
        long originalB = b;

        long u = Math.Abs(a);
        long v = Math.Abs(b);

        long ux = 1;
        long uy = 0;

        long vx = 0;
        long vy = 1;

        while (v != 0)
        {
            long q = u / v;

            long temp = u % v;
            u = v;
            v = temp;

            long tempX = ux - q * vx;
            long tempY = uy - q * vy;

            ux = vx;
            uy = vy;

            vx = tempX;
            vy = tempY;
        }

        x = originalA < 0 ? -ux : ux;
        y = originalB < 0 ? -uy : uy;

        return u;
    }

    /// <summary>
    /// Решает уравнение:
    ///
    /// a*x + b*y = gcd(a,b)
    ///
    /// Возвращает gcd, x и y.
    /// </summary>
    public (long gcd, long x, long y) SolveExtendedGcd(
        long a,
        long b)
    {
        long gcd = ExtendedGcd(
            a,
            b,
            out long x,
            out long y);

        return (gcd, x, y);
    }

    /// <summary>
    /// Генерирует случайное число в диапазоне [min, max].
    /// </summary>
    public long GenerateNumber(long min, long max)
    {
        return RandomLong(min, max);
    }

    /// <summary>
    /// Генерирует случайное число.
    /// </summary>
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

    /// <summary>
    /// Генерирует два произвольных числа.
    /// </summary>
    public (long a, long b) GenerateNumbers(
        long min,
        long max)
    {
        long a = GenerateNumber(min, max);
        long b = GenerateNumber(min, max);

        return (a, b);
    }

    /// <summary>
    /// Генерирует два взаимно простых числа.
    /// </summary>
    public (long a, long b) GenerateCoprimeNumbers(
        long min,
        long max)
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

    /// <summary>
    /// Генерирует случайное простое число
    /// с использованием теста Ферма.
    /// </summary>
    public long GeneratePrime(
        long min,
        long max,
        int iterations = 20)
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

            if (_ferma.IsPrimeFermat(number, iterations))
            {
                return number;
            }
        }
    }

    /// <summary>
    /// Генерирует два простых числа.
    ///
    /// Проверка каждого числа выполняется
    /// тестом Ферма.
    /// </summary>
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

    /// <summary>
    /// Ввод числа с клавиатуры.
    /// </summary>
    public long ReadNumber(string message)
    {
        while (true)
        {
            Console.Write(message);

            string? input = Console.ReadLine();

            if (long.TryParse(input, out long number))
            {
                return number;
            }

            Console.WriteLine(
                "Ошибка: необходимо ввести целое число.");
        }
    }

    /// <summary>
    /// Ввод двух чисел a и b с клавиатуры.
    /// </summary>
    public (long a, long b) ReadNumbers()
    {
        long a = ReadNumber("Введите a: ");
        long b = ReadNumber("Введите b: ");

        return (a, b);
    }

    /// <summary>
    /// Ввод или генерация a и b в зависимости от выбранного режима.
    ///
    /// 1 - ввод с клавиатуры
    /// 2 - случайная генерация
    /// 3 - генерация простых чисел
    /// </summary>
    public (long a, long b) GetNumbers(
        int mode,
        long min = 2,
        long max = 1000,
        int iterations = 20)
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

    /// <summary>
    /// Нахождение обратного элемента:
    ///
    /// a*x ≡ 1 (mod modulus)
    /// </summary>
    public long ModularInverse(
        long a,
        long modulus)
    {
        if (modulus <= 1)
        {
            throw new ArgumentException(
                "Модуль должен быть больше 1.");
        }

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

        return ((x % modulus) + modulus) % modulus;
    }
}