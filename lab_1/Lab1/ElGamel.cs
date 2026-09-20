using System.Numerics;

namespace Lab1;

public class ElGamel
{
    private readonly FastModularExponentiation _fastModularExponentiation;
    private readonly  NumberTheoryFerma _ferma;
    private Random _random = new Random();
    
    public ElGamel(FastModularExponentiation  fastModularExponentiation)
    {
        _fastModularExponentiation = new FastModularExponentiation();
        _ferma = new NumberTheoryFerma(_fastModularExponentiation);
    }

    private long GeneratePrimeNumber()
    {
        const long minPrime = 100_000;
        const long maxPrime = 1_000_000;

        while (true)
        {
            long candidate = _random.NextInt64(minPrime, maxPrime);

            // Делаем нечётным.
            candidate |= 1L;

            if (_ferma.IsPrimeFermat(candidate))
                return candidate;
        }
    }

    private (long g, long x, long y) GenerateParametersAlice(long p)
    {
        long g = FindPrimitiveRoot(p);

        long x = _random.NextInt64(1, p - 1);

        long y = _fastModularExponentiation.Solver(g, x, p);

        return (g, x, y);
    }
    
    private long FindPrimitiveRoot(long p)
    {
        if (p < 3 || !_ferma.IsPrimeFermat(p))
        {
            throw new ArgumentException(
                "Для поиска генератора p должно быть простым числом больше 2.",
                nameof(p));
        }

        List<long> factors = GetUniquePrimeFactors(p - 1);

        for (long g = 2; g < p; g++)
        {
            bool isPrimitiveRoot = true;

            foreach (long primeFactor in factors)
            {
                long exponent = (p - 1) / primeFactor;

                if (_fastModularExponentiation.Solver(g, exponent, p) == 1)
                {
                    isPrimitiveRoot = false;
                    break;
                }
            }

            if (isPrimitiveRoot)
                return g;
        }

        throw new InvalidOperationException(
            $"Не удалось подобрать первообразный корень для p = {p}.");
    }
    
    public long SolveFromConsole()
    {
        Console.Write("Введите p: ");
        long p = long.Parse(Console.ReadLine()!);

        Console.Write("Введите g: ");
        long g = long.Parse(Console.ReadLine()!);

        Console.Write("Введите x: ");
        long x = long.Parse(Console.ReadLine()!);

        Console.Write("Введите C: ");
        long c = long.Parse(Console.ReadLine()!);

        Console.Write("Введите D: ");
        long d = long.Parse(Console.ReadLine()!);

        // Проверяем p
        if (!_ferma.IsPrimeFermat(p))
            throw new ArgumentException(
                "p должно быть простым числом.");

        // Проверяем g
        if (g <= 1 || g >= p)
            throw new ArgumentException(
                "g должно находиться в диапазоне (1, p).");

        // Проверяем закрытый ключ x
        if (x <= 0 || x >= p - 1)
            throw new ArgumentException(
                "x должно находиться в диапазоне [1, p - 2].");

        // Проверяем C
        if (c <= 0 || c >= p)
            throw new ArgumentException(
                "C должно находиться в диапазоне [1, p - 1].");

        // Проверяем D
        if (d <= 0 || d >= p)
            throw new ArgumentException(
                "D должно находиться в диапазоне [1, p - 1].");

        // Вычисляем открытый ключ:
        // y = g^x mod p
        long y = _fastModularExponentiation.Solver(
            g,
            x,
            p);

        // Расшифровываем:
        // m = D * (C^x)^(-1) mod p
        long decryptedMessage = DecryptAlice(
            c,
            x,
            d,
            p);

        Console.WriteLine();
        Console.WriteLine("Результат:");
        Console.WriteLine($"p = {p}");
        Console.WriteLine($"g = {g}");
        Console.WriteLine($"x = {x}");
        Console.WriteLine($"y = {y}");
        Console.WriteLine($"C = {c}");
        Console.WriteLine($"D = {d}");
        Console.WriteLine($"M = {decryptedMessage}");

        return decryptedMessage;
    }
    
    private static List<long> GetUniquePrimeFactors(long value)
    {
        var factors = new List<long>();

        for (long divisor = 2; divisor <= value / divisor; divisor++)
        {
            if (value % divisor != 0)
                continue;

            factors.Add(divisor);

            while (value % divisor == 0)
            {
                value /= divisor;
            }
        }

        if (value > 1)
            factors.Add(value);

        return factors;
    }

    private (long m, long u, long v) GenerateParametersBobEncrypt(long p, long g, long y)
    {
        var k = _random.NextInt64(1, p - 1);
        var m = _random.NextInt64(1, p);
        var u = _fastModularExponentiation.Solver(g, k, p);
        long sharedSecret = _fastModularExponentiation.Solver(y, k, p);
        long v = FastModularExponentiation.MultiplyModulo(m, sharedSecret, p);
        return (m, u, v);
    }

    private long DecryptAlice(long u, long x, long v, long p)
    {
        // s = u^x mod p = g^(kx) mod p.
        long sharedSecret = _fastModularExponentiation.Solver(u, x, p);

        // s^(-1) mod p = s^(p - 2) mod p,
        // потому что p — простое число.
        long inverseSharedSecret = _fastModularExponentiation.Solver(sharedSecret, p - 2, p);

        // m = v * s^(-1) mod p.
        return FastModularExponentiation.MultiplyModulo(v, inverseSharedSecret, p);
    }
    
    
    public (long u, long v) EncryptBlock(long message, ElGamalKeys keys)
    {
        if (message < 1 || message >= keys.P)
            throw new ArgumentOutOfRangeException(
                nameof(message),
                "Сообщение должно быть в диапазоне [1, p - 1].");

        long k = _random.NextInt64(1, keys.P - 1);

        long u = _fastModularExponentiation.Solver(keys.G, k, keys.P);
        long sharedSecret = _fastModularExponentiation.Solver(keys.Y, k, keys.P);
        long v = FastModularExponentiation.MultiplyModulo(message, sharedSecret, keys.P);

        return (u, v);
    }

    public long DecryptBlock(long u, long v, ElGamalKeys keys)
    {
        long sharedSecret = _fastModularExponentiation.Solver(u, keys.X, keys.P);
        long inverseSharedSecret =
            _fastModularExponentiation.Solver(sharedSecret, keys.P - 2, keys.P);

        return FastModularExponentiation.MultiplyModulo(v, inverseSharedSecret, keys.P);
    }
    
    public ElGamalResult SolverElGamel()
    {
        var p = GeneratePrimeNumber();
        var (g, x, y) = GenerateParametersAlice(p);
        var (m, u, v) = GenerateParametersBobEncrypt(p, g, y);
        var decryptedMessage = DecryptAlice(u, x, v, p);
        var messageM = m;
        if (decryptedMessage != messageM)
        {
            throw new InvalidOperationException(
                $"Расшифрование завершилось ошибкой. " +
                $"Ожидалось: {messageM}; получено: {decryptedMessage}.");
        }

        return new ElGamalResult(p, g, messageM, u, v, decryptedMessage);
    }
    
    /// <summary>
    /// Шифрует любой файл с помощью алгоритма Эль-Гамаля.
    ///
    /// Формат зашифрованного файла:
    /// 4 байта  - сигнатура "EG01"
    /// 8 байт  - количество исходных байт
    /// Для каждого байта:
    ///     8 байт - u
    ///     8 байт - v
    /// </summary>
    public void EncryptFile(
        string inputPath,
        string outputPath,
        ElGamalKeys keys)
    {
        if (string.IsNullOrWhiteSpace(inputPath))
            throw new ArgumentException(
                "Не указан исходный файл.",
                nameof(inputPath));

        if (string.IsNullOrWhiteSpace(outputPath))
            throw new ArgumentException(
                "Не указан выходной файл.",
                nameof(outputPath));

        if (!File.Exists(inputPath))
            throw new FileNotFoundException(
                "Исходный файл не найден.",
                inputPath);

        ValidateKeys(keys);

        if (Path.GetFullPath(inputPath) == Path.GetFullPath(outputPath))
            throw new ArgumentException(
                "Исходный и выходной файлы не должны совпадать.");

        using var input = new FileStream(
            inputPath,
            FileMode.Open,
            FileAccess.Read,
            FileShare.Read);

        using var output = new FileStream(
            outputPath,
            FileMode.Create,
            FileAccess.Write,
            FileShare.None);

        using var writer = new BinaryWriter(output);

        // Сигнатура формата файла.
        writer.Write(new byte[] { (byte)'E', (byte)'G', (byte)'0', (byte)'1' });

        // Записываем размер исходного файла.
        writer.Write(input.Length);

        int currentByte;

        while ((currentByte = input.ReadByte()) != -1)
        {
            // byte 0..255 преобразуем в 1..256.
            long message = currentByte + 1L;

            var (u, v) = EncryptBlock(message, keys);

            writer.Write(u);
            writer.Write(v);
        }
    }

    /// <summary>
    /// Расшифровывает файл, созданный методом EncryptFile.
    /// </summary>
    public void DecryptFile(
        string inputPath,
        string outputPath,
        ElGamalKeys keys)
    {
        if (string.IsNullOrWhiteSpace(inputPath))
            throw new ArgumentException(
                "Не указан зашифрованный файл.",
                nameof(inputPath));

        if (string.IsNullOrWhiteSpace(outputPath))
            throw new ArgumentException(
                "Не указан выходной файл.",
                nameof(outputPath));

        if (!File.Exists(inputPath))
            throw new FileNotFoundException(
                "Зашифрованный файл не найден.",
                inputPath);

        ValidateKeys(keys);

        if (Path.GetFullPath(inputPath) == Path.GetFullPath(outputPath))
            throw new ArgumentException(
                "Исходный и выходной файлы не должны совпадать.");

        using var input = new FileStream(
            inputPath,
            FileMode.Open,
            FileAccess.Read,
            FileShare.Read);

        using var reader = new BinaryReader(input);

        using var output = new FileStream(
            outputPath,
            FileMode.Create,
            FileAccess.Write,
            FileShare.None);

        // Проверяем сигнатуру.
        byte[] signature = reader.ReadBytes(4);

        if (signature.Length != 4 ||
            signature[0] != 'E' ||
            signature[1] != 'G' ||
            signature[2] != '0' ||
            signature[3] != '1')
        {
            throw new InvalidOperationException(
                "Файл не является файлом ElGamal.");
        }

        long fileSize = reader.ReadInt64();

        if (fileSize < 0)
        {
            throw new InvalidOperationException(
                "Некорректный размер исходного файла.");
        }

        long expectedSize = 4 + 8 + fileSize * 16;

        if (input.Length != expectedSize)
        {
            throw new InvalidOperationException(
                "Зашифрованный файл повреждён или имеет некорректный формат.");
        }

        for (long i = 0; i < fileSize; i++)
        {
            long u = reader.ReadInt64();
            long v = reader.ReadInt64();

            long message = DecryptBlock(u, v, keys);

            long originalByte = message - 1;

            if (originalByte < 0 || originalByte > 255)
            {
                throw new InvalidOperationException(
                    $"Получено некорректное значение байта: {originalByte}.");
            }

            output.WriteByte((byte)originalByte);
        }
    }

    public ElGamalKeys GenerateKeys()
    {
        long p = GeneratePrimeNumber();
        var (g, x, y) = GenerateParametersAlice(p);

        return new ElGamalKeys(p, g, x, y);
    }
    
    /// <summary>
    /// Проверяет корректность ключей Эль-Гамаля.
    /// </summary>
    private void ValidateKeys(ElGamalKeys keys)
    {
        if (keys == null)
            throw new ArgumentNullException(nameof(keys));

        if (keys.P <= 256)
            throw new ArgumentException(
                "P должно быть больше 256.");

        if (!_ferma.IsPrimeFermat(keys.P))
            throw new ArgumentException(
                "P должно быть простым числом.");

        if (keys.G <= 1 || keys.G >= keys.P)
            throw new ArgumentException(
                "G должно находиться в диапазоне (1, P).");

        if (keys.X <= 0 || keys.X >= keys.P - 1)
            throw new ArgumentException(
                "X должно находиться в диапазоне [1, P - 2].");

        if (keys.Y <= 0 || keys.Y >= keys.P)
            throw new ArgumentException(
                "Y должно находиться в диапазоне [1, P - 1].");
    }
    
}