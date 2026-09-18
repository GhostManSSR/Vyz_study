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
    /// Шифрует файл, используя ключи ElGamal.
    /// Формат зашифрованного файла:
    /// 4 байта: количество блоков (int)
    /// далее для каждого блока: 8 байт u (long) + 8 байт v (long).
    /// </summary>
    public void EncryptFile(string inputPath, string outputPath, ElGamalKeys keys)
    {
        if (keys.P <= 256)
            throw new InvalidOperationException(
                "Для побайтового шифрования p должно быть > 256.");

        byte[] plainBytes = File.ReadAllBytes(inputPath);

        using var output = new FileStream(outputPath, FileMode.Create, FileAccess.Write);
        using var writer = new BinaryWriter(output);

        int blockCount = plainBytes.Length;
        writer.Write(blockCount);

        foreach (byte b in plainBytes)
        {
            long message = b + 1L; // чтобы message ∈ [1, 256]

            (long u, long v) = EncryptBlock(message, keys);

            writer.Write(u);
            writer.Write(v);
        }
    }

    /// <summary>
    /// Расшифровывает файл, зашифрованный методом EncryptFile.
    /// </summary>
    public void DecryptFile(string inputPath, string outputPath, ElGamalKeys keys)
    {
        using var input = new FileStream(inputPath, FileMode.Open, FileAccess.Read);
        using var reader = new BinaryReader(input);

        int blockCount = reader.ReadInt32();

        var decryptedBytes = new byte[blockCount];

        for (int i = 0; i < blockCount; i++)
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

            decryptedBytes[i] = (byte)originalByte;
        }

        File.WriteAllBytes(outputPath, decryptedBytes);
    }
}