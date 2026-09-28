using System.Security.Cryptography;

namespace Lab1;

public class RSASignature
{
    private readonly long _p;
    private readonly long _q;
    private const long MinP = 32500;
    private const long MaxP = 45000;

    private const long MinQ = 32500;
    private const long MaxQ = 45000;

    // N = p * q
    private readonly long _n;

    // Euler function: φ(N) = (p - 1)(q - 1)
    private readonly long _phi;

    // Открытый ключ
    // В условии методички это d.
    private readonly long _publicKey;

    // Закрытый ключ
    // В условии методички это e.
    private readonly long _privateKey;

    public long P => _p;
    public long Q => _q;
    public long N => _n;
    public long Phi => _phi;
    public long PublicKey => _publicKey;
    public long PrivateKey => _privateKey;

    /// <summary>
    /// Создание RSA-подписи с заданными простыми p и q.
    /// </summary>
    public RSASignature(long p, long q)
    {
        if (p < 3 || q < 3)
            throw new ArgumentException("p и q должны быть больше 2.");
        
        if (p < MinP || p > MaxP)
            throw new ArgumentException(
                $"p должно находиться в диапазоне [{MinP}; {MaxP}].");

        if (q < MinQ || q > MaxQ)
            throw new ArgumentException(
                $"q должно находиться в диапазоне [{MinQ}; {MaxQ}].");

        if (!IsPrime(p))
            throw new ArgumentException($"Число p={p} не является простым.");

        if (!IsPrime(q))
            throw new ArgumentException($"Число q={q} не является простым.");

        if (p == q)
            throw new ArgumentException("p и q должны быть различными.");

        _p = p;
        _q = q;

        checked
        {
            _n = p * q;
            _phi = (p - 1) * (q - 1);
        }

        /*
         * Выбираем открытый ключ d.
         *
         * Нужно:
         * 1. 1 < d < φ(N)
         * 2. gcd(d, φ(N)) = 1
         */
        _publicKey = FindPublicKey(_phi);

        /*
         * Находим закрытый ключ c:
         *
         * c * d ≡ 1 (mod φ(N))
         */
        _privateKey = ModInverse(_publicKey, _phi);
    }

    /// <summary>
    /// Подписывает файл.
    ///
    /// Возвращает массив из 32 подписей:
    /// одна RSA-подпись на каждый байт SHA-256.
    /// </summary>
    public long[] SignFile(string filePath)
    {
        if (!File.Exists(filePath))
            throw new FileNotFoundException("Файл не найден.", filePath);

        byte[] hash = CalculateHash(filePath);

        return SignHash(hash);
    }

    /// <summary>
    /// Проверяет подпись файла.
    /// </summary>
    public bool VerifyFile(string filePath, long[] signature)
    {
        if (!File.Exists(filePath))
            throw new FileNotFoundException("Файл не найден.", filePath);

        if (signature == null)
            throw new ArgumentNullException(nameof(signature));

        byte[] hash = CalculateHash(filePath);

        return VerifyHash(hash, signature);
    }

    /// <summary>
    /// Сохраняет подпись в отдельный текстовый файл.
    /// </summary>
    public void SaveSignature(string signaturePath, long[] signature)
    {
        if (signature == null)
            throw new ArgumentNullException(nameof(signature));

        string data = string.Join(" ", signature);

        File.WriteAllText(signaturePath, data);
    }

    /// <summary>
    /// Загружает подпись из файла.
    /// </summary>
    public long[] LoadSignature(string signaturePath)
    {
        if (!File.Exists(signaturePath))
            throw new FileNotFoundException(
                "Файл подписи не найден.",
                signaturePath);

        string data = File.ReadAllText(signaturePath);

        if (string.IsNullOrWhiteSpace(data))
            throw new InvalidDataException("Файл подписи пуст.");

        string[] values = data
            .Split(
                new[] { ' ', '\r', '\n', '\t' },
                StringSplitOptions.RemoveEmptyEntries);

        long[] signature = new long[values.Length];

        for (int i = 0; i < values.Length; i++)
        {
            if (!long.TryParse(values[i], out signature[i]))
                throw new InvalidDataException(
                    $"Некорректное значение подписи: {values[i]}");
        }

        return signature;
    }

    /// <summary>
    /// Подписывает массив байт хеша.
    /// </summary>
    public long[] SignHash(byte[] hash)
    {
        if (hash == null)
            throw new ArgumentNullException(nameof(hash));

        long[] signature = new long[hash.Length];

        for (int i = 0; i < hash.Length; i++)
        {
            /*
             * s = m^c mod N
             *
             * Здесь используется закрытый ключ.
             */
            signature[i] = FastModularExponentiation(
                hash[i],
                _privateKey,
                _n);
        }

        return signature;
    }

    /// <summary>
    /// Проверяет массив подписанного хеша.
    /// </summary>
    public bool VerifyHash(byte[] hash, long[] signature)
    {
        if (hash == null)
            throw new ArgumentNullException(nameof(hash));

        if (signature == null)
            throw new ArgumentNullException(nameof(signature));

        if (hash.Length != signature.Length)
            return false;

        for (int i = 0; i < hash.Length; i++)
        {
            /*
             * m = s^d mod N
             *
             * Используем открытый ключ.
             */
            long decoded = FastModularExponentiation(
                signature[i],
                _publicKey,
                _n);

            if (decoded != hash[i])
                return false;
        }

        return true;
    }
    
    /// <summary>
    /// Шифрует файл побайтово с использованием закрытого ключа.
    /// Возвращает массив зашифрованных байтов (каждый байт -> long).
    /// </summary>
    public long[] EncryptFile(string filePath)
    {
        if (!File.Exists(filePath))
            throw new FileNotFoundException("Файл не найден.", filePath);

        byte[] data = File.ReadAllBytes(filePath);

        long[] encrypted = new long[data.Length];

        for (int i = 0; i < data.Length; i++)
        {
            // Шифрование: s = m^privateKey mod N
            encrypted[i] = FastModularExponentiation(
                data[i],
                _privateKey,
                _n);
        }

        return encrypted;
    }
    
    /// <summary>
    /// Шифрует файл и сохраняет зашифрованные данные в отдельный файл.
    /// </summary>
    public void SaveEncryptedFile(string filePath, string encryptedFilePath)
    {
        long[] encrypted = EncryptFile(filePath);
    
        string data = string.Join(" ", encrypted);
    
        File.WriteAllText(encryptedFilePath, data);
    }
    
    /// <summary>
    /// Расшифровывает файл, зашифрованный методом EncryptFile.
    /// </summary>
    public byte[] DecryptFile(string encryptedFilePath)
    {
        if (!File.Exists(encryptedFilePath))
            throw new FileNotFoundException(
                "Зашифрованный файл не найден.", 
                encryptedFilePath);

        string data = File.ReadAllText(encryptedFilePath);

        string[] values = data
            .Split(
                new[] { ' ', '\r', '\n', '\t' },
                StringSplitOptions.RemoveEmptyEntries);

        byte[] decrypted = new byte[values.Length];

        for (int i = 0; i < values.Length; i++)
        {
            if (!long.TryParse(values[i], out long encryptedValue))
                throw new InvalidDataException(
                    $"Некорректное значение: {values[i]}");

            // Расшифровка: m = s^publicKey mod N
            long decryptedValue = FastModularExponentiation(
                encryptedValue,
                _publicKey,
                _n);

            decrypted[i] = (byte)decryptedValue;
        }

        return decrypted;
    }

    /// <summary>
    /// Вычисление SHA-256 файла.
    /// SHA-256 сильнее MD5, поэтому соответствует требованиям лабораторной.
    /// </summary>
    public byte[] CalculateHash(string filePath)
    {
        if (!File.Exists(filePath))
            throw new FileNotFoundException("Файл не найден.", filePath);

        using FileStream stream = File.OpenRead(filePath);

        using SHA256 sha256 = SHA256.Create();

        return sha256.ComputeHash(stream);
    }

    /// <summary>
    /// Быстрое возведение в степень по модулю:
    ///
    /// result = base^power mod modulus
    ///
    /// Алгоритм бинарного возведения в степень.
    /// </summary>
    private long FastModularExponentiation(
        long value,
        long power,
        long modulus)
    {
        if (modulus <= 0)
            throw new ArgumentException(
                "Модуль должен быть положительным.");

        if (power < 0)
            throw new ArgumentException(
                "Степень не может быть отрицательной.");

        value %= modulus;

        long result = 1 % modulus;

        while (power > 0)
        {
            if ((power & 1) == 1)
            {
                result = MultiplyMod(result, value, modulus);
            }

            value = MultiplyMod(value, value, modulus);

            power >>= 1;
        }

        return result;
    }

    /// <summary>
    /// Умножение по модулю.
    ///
    /// Используем BigInteger только внутри операции,
    /// чтобы гарантированно избежать переполнения long.
    ///
    /// Сам RSA, ключи и результат остаются long.
    /// </summary>
    private long MultiplyMod(
        long a,
        long b,
        long modulus)
    {
        return (long)(
            (System.Numerics.BigInteger)a *
            b %
            modulus);
    }

    /// <summary>
    /// Поиск открытого ключа e.
    /// </summary>
    private long FindPublicKey(long phi)
    {
        /*
         * Начинаем с распространённого значения 65537.
         * Если φ меньше, ищем подходящее число.
         */
        if (65537 < phi && Gcd(65537, phi) == 1)
            return 65537;

        for (long e = 3; e < phi; e += 2)
        {
            if (Gcd(e, phi) == 1)
                return e;
        }

        throw new InvalidOperationException(
            "Не удалось найти открытый ключ.");
    }

    /// <summary>
    /// Расширенный алгоритм Евклида.
    ///
    /// Находит обратный элемент:
    ///
    /// a * x ≡ 1 (mod m)
    /// </summary>
    private long ModInverse(long a, long m)
    {
        long oldR = a;
        long r = m;

        long oldS = 1;
        long s = 0;

        while (r != 0)
        {
            long quotient = oldR / r;

            long tempR = oldR - quotient * r;
            oldR = r;
            r = tempR;

            long tempS = oldS - quotient * s;
            oldS = s;
            s = tempS;
        }

        if (oldR != 1)
            throw new InvalidOperationException(
                "Обратный элемент не существует.");

        long result = oldS % m;

        if (result < 0)
            result += m;

        return result;
    }

    /// <summary>
    /// НОД двух чисел.
    /// </summary>
    private long Gcd(long a, long b)
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
    /// Проверка числа на простоту.
    /// </summary>
    public bool IsPrime(long number)
    {
        if (number < 2)
            return false;

        if (number == 2)
            return true;

        if (number % 2 == 0)
            return false;

        for (long i = 3; i <= number / i; i += 2)
        {
            if (number % i == 0)
                return false;
        }

        return true;
    }
}