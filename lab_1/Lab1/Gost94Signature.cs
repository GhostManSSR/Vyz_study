using System.Numerics;
using System.Security.Cryptography;

namespace Lab10;

public sealed class Gost94Signature
{
    // ============================================================
    // ОБЩИЕ ПАРАМЕТРЫ ГОСТ
    // ============================================================

    // p — простое число длиной 31 бит
    // q — простое число длиной 16 бит
    // p - 1 = k * q
    // a^q mod p = 1

    public BigInteger P { get; }
    public BigInteger Q { get; }
    public BigInteger A { get; }

    // ============================================================
    // КЛЮЧИ ПОЛЬЗОВАТЕЛЯ
    // ============================================================

    // x — секретный ключ
    public BigInteger PrivateKey { get; }

    // y = a^x mod p
    public BigInteger PublicKey { get; }

    private static readonly RandomNumberGenerator Random =
        RandomNumberGenerator.Create();

    // ============================================================
    // КОНСТРУКТОР
    // ============================================================

    public Gost94Signature(
        BigInteger p,
        BigInteger q,
        BigInteger a,
        BigInteger privateKey)
    {
        P = p;
        Q = q;
        A = a;

        PrivateKey = privateKey;

        // y = a^x mod p
        PublicKey =
            BigInteger.ModPow(
                A,
                PrivateKey,
                P);

        ValidateParameters();
    }

    // ============================================================
    // ГЕНЕРАЦИЯ ПАРАМЕТРОВ
    // ============================================================

    public static Gost94Signature Generate()
    {
        // По методичке:
        //
        // p = 31 бит
        // q = 16 бит
        //
        // Старший бит должен быть равен 1.

        const int pBits = 31;
        const int qBits = 16;

        while (true)
        {
            // ----------------------------------------------------
            // 1. Генерируем простое q длиной 16 бит
            // ----------------------------------------------------

            BigInteger q =
                GeneratePrime(qBits);

            // ----------------------------------------------------
            // 2. Ищем p = k*q + 1
            //
            // p должен быть простым и иметь ровно 31 бит.
            // ----------------------------------------------------

            BigInteger minP =
                BigInteger.One << (pBits - 1);

            BigInteger maxP =
                (BigInteger.One << pBits) - 1;

            BigInteger minK =
                (minP - 1 + q - 1) / q;

            BigInteger maxK =
                (maxP - 1) / q;

            if (minK > maxK)
                continue;

            BigInteger k =
                RandomBigInteger(
                    minK,
                    maxK);

            BigInteger p =
                k * q + 1;

            if (p < minP || p > maxP)
                continue;

            // p должно быть простым.
            if (!IsProbablePrime(p))
                continue;

            // ----------------------------------------------------
            // 3. Ищем a
            //
            // a = g^k mod p
            //
            // Тогда:
            //
            // a^q mod p
            // =
            // (g^k)^q mod p
            // =
            // g^(kq) mod p
            // =
            // g^(p-1) mod p
            // =
            // 1
            //
            // по малой теореме Ферма.
            // ----------------------------------------------------

            for (int i = 0; i < 100; i++)
            {
                BigInteger g =
                    RandomBigInteger(
                        2,
                        p - 2);

                BigInteger a =
                    BigInteger.ModPow(
                        g,
                        k,
                        p);

                if (a <= 1)
                    continue;

                if (a >= p)
                    continue;

                if (BigInteger.ModPow(a, q, p) != 1)
                    continue;

                // ------------------------------------------------
                // 4. Генерируем секретный ключ x
                //
                // 1 < x < q
                // ------------------------------------------------

                BigInteger x =
                    RandomBigInteger(
                        1,
                        q - 1);

                return new Gost94Signature(
                    p,
                    q,
                    a,
                    x);
            }
        }
    }

    // ============================================================
    // ПОДПИСЬ ФАЙЛА
    // ============================================================

    public SignatureData SignFile(
        string filePath)
    {
        if (!File.Exists(filePath))
        {
            throw new FileNotFoundException(
                "Файл не найден.",
                filePath);
        }

        // Вычисляем SHA-256.
        //
        // Получаем 32 байта.
        byte[] hash =
            ComputeHash(filePath);

        var signatures =
            new List<ByteSignature>(
                hash.Length);

        // --------------------------------------------------------
        // Согласно условию лабораторной:
        //
        // хеш представляется массивом байтов,
        // каждый байт подписывается отдельно.
        // --------------------------------------------------------

        foreach (byte hashByte in hash)
        {
            ByteSignature signature =
                SignByte(hashByte);

            signatures.Add(signature);
        }

        return new SignatureData(
            P,
            Q,
            A,
            PublicKey,
            hash,
            signatures);
    }

    // ============================================================
    // ПОДПИСЬ ОДНОГО БАЙТА ХЕША
    // ============================================================

    private ByteSignature SignByte(
        byte hashByte)
    {
        // В ГОСТ:
        //
        // 0 < h < q
        //
        // Поскольку q имеет 16 бит,
        // любой байт 1..255 автоматически меньше q.
        //
        // Особый случай:
        // если h = 0, используем h = 1.

        BigInteger h = hashByte;

        if (h == 0)
        {
            h = 1;
        }

        while (true)
        {
            // ----------------------------------------------------
            // 1. Случайное k
            //
            // 0 < k < q
            // ----------------------------------------------------

            BigInteger k =
                RandomBigInteger(
                    1,
                    Q - 1);

            // ----------------------------------------------------
            // 2. r = (a^k mod p) mod q
            // ----------------------------------------------------

            BigInteger r =
                BigInteger.ModPow(
                    A,
                    k,
                    P) % Q;

            // По алгоритму ГОСТ:
            // если r == 0, выбираем новое k.
            if (r == 0)
                continue;

            // ----------------------------------------------------
            // 3. s = (x*r + k*h) mod q
            // ----------------------------------------------------

            BigInteger s =
                (
                    PrivateKey * r +
                    k * h
                ) % Q;

            // По алгоритму ГОСТ:
            // если s == 0, выбираем новое k.
            if (s == 0)
                continue;

            return new ByteSignature(
                r,
                s);
        }
    }

    // ============================================================
    // ПРОВЕРКА ФАЙЛА
    // ============================================================

    public bool VerifyFile(
        string filePath,
        SignatureData signature)
    {
        if (!File.Exists(filePath))
            return false;

        // Проверяем параметры.
        if (signature.P != P)
            return false;

        if (signature.Q != Q)
            return false;

        if (signature.A != A)
            return false;

        if (signature.PublicKey != PublicKey)
            return false;

        // --------------------------------------------------------
        // Вычисляем хеш заново.
        // --------------------------------------------------------

        byte[] hash =
            ComputeHash(filePath);

        // SHA-256 = 32 байта.
        if (signature.Signatures.Count != hash.Length)
            return false;

        // --------------------------------------------------------
        // Проверяем каждый байт.
        // --------------------------------------------------------

        for (int i = 0; i < hash.Length; i++)
        {
            if (!VerifyByte(
                    hash[i],
                    signature.Signatures[i]))
            {
                return false;
            }
        }

        return true;
    }

    // ============================================================
    // ПРОВЕРКА ОДНОГО БАЙТА
    // ============================================================

    private bool VerifyByte(
        byte hashByte,
        ByteSignature signature)
    {
        BigInteger r =
            signature.R;

        BigInteger s =
            signature.S;

        // --------------------------------------------------------
        // 1. Проверяем:
        //
        // 0 < r < q
        // 0 < s < q
        // --------------------------------------------------------

        if (r <= 0 || r >= Q)
            return false;

        if (s <= 0 || s >= Q)
            return false;

        // --------------------------------------------------------
        // 2. Получаем h
        // --------------------------------------------------------

        BigInteger h =
            hashByte;

        if (h == 0)
        {
            h = 1;
        }

        // --------------------------------------------------------
        // 3. v = h^(-1) mod q
        // --------------------------------------------------------

        BigInteger v =
            ModInverse(
                h,
                Q);

        // --------------------------------------------------------
        // 4. z1 = s*v mod q
        // --------------------------------------------------------

        BigInteger z1 =
            (s * v) % Q;

        // --------------------------------------------------------
        // 5. z2 = -r*v mod q
        //
        // Чтобы не получить отрицательное число:
        //
        // z2 = (q - r*v) mod q
        // --------------------------------------------------------

        BigInteger z2 =
            ((Q - r) * v) % Q;

        // --------------------------------------------------------
        // 6. u =
        //
        // ((a^z1 * y^z2) mod p) mod q
        // --------------------------------------------------------

        BigInteger part1 =
            BigInteger.ModPow(
                A,
                z1,
                P);

        BigInteger part2 =
            BigInteger.ModPow(
                PublicKey,
                z2,
                P);

        BigInteger u =
            (part1 * part2 % P) % Q;

        // --------------------------------------------------------
        // 7. Проверяем:
        //
        // u == r
        // --------------------------------------------------------

        return u == r;
    }

    // ============================================================
    // SHA-256
    // ============================================================

    public static byte[] ComputeHash(
        string filePath)
    {
        using FileStream stream =
            File.OpenRead(filePath);

        using SHA256 sha256 =
            SHA256.Create();

        return sha256.ComputeHash(stream);
    }

    // ============================================================
    // ОБРАТНЫЙ ЭЛЕМЕНТ ПО МОДУЛЮ
    // ============================================================

    private static BigInteger ModInverse(
        BigInteger value,
        BigInteger modulus)
    {
        value %= modulus;

        if (value < 0)
            value += modulus;

        BigInteger oldR = value;
        BigInteger r = modulus;

        BigInteger oldS = 1;
        BigInteger s = 0;

        while (r != 0)
        {
            BigInteger quotient =
                oldR / r;

            BigInteger tempR =
                oldR - quotient * r;

            oldR = r;
            r = tempR;

            BigInteger tempS =
                oldS - quotient * s;

            oldS = s;
            s = tempS;
        }

        if (oldR != 1)
        {
            throw new ArithmeticException(
                "Обратный элемент не существует.");
        }

        return (oldS % modulus + modulus) % modulus;
    }

    // ============================================================
    // ГЕНЕРАЦИЯ СЛУЧАЙНОГО ЧИСЛА
    // ============================================================

    private static BigInteger RandomBigInteger(
        BigInteger min,
        BigInteger max)
    {
        if (min > max)
            throw new ArgumentException();

        BigInteger range =
            max - min + 1;

        byte[] bytes =
            range.ToByteArray(
                isUnsigned: true,
                isBigEndian: false);

        BigInteger result;

        do
        {
            Random.GetBytes(bytes);

            result =
                new BigInteger(
                    bytes,
                    isUnsigned: true,
                    isBigEndian: false);
        }
        while (result >= range);

        return min + result;
    }

    // ============================================================
    // ГЕНЕРАЦИЯ ПРОСТОГО ЧИСЛА
    // ============================================================

    private static BigInteger GeneratePrime(
        int bits)
    {
        while (true)
        {
            int byteCount =
                (bits + 7) / 8;

            byte[] bytes =
                new byte[byteCount];

            Random.GetBytes(bytes);

            // Работаем как с unsigned.
            BigInteger candidate =
                new BigInteger(
                    bytes,
                    isUnsigned: true,
                    isBigEndian: false);

            // Оставляем ровно bits бит.
            BigInteger mask =
                (BigInteger.One << bits) - 1;

            candidate &= mask;

            // Старший бит = 1.
            candidate |=
                BigInteger.One << (bits - 1);

            // Число должно быть нечётным.
            candidate |= 1;

            if (BitLength(candidate) != bits)
                continue;

            if (IsProbablePrime(candidate))
                return candidate;
        }
    }

    // ============================================================
    // ТЕСТ ПРОСТОТЫ MILLER-RABIN
    // ============================================================

    private static bool IsProbablePrime(
        BigInteger n,
        int rounds = 20)
    {
        if (n < 2)
            return false;

        int[] smallPrimes =
        {
            2, 3, 5, 7, 11, 13,
            17, 19, 23, 29, 31, 37
        };

        foreach (int prime in smallPrimes)
        {
            if (n == prime)
                return true;

            if (n % prime == 0)
                return false;
        }

        BigInteger d =
            n - 1;

        int s = 0;

        while (d.IsEven)
        {
            d /= 2;
            s++;
        }

        for (int i = 0; i < rounds; i++)
        {
            BigInteger a =
                RandomBigInteger(
                    2,
                    n - 2);

            BigInteger x =
                BigInteger.ModPow(
                    a,
                    d,
                    n);

            if (x == 1 || x == n - 1)
                continue;

            bool passed =
                false;

            for (int j = 1; j < s; j++)
            {
                x =
                    BigInteger.ModPow(
                        x,
                        2,
                        n);

                if (x == n - 1)
                {
                    passed = true;
                    break;
                }
            }

            if (!passed)
                return false;
        }

        return true;
    }

    // ============================================================
    // ДЛИНА ЧИСЛА В БИТАХ
    // ============================================================

    private static int BitLength(
        BigInteger value)
    {
        if (value == 0)
            return 0;

        int result = 0;

        while (value > 0)
        {
            value >>= 1;
            result++;
        }

        return result;
    }

    // ============================================================
    // ПРОВЕРКА ПАРАМЕТРОВ
    // ============================================================

    private void ValidateParameters()
    {
        // p должен иметь 31 бит.
        if (BitLength(P) != 31)
        {
            throw new ArgumentException(
                "p должен иметь длину 31 бит.");
        }

        // q должен иметь 16 бит.
        if (BitLength(Q) != 16)
        {
            throw new ArgumentException(
                "q должен иметь длину 16 бит.");
        }

        // p и q должны быть простыми.
        if (!IsProbablePrime(P))
        {
            throw new ArgumentException(
                "p не является простым.");
        }

        if (!IsProbablePrime(Q))
        {
            throw new ArgumentException(
                "q не является простым.");
        }

        // q должно делить p - 1.
        if ((P - 1) % Q != 0)
        {
            throw new ArgumentException(
                "q не делит p - 1.");
        }

        // 1 < a < p.
        if (A <= 1 || A >= P)
        {
            throw new ArgumentException(
                "Некорректное значение a.");
        }

        // a^q mod p = 1.
        if (BigInteger.ModPow(A, Q, P) != 1)
        {
            throw new ArgumentException(
                "a^q mod p должно быть равно 1.");
        }

        // 0 < x < q.
        if (PrivateKey <= 0 ||
            PrivateKey >= Q)
        {
            throw new ArgumentException(
                "Некорректный секретный ключ x.");
        }
    }
    
    public void SaveSignature(
        SignatureData signature,
        string signatureFilePath)
    {
        using FileStream stream = new(
            signatureFilePath,
            FileMode.Create,
            FileAccess.Write);

        using BinaryWriter writer = new(stream);

        // Версия формата подписи
        writer.Write(1);

        // Общие параметры
        WriteBigInteger(writer, signature.P);
        WriteBigInteger(writer, signature.Q);
        WriteBigInteger(writer, signature.A);

        // Открытый ключ
        WriteBigInteger(writer, signature.PublicKey);

        // SHA-256 исходного файла
        writer.Write(signature.Hash.Length);
        writer.Write(signature.Hash);

        // Количество подписанных байтов хеша
        writer.Write(signature.Signatures.Count);

        // Пары r, s
        foreach (ByteSignature item in signature.Signatures)
        {
            WriteBigInteger(writer, item.R);
            WriteBigInteger(writer, item.S);
        }
    }
    
    public void SaveSignatureText(
    SignatureData signature,
    string filePath)
{
    using StreamWriter writer =
        new StreamWriter(
            filePath,
            false,
            System.Text.Encoding.UTF8);

    writer.WriteLine("============================================================");
    writer.WriteLine("             ГОСТ Р 34.10-94 ЭЛЕКТРОННАЯ ПОДПИСЬ");
    writer.WriteLine("============================================================");
    writer.WriteLine();

    writer.WriteLine("ОБЩИЕ ПАРАМЕТРЫ");
    writer.WriteLine("------------------------------------------------------------");

    writer.WriteLine($"p = {signature.P}");
    writer.WriteLine($"p (hex) = {signature.P.ToString("X")}");

    writer.WriteLine();

    writer.WriteLine($"q = {signature.Q}");
    writer.WriteLine($"q (hex) = {signature.Q.ToString("X")}");

    writer.WriteLine();

    writer.WriteLine($"a = {signature.A}");
    writer.WriteLine($"a (hex) = {signature.A.ToString("X")}");

    writer.WriteLine();

    writer.WriteLine("ОТКРЫТЫЙ КЛЮЧ");
    writer.WriteLine("------------------------------------------------------------");

    writer.WriteLine($"y = {signature.PublicKey}");
    writer.WriteLine($"y (hex) = {signature.PublicKey.ToString("X")}");

    writer.WriteLine();

    writer.WriteLine("SHA-256 ИСХОДНОГО ФАЙЛА");
    writer.WriteLine("------------------------------------------------------------");

    writer.WriteLine(
        $"Hash = {Convert.ToHexString(signature.Hash)}");

    writer.WriteLine();

    writer.WriteLine(
        $"Количество подписанных байтов = {signature.Hash.Length}");

    writer.WriteLine(
        $"Количество подписей = {signature.Signatures.Count}");

    writer.WriteLine();

    writer.WriteLine("ПОДПИСИ");
    writer.WriteLine("============================================================");
    writer.WriteLine();

    for (int i = 0; i < signature.Signatures.Count; i++)
    {
        byte hashByte = signature.Hash[i];

        // В нашей реализации h=0 заменяется на 1,
        // поскольку по алгоритму должно выполняться 0 < h < q.
        BigInteger h =
            hashByte == 0
                ? BigInteger.One
                : new BigInteger(hashByte);

        ByteSignature current =
            signature.Signatures[i];

        writer.WriteLine(
            $"БАЙТ #{i}");

        writer.WriteLine(
            "------------------------------------------------------------");

        writer.WriteLine(
            $"Исходный байт hash[{i}] = {hashByte}");

        writer.WriteLine(
            $"Исходный байт HEX = {hashByte:X2}");

        writer.WriteLine(
            $"h = {h}");

        writer.WriteLine();

        writer.WriteLine(
            $"r = {current.R}");

        writer.WriteLine(
            $"r (hex) = {current.R.ToString("X")}");

        writer.WriteLine();

        writer.WriteLine(
            $"s = {current.S}");

        writer.WriteLine(
            $"s (hex) = {current.S.ToString("X")}");

        writer.WriteLine();

        writer.WriteLine(
            "Формула:");

        writer.WriteLine(
            "r = (a^k mod p) mod q");

        writer.WriteLine(
            "s = (x * r + k * h) mod q");

        writer.WriteLine();

        writer.WriteLine(
            "------------------------------------------------------------");

        writer.WriteLine();
    }

    writer.WriteLine("============================================================");
    writer.WriteLine("Конец электронной подписи");
    writer.WriteLine("============================================================");
}
    
    public static SignatureData LoadSignature(
        string signatureFilePath)
    {
        if (!File.Exists(signatureFilePath))
        {
            throw new FileNotFoundException(
                "Файл подписи не найден.",
                signatureFilePath);
        }

        using FileStream stream = new(
            signatureFilePath,
            FileMode.Open,
            FileAccess.Read);

        using BinaryReader reader = new(stream);

        int version = reader.ReadInt32();

        if (version != 1)
        {
            throw new InvalidDataException(
                "Неизвестная версия файла подписи.");
        }

        BigInteger p =
            ReadBigInteger(reader);

        BigInteger q =
            ReadBigInteger(reader);

        BigInteger a =
            ReadBigInteger(reader);

        BigInteger publicKey =
            ReadBigInteger(reader);

        int hashLength =
            reader.ReadInt32();

        if (hashLength != 32)
        {
            throw new InvalidDataException(
                "Ожидался SHA-256 длиной 32 байта.");
        }

        byte[] hash =
            reader.ReadBytes(hashLength);

        if (hash.Length != hashLength)
        {
            throw new EndOfStreamException();
        }

        int signatureCount =
            reader.ReadInt32();

        if (signatureCount != hashLength)
        {
            throw new InvalidDataException(
                "Количество подписей не соответствует размеру хеша.");
        }

        var signatures =
            new List<ByteSignature>(signatureCount);

        for (int i = 0; i < signatureCount; i++)
        {
            BigInteger r =
                ReadBigInteger(reader);

            BigInteger s =
                ReadBigInteger(reader);

            signatures.Add(
                new ByteSignature(r, s));
        }

        return new SignatureData(
            p,
            q,
            a,
            publicKey,
            hash,
            signatures);
    }
    
    private static void WriteBigInteger(
        BinaryWriter writer,
        BigInteger value)
    {
        byte[] bytes =
            value.ToByteArray(
                isUnsigned: true,
                isBigEndian: false);

        writer.Write(bytes.Length);
        writer.Write(bytes);
    }

    private static BigInteger ReadBigInteger(
        BinaryReader reader)
    {
        int length =
            reader.ReadInt32();

        if (length <= 0 || length > 1024)
        {
            throw new InvalidDataException(
                "Некорректный размер BigInteger.");
        }

        byte[] bytes =
            reader.ReadBytes(length);

        if (bytes.Length != length)
        {
            throw new EndOfStreamException();
        }

        return new BigInteger(
            bytes,
            isUnsigned: true,
            isBigEndian: false);
    }
}

// ================================================================
// ПОДПИСЬ ОДНОГО БАЙТА
// ================================================================

public sealed record ByteSignature(
    BigInteger R,
    BigInteger S);

// ================================================================
// ПОДПИСЬ ФАЙЛА
// ================================================================

public sealed class SignatureData
{
    public BigInteger P { get; }

    public BigInteger Q { get; }

    public BigInteger A { get; }

    public BigInteger PublicKey { get; }

    public byte[] Hash { get; }

    public List<ByteSignature> Signatures { get; }

    public SignatureData(
        BigInteger p,
        BigInteger q,
        BigInteger a,
        BigInteger publicKey,
        byte[] hash,
        List<ByteSignature> signatures)
    {
        P = p;
        Q = q;
        A = a;
        PublicKey = publicKey;
        Hash = hash;
        Signatures = signatures;
    }
}