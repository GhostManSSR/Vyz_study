using System.Numerics;
using System.Security.Cryptography;

namespace Lab1;

public class ElGamalSignature
{
      // Простое число p
    private readonly long _p;

    // Примитивный корень по модулю p
    private readonly long _g;

    // Закрытый ключ x
    private readonly long _privateKey;

    // Открытый ключ y = g^x mod p
    private readonly long _publicKey;

    public long P => _p;
    public long G => _g;
    public long PrivateKey => _privateKey;
    public long PublicKey => _publicKey;

    /*
     * Создание параметров Эль-Гамаля.
     *
     * p - простое число
     * g - примитивный корень по модулю p
     * x - закрытый ключ
     */
    public ElGamalSignature(
        long p = 1000003,
        long g = 2,
        long? privateKey = null)
    {
        if (!IsPrime(p))
            throw new ArgumentException(
                "p должно быть простым числом.");

        if (g <= 1 || g >= p)
            throw new ArgumentException(
                "g должно удовлетворять условию 1 < g < p.");

        if (!IsPrimitiveRoot(g, p))
            throw new ArgumentException(
                "g не является примитивным корнем по модулю p.");

        _p = p;
        _g = g;

        /*
         * Закрытый ключ:
         *
         * 1 < x < p - 1
         */
        if (privateKey.HasValue)
        {
            if (privateKey.Value <= 1 ||
                privateKey.Value >= p - 1)
            {
                throw new ArgumentException(
                    "Закрытый ключ x должен удовлетворять " +
                    "1 < x < p - 1.");
            }

            _privateKey = privateKey.Value;
        }
        else
        {
            _privateKey = RandomNumberGenerator.GetInt32(
                2,
                checked((int)(p - 1)));
        }

        /*
         * Открытый ключ:
         *
         * y = g^x mod p
         */
        _publicKey = FastModularExponentiation(
            _g,
            _privateKey,
            _p);
    }

    /// <summary>
    /// Подписывает файл.
    ///
    /// Каждый байт SHA-256 подписывается отдельно.
    /// </summary>
    public ElGamalSignaturePair[] SignFile(
        string filePath)
    {
        if (!File.Exists(filePath))
            throw new FileNotFoundException(
                "Файл не найден.",
                filePath);

        byte[] hash =
            CalculateHash(filePath);

        return SignHash(hash);
    }

    /// <summary>
    /// Проверяет подпись файла.
    /// </summary>
    public bool VerifyFile(
        string filePath,
        ElGamalSignaturePair[] signature)
    {
        if (!File.Exists(filePath))
            throw new FileNotFoundException(
                "Файл не найден.",
                filePath);

        if (signature == null)
            throw new ArgumentNullException(
                nameof(signature));

        byte[] hash =
            CalculateHash(filePath);

        return VerifyHash(
            hash,
            signature);
    }

    /// <summary>
    /// Подписывает SHA-256.
    ///
    /// Для каждого байта h:
    ///
    /// r = g^k mod p
    ///
    /// s = (h - x*r) * k^(-1) mod (p - 1)
    ///
    /// где:
    /// x - закрытый ключ
    /// k - случайное число
    /// </summary>
    public ElGamalSignaturePair[] SignHash(
        byte[] hash)
    {
        if (hash == null)
            throw new ArgumentNullException(
                nameof(hash));

        ElGamalSignaturePair[] signature =
            new ElGamalSignaturePair[hash.Length];

        for (int i = 0; i < hash.Length; i++)
        {
            long h = hash[i];

            /*
             * k должно удовлетворять:
             *
             * 1 < k < p - 1
             * gcd(k, p - 1) = 1
             */
            long k = GenerateK();

            /*
             * r = g^k mod p
             */
            long r =
                FastModularExponentiation(
                    _g,
                    k,
                    _p);

            /*
             * k^(-1) mod (p - 1)
             */
            long kInverse =
                ModInverse(
                    k,
                    _p - 1);

            /*
             * s =
             * (h - x*r) * k^(-1) mod (p - 1)
             */
            long xr =
                MultiplyMod(
                    _privateKey,
                    r,
                    _p - 1);

            long value =
                (h - xr) % (_p - 1);

            if (value < 0)
                value += _p - 1;

            long s =
                MultiplyMod(
                    value,
                    kInverse,
                    _p - 1);

            signature[i] =
                new ElGamalSignaturePair(
                    r,
                    s);
        }

        return signature;
    }

    /// <summary>
    /// Проверяет подпись хеша.
    ///
    /// Для каждого байта:
    ///
    /// g^h mod p == y^r * r^s mod p
    /// </summary>
    public bool VerifyHash(
        byte[] hash,
        ElGamalSignaturePair[] signature)
    {
        if (hash == null)
            throw new ArgumentNullException(
                nameof(hash));

        if (signature == null)
            throw new ArgumentNullException(
                nameof(signature));

        if (hash.Length != signature.Length)
            return false;

        for (int i = 0; i < hash.Length; i++)
        {
            long r = signature[i].R;
            long s = signature[i].S;

            /*
             * Для корректной подписи:
             *
             * 0 < r < p
             * 0 <= s < p - 1
             */
            if (r <= 0 || r >= _p)
                return false;

            if (s < 0 || s >= _p - 1)
                return false;

            /*
             * Левая часть:
             *
             * g^h mod p
             */
            long left =
                FastModularExponentiation(
                    _g,
                    hash[i],
                    _p);

            /*
             * Правая часть:
             *
             * y^r * r^s mod p
             */
            long first =
                FastModularExponentiation(
                    _publicKey,
                    r,
                    _p);

            long second =
                FastModularExponentiation(
                    r,
                    s,
                    _p);

            long right =
                MultiplyMod(
                    first,
                    second,
                    _p);

            if (left != right)
                return false;
        }

        return true;
    }

    /// <summary>
    /// Вычисление SHA-256 файла.
    /// </summary>
    public byte[] CalculateHash(
        string filePath)
    {
        if (!File.Exists(filePath))
            throw new FileNotFoundException(
                "Файл не найден.",
                filePath);

        using FileStream stream =
            File.OpenRead(filePath);

        using SHA256 sha256 =
            SHA256.Create();

        return sha256.ComputeHash(stream);
    }

    /// <summary>
    /// Сохраняет подпись в читаемый .sig файл.
    /// </summary>
    public void SaveSignature(
        string signaturePath,
        ElGamalSignaturePair[] signature)
    {
        if (signature == null)
            throw new ArgumentNullException(
                nameof(signature));

        using StreamWriter writer =
            new StreamWriter(signaturePath);

        writer.WriteLine(
            "ELGAMAL DIGITAL SIGNATURE");

        writer.WriteLine(
            "========================");

        writer.WriteLine();

        writer.WriteLine(
            "Algorithm: ElGamal");

        writer.WriteLine(
            "Hash algorithm: SHA-256");

        writer.WriteLine();

        writer.WriteLine(
            $"p = {_p}");

        writer.WriteLine(
            $"g = {_g}");

        writer.WriteLine(
            $"PublicKey(y) = {_publicKey}");

        writer.WriteLine();

        writer.WriteLine(
            "Signature:");

        writer.WriteLine(
            "Index | R | S");

        writer.WriteLine(
            "----------------");

        for (int i = 0; i < signature.Length; i++)
        {
            writer.WriteLine(
                $"{i} | " +
                $"{signature[i].R} | " +
                $"{signature[i].S}");
        }
    }

    /// <summary>
    /// Загружает подпись из .sig файла.
    /// </summary>
    public ElGamalSignaturePair[] LoadSignature(
        string signaturePath)
    {
        if (!File.Exists(signaturePath))
            throw new FileNotFoundException(
                "Файл подписи не найден.",
                signaturePath);

        string[] lines =
            File.ReadAllLines(signaturePath);

        List<ElGamalSignaturePair> result =
            new List<ElGamalSignaturePair>();

        bool signatureStarted = false;

        foreach (string line in lines)
        {
            if (line == "Signature:")
            {
                signatureStarted = true;
                continue;
            }

            if (!signatureStarted)
                continue;

            if (line.StartsWith("Index"))
                continue;

            if (line.StartsWith("-"))
                continue;

            if (string.IsNullOrWhiteSpace(line))
                continue;

            string[] parts =
                line.Split('|');

            if (parts.Length != 3)
                continue;

            if (!long.TryParse(
                    parts[1].Trim(),
                    out long r))
            {
                throw new InvalidDataException(
                    $"Некорректное R: {parts[1]}");
            }

            if (!long.TryParse(
                    parts[2].Trim(),
                    out long s))
            {
                throw new InvalidDataException(
                    $"Некорректное S: {parts[2]}");
            }

            result.Add(
                new ElGamalSignaturePair(
                    r,
                    s));
        }

        if (result.Count == 0)
            throw new InvalidDataException(
                "В файле не найдена подпись.");

        return result.ToArray();
    }

    /// <summary>
    /// Генерация случайного k.
    /// </summary>
    private long GenerateK()
    {
        while (true)
        {
            long k =
                RandomNumberGenerator.GetInt32(
                    2,
                    checked((int)(_p - 1)));

            if (Gcd(k, _p - 1) == 1)
                return k;
        }
    }

    /// <summary>
    /// Быстрое возведение в степень по модулю.
    /// </summary>
    private long FastModularExponentiation(
        long value,
        long power,
        long modulus)
    {
        if (modulus <= 0)
            throw new ArgumentException(
                "Модуль должен быть положительным.");

        long result = 1 % modulus;

        value %= modulus;

        while (power > 0)
        {
            if ((power & 1) == 1)
            {
                result =
                    MultiplyMod(
                        result,
                        value,
                        modulus);
            }

            value =
                MultiplyMod(
                    value,
                    value,
                    modulus);

            power >>= 1;
        }

        return result;
    }

    /// <summary>
    /// Безопасное умножение по модулю.
    /// </summary>
    private long MultiplyMod(
        long a,
        long b,
        long modulus)
    {
        return (long)(
            (BigInteger)a *
            b %
            modulus);
    }

    /// <summary>
    /// Обратный элемент:
    ///
    /// a * x = 1 mod m
    /// </summary>
    private long ModInverse(
        long a,
        long m)
    {
        long oldR = a;
        long r = m;

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
            throw new InvalidOperationException(
                "Обратный элемент не существует.");

        long result =
            oldS % m;

        if (result < 0)
            result += m;

        return result;
    }

    /// <summary>
    /// НОД.
    /// </summary>
    public long Gcd(
        long a,
        long b)
    {
        while (b != 0)
        {
            long temp = a % b;

            a = b;
            b = temp;
        }

        return Math.Abs(a);
    }

    /// <summary>
    /// Проверка простоты.
    /// </summary>
    public bool IsPrime(
        long number)
    {
        if (number < 2)
            return false;

        if (number == 2)
            return true;

        if (number % 2 == 0)
            return false;

        for (long i = 3;
             i <= number / i;
             i += 2)
        {
            if (number % i == 0)
                return false;
        }

        return true;
    }

    /// <summary>
    /// Проверка, что g является примитивным корнем по модулю p.
    /// Для простого p:
    ///
    /// g^((p-1)/q) != 1 mod p
    ///
    /// для всех простых делителей q числа p-1.
    /// </summary>
    private bool IsPrimitiveRoot(
        long g,
        long p)
    {
        long phi = p - 1;

        List<long> factors =
            GetPrimeFactors(phi);

        foreach (long factor in factors)
        {
            long result =
                FastModularExponentiation(
                    g,
                    phi / factor,
                    p);

            if (result == 1)
                return false;
        }

        return true;
    }

    /// <summary>
    /// Получение простых множителей числа.
    /// </summary>
    private List<long> GetPrimeFactors(
        long number)
    {
        List<long> factors =
            new List<long>();

        long n = number;

        if (n % 2 == 0)
        {
            factors.Add(2);

            while (n % 2 == 0)
                n /= 2;
        }

        for (long i = 3;
             i <= n / i;
             i += 2)
        {
            if (n % i == 0)
            {
                factors.Add(i);

                while (n % i == 0)
                    n /= i;
            }
        }

        if (n > 1)
            factors.Add(n);

        return factors;
    }
}