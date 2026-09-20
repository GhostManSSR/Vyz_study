namespace Lab1;

public class RSA
{
    private readonly FastModularExponentiation _fastModular;
    private readonly NumberTheoryFerma _ferma;

    public long P { get; private set; }
    public long Q { get; private set; }

    public long N { get; private set; }
    public long Phi { get; private set; }

    // Открытая экспонента.
    // В некоторых методичках обозначается как C.
    public long E { get; private set; }

    // Закрытая экспонента.
    // В методичке обозначается как D.
    public long D { get; private set; }

    public RSA(
        FastModularExponentiation fastModular,
        NumberTheoryFerma ferma)
    {
        _fastModular = fastModular;
        _ferma = ferma;
    }

    /// <summary>
    /// Автоматическая генерация p, q, E и D.
    /// </summary>
    public void GenerateKeys(
        long minPrime = 257,
        long maxPrime = 1000)
    {
        if (minPrime < 2)
            minPrime = 2;

        if (minPrime > maxPrime)
            throw new ArgumentException(
                "Минимальное значение не может быть больше максимального.");

        P = _fastModular.GeneratePrime(
            minPrime,
            maxPrime);

        do
        {
            Q = _fastModular.GeneratePrime(
                minPrime,
                maxPrime);
        }
        while (Q == P);

        InitializeKeys();
    }

    // /// <summary>
    // /// Создание RSA с заданными p и q.
    // /// D вычисляется автоматически.
    // /// </summary>
    // public void GenerateKeys(long p, long q)
    // {
    //     ValidatePrime(p, nameof(p));
    //     ValidatePrime(q, nameof(q));
    //
    //     ValidateDifferentPrimes(p, q);
    //
    //     P = p;
    //     Q = q;
    //
    //     InitializeKeys();
    // }

    /// <summary>
    /// Создание RSA с заданными p, q и D.
    /// </summary>
    public void GenerateKeys(
        long p,
        long q,
        long d)
    {
        ValidatePrime(p, nameof(p));
        ValidatePrime(q, nameof(q));

        ValidateDifferentPrimes(p, q);

        P = p;
        Q = q;

        N = P * Q;
        Phi = (P - 1) * (Q - 1);

        if (N <= byte.MaxValue)
        {
            throw new ArgumentException(
                "Произведение p * q должно быть больше 255.");
        }

        if (d <= 1 || d >= Phi)
        {
            throw new ArgumentException(
                "D должно удовлетворять условию 1 < D < φ(n).");
        }

        if (Gcd(d, Phi) != 1)
        {
            throw new ArgumentException(
                "D должно быть взаимно простым с φ(n).");
        }

        D = d;

        // E — обратный элемент D по модулю φ(n).
        E = ModularInverse(D, Phi);
    }

    /// <summary>
    /// Ввод p, q и D с клавиатуры.
    /// </summary>
    public void GenerateKeysFromConsole()
    {
        Console.WriteLine("=== Генерация RSA ключей ===");

        long p = _fastModular.ReadNumber(
            "Введите p: ");

        long q = _fastModular.ReadNumber(
            "Введите q: ");

        long d = _fastModular.ReadNumber(
            "Введите D: ");

        GenerateKeys(p, q, d);
    }

    /// <summary>
    /// Автоматическая генерация ключей
    /// с использованием FastModularExponentiation.
    /// </summary>
    // public void GenerateKeysAutomatically(
    //     long minPrime = 257,
    //     long maxPrime = 1000)
    // {
    //     GenerateKeys(
    //         minPrime,
    //         maxPrime);
    // }

    private void InitializeKeys()
    {
        N = P * Q;
        Phi = (P - 1) * (Q - 1);

        if (N <= byte.MaxValue)
        {
            throw new ArgumentException(
                "Произведение p * q должно быть больше 255.");
        }

        // Сначала пробуем стандартное значение.
        E = 65537;

        // Для учебных небольших чисел 65537 может быть
        // больше φ(n), поэтому подбираем небольшое E.
        if (E >= Phi || Gcd(E, Phi) != 1)
        {
            E = 3;

            while (
                E < Phi &&
                Gcd(E, Phi) != 1)
            {
                E += 2;
            }
        }

        if (E >= Phi)
        {
            throw new InvalidOperationException(
                "Не удалось подобрать открытую экспоненту E.");
        }

        D = ModularInverse(E, Phi);
    }

    /// <summary>
    /// Шифрование одного байта.
    /// </summary>
    public long EncryptByte(byte value)
    {
        ValidateKeys();

        if (value >= N)
        {
            throw new ArgumentException(
                $"Байт {value} не может быть зашифрован " +
                $"при N = {N}.");
        }

        return _fastModular.Solver(
            value,
            E,
            N);
    }

    /// <summary>
    /// Расшифрование одного блока.
    /// </summary>
    public byte DecryptByte(long encrypted)
    {
        ValidateKeys();

        if (encrypted < 0 || encrypted >= N)
        {
            throw new ArgumentException(
                "Зашифрованное значение находится " +
                "вне диапазона RSA.");
        }

        long result = _fastModular.Solver(
            encrypted,
            D,
            N);

        if (result < byte.MinValue ||
            result > byte.MaxValue)
        {
            throw new InvalidOperationException(
                $"Результат расшифрования {result} " +
                "не является байтом. " +
                "Вероятно, используется неправильный ключ.");
        }

        return (byte)result;
    }

    /// <summary>
    /// Шифрование любого файла.
    /// </summary>
    public void EncryptFile(
        string inputFile,
        string outputFile)
    {
        ValidateKeys();

        byte[] data =
            File.ReadAllBytes(inputFile);

        using FileStream output = new(
            outputFile,
            FileMode.Create,
            FileAccess.Write);

        using BinaryWriter writer =
            new(output);

        // Сохраняем размер исходного файла.
        writer.Write(data.Length);

        foreach (byte value in data)
        {
            long encrypted =
                EncryptByte(value);

            writer.Write(encrypted);
        }
    }

    /// <summary>
    /// Расшифрование любого файла.
    /// </summary>
    public void DecryptFile(
        string inputFile,
        string outputFile)
    {
        ValidateKeys();

        using FileStream input = new(
            inputFile,
            FileMode.Open,
            FileAccess.Read);

        using BinaryReader reader =
            new(input);

        int originalLength =
            reader.ReadInt32();

        if (originalLength < 0)
        {
            throw new InvalidOperationException(
                "Некорректный размер исходного файла.");
        }

        byte[] result =
            new byte[originalLength];

        for (int i = 0;
             i < originalLength;
             i++)
        {
            long encrypted =
                reader.ReadInt64();

            long decrypted =
                _fastModular.Solver(
                    encrypted,
                    D,
                    N);

            // При правильном ключе результат обязательно
            // должен соответствовать исходному байту.
            if (decrypted < byte.MinValue ||
                decrypted > byte.MaxValue)
            {
                throw new InvalidOperationException(
                    $"Результат расшифрования {decrypted} " +
                    "не является байтом. " +
                    "Вероятно, используется неправильный ключ.");
            }

            result[i] =
                (byte)decrypted;
        }

        File.WriteAllBytes(
            outputFile,
            result);
    }

    private void ValidateKeys()
    {
        if (P <= 0 ||
            Q <= 0 ||
            N <= 0 ||
            Phi <= 0 ||
            E <= 0 ||
            D <= 0)
        {
            throw new InvalidOperationException(
                "RSA-ключи ещё не сгенерированы.");
        }
    }

    private void ValidatePrime(
        long value,
        string name)
    {
        if (!_ferma.IsPrimeFermat(value))
        {
            throw new ArgumentException(
                $"{name} должно быть простым числом.");
        }
    }

    private static void ValidateDifferentPrimes(
        long p,
        long q)
    {
        if (p == q)
        {
            throw new ArgumentException(
                "p и q должны отличаться.");
        }
    }

    private static long Gcd(
        long a,
        long b)
    {
        a = Math.Abs(a);
        b = Math.Abs(b);

        while (b != 0)
        {
            (a, b) =
                (b, a % b);
        }

        return a;
    }

    /// <summary>
    /// Расширенный алгоритм Евклида.
    /// Поиск обратного элемента:
    /// a * x ≡ 1 (mod modulus)
    /// </summary>
    private static long ModularInverse(
        long a,
        long modulus)
    {
        long oldR = a;
        long r = modulus;

        long oldS = 1;
        long s = 0;

        while (r != 0)
        {
            long quotient =
                oldR / r;

            long tempR =
                oldR - quotient * r;

            oldR = r;
            r = tempR;

            long tempS =
                oldS - quotient * s;

            oldS = s;
            s = tempS;
        }

        if (oldR != 1)
        {
            throw new ArgumentException(
                "Обратного элемента не существует.");
        }

        long result =
            oldS % modulus;

        if (result < 0)
            result += modulus;

        return result;
    }
}