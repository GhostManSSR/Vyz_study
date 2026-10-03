namespace Lab1;

using System.Numerics;
using System.Security.Cryptography;
using System.Text.Json;

public class Fips186
{
    // Учебные параметры.
//
// q = 1009 — простое число.
// p = 10091 — простое число.
//
// p - 1 = 10090 = 10 * q.
//
// Поэтому q является делителем p - 1.
    private readonly BigInteger p = 10091;
    private readonly BigInteger q = 1009;
    private readonly BigInteger g;

    // Закрытый ключ x.
    public BigInteger PrivateKey { get; private set; }

    // Открытый ключ y = g^x mod p.
    public BigInteger PublicKey { get; private set; }

    public Fips186()
    {
        // Ищем g:
        //
        // g = h^((p - 1) / q) mod p
        //
        // причем g > 1.
        for (BigInteger h = 2; h < p; h++)
        {
            BigInteger candidate =
                BigInteger.ModPow(
                    h,
                    (p - 1) / q,
                    p);

            if (candidate > 1 &&
                BigInteger.ModPow(candidate, q, p) == 1)
            {
                g = candidate;
                break;
            }
        }

        if (g == 0)
        {
            throw new Exception(
                "Не удалось найти параметр g.");
        }
    }
    
    // ============================================================
    // СОЗДАНИЕ ПОДПИСАННОГО ФАЙЛА
    // ============================================================

    public void SignFileToFiles(
        string filePath,
        string signedFilePath,
        string signatureFilePath)
    {
        if (!File.Exists(filePath))
        {
            throw new FileNotFoundException(
                "Исходный файл не найден.",
                filePath);
        }

        // Получаем подпись исходного файла.
        DigitalSignature signature =
            SignFile(filePath);

        // Сохраняем отдельную подпись.
        SaveSignature(
            signatureFilePath,
            signature);

        // Читаем исходный файл как массив байтов.
        byte[] fileBytes =
            File.ReadAllBytes(filePath);

        // Сериализуем подпись.
        string signatureJson =
            JsonSerializer.Serialize(
                signature,
                new JsonSerializerOptions
                {
                    WriteIndented = false
                });

        byte[] signatureBytes =
            System.Text.Encoding.UTF8.GetBytes(
                signatureJson);

        /*
         * Формат .signed:
         *
         * [8 байт]   размер исходного файла
         * [N байт]   исходный файл
         * [8 байт]   размер подписи
         * [M байт]   JSON подписи
         */

        using FileStream stream =
            new(
                signedFilePath,
                FileMode.Create,
                FileAccess.Write);

        using BinaryWriter writer =
            new(stream);

        // Размер исходного файла.
        writer.Write(
            (long)fileBytes.Length);

        // Сам исходный файл.
        writer.Write(
            fileBytes);

        // Размер подписи.
        writer.Write(
            (long)signatureBytes.Length);

        // Подпись.
        writer.Write(
            signatureBytes);
    }


    // ============================================================
    // ПРОВЕРКА ПОДПИСАННОГО ФАЙЛА
    // ============================================================

    public bool VerifySignedFile(
        string signedFilePath)
    {
        if (!File.Exists(signedFilePath))
        {
            return false;
        }

        string temporaryFile =
            Path.GetTempFileName();

        try
        {
            using FileStream stream =
                new(
                    signedFilePath,
                    FileMode.Open,
                    FileAccess.Read);

            using BinaryReader reader =
                new(stream);

            // Читаем размер исходного файла.
            long fileSize =
                reader.ReadInt64();

            if (fileSize < 0 ||
                fileSize > stream.Length)
            {
                return false;
            }

            // Читаем исходный файл.
            byte[] fileBytes =
                reader.ReadBytes(
                    checked((int)fileSize));

            if (fileBytes.Length != fileSize)
            {
                return false;
            }

            // Читаем размер подписи.
            long signatureSize =
                reader.ReadInt64();

            if (signatureSize < 0 ||
                signatureSize > stream.Length - stream.Position)
            {
                return false;
            }

            // Читаем подпись.
            byte[] signatureBytes =
                reader.ReadBytes(
                    checked((int)signatureSize));

            if (signatureBytes.Length != signatureSize)
            {
                return false;
            }

            string signatureJson =
                System.Text.Encoding.UTF8.GetString(
                    signatureBytes);

            DigitalSignature? signature =
                JsonSerializer.Deserialize<DigitalSignature>(
                    signatureJson);

            if (signature == null)
            {
                return false;
            }

            /*
             * Создаём временный файл с исходными
             * данными и проверяем его.
             */
            File.WriteAllBytes(
                temporaryFile,
                fileBytes);

            return VerifyFile(
                temporaryFile,
                signature);
        }
        catch
        {
            return false;
        }
        finally
        {
            if (File.Exists(temporaryFile))
            {
                File.Delete(temporaryFile);
            }
        }
    }

    // ============================================================
    // ГЕНЕРАЦИЯ КЛЮЧЕЙ
    // ============================================================

    public void GenerateKeys()
    {
        PrivateKey = RandomBigInteger(q);

        PublicKey =
            BigInteger.ModPow(
                g,
                PrivateKey,
                p);
    }

    // ============================================================
    // SHA-256
    // ============================================================

    public byte[] CalculateHash(byte[] data)
    {
        return SHA256.HashData(data);
    }

    public byte[] CalculateFileHash(string filePath)
    {
        using FileStream stream =
            File.OpenRead(filePath);

        return SHA256.HashData(stream);
    }

    // ============================================================
    // ПОДПИСЬ
    // ============================================================

    /*
     * По условию лабораторной каждый байт хэша
     * подписывается отдельно.
     *
     * SHA-256 = 32 байта.
     *
     * Поэтому результат содержит 32 пары:
     *
     * (r0, s0)
     * (r1, s1)
     * ...
     * (r31, s31)
     */

    public DigitalSignature Sign(
        byte[] hash)
    {
        if (PrivateKey <= 0 ||
            PrivateKey >= q)
        {
            throw new InvalidOperationException(
                "Сначала необходимо сгенерировать ключи.");
        }

        List<SignaturePart> parts = [];

        foreach (byte hashByte in hash)
        {
            parts.Add(
                SignByte(
                    hashByte));
        }

        return new DigitalSignature
        {
            Parts = parts
        };
    }

    private SignaturePart SignByte(
        byte hashByte)
    {
        while (true)
        {
            // 0 < k < q
            BigInteger k =
                RandomBigInteger(q);

            // r = (g^k mod p) mod q
            BigInteger r =
                BigInteger.ModPow(
                    g,
                    k,
                    p) % q;

            if (r == 0)
            {
                continue;
            }

            // k^(-1) mod q
            BigInteger kInverse =
                ModInverse(
                    k,
                    q);

            /*
             * s =
             * k^(-1) * (H(m) + x*r) mod q
             */
            BigInteger s =
                (
                    kInverse *
                    (
                        hashByte +
                        PrivateKey * r
                    )
                ) % q;

            if (s == 0)
            {
                continue;
            }

            return new SignaturePart
            {
                R = r,
                S = s
            };
        }
    }

    // ============================================================
    // ПРОВЕРКА ПОДПИСИ
    // ============================================================

    public bool Verify(
        byte[] hash,
        DigitalSignature signature)
    {
        if (PublicKey <= 0 ||
            PublicKey >= p)
        {
            throw new InvalidOperationException(
                "Открытый ключ не установлен.");
        }

        if (hash.Length != signature.Parts.Count)
        {
            return false;
        }

        for (int i = 0; i < hash.Length; i++)
        {
            if (!VerifyByte(
                    hash[i],
                    signature.Parts[i]))
            {
                return false;
            }
        }

        return true;
    }

    private bool VerifyByte(
        byte hashByte,
        SignaturePart signature)
    {
        BigInteger r = signature.R;
        BigInteger s = signature.S;

        // 0 < r < q
        if (r <= 0 || r >= q)
        {
            return false;
        }

        // 0 < s < q
        if (s <= 0 || s >= q)
        {
            return false;
        }

        /*
         * w = s^(-1) mod q
         */
        BigInteger w =
            ModInverse(
                s,
                q);

        /*
         * u1 = H(m) * w mod q
         */
        BigInteger u1 =
            (hashByte * w) % q;

        /*
         * u2 = r * w mod q
         */
        BigInteger u2 =
            (r * w) % q;

        /*
         * v =
         * ((g^u1 * y^u2) mod p) mod q
         */
        BigInteger gu1 =
            BigInteger.ModPow(
                g,
                u1,
                p);

        BigInteger yu2 =
            BigInteger.ModPow(
                PublicKey,
                u2,
                p);

        BigInteger v =
            ((gu1 * yu2) % p) % q;

        return v == r;
    }

    // ============================================================
    // ПОДПИСЬ ФАЙЛА
    // ============================================================

    public DigitalSignature SignFile(
        string filePath)
    {
        byte[] hash =
            CalculateFileHash(filePath);

        DigitalSignature signature =
            Sign(hash);

        signature.FileSize =
            new FileInfo(filePath).Length;

        return signature;
    }

    // ============================================================
    // ПРОВЕРКА ФАЙЛА
    // ============================================================

    public bool VerifyFile(
        string filePath,
        DigitalSignature signature)
    {
        if (!File.Exists(filePath))
        {
            return false;
        }

        FileInfo fileInfo =
            new(filePath);

        /*
         * Дополнительная проверка размера файла.
         */
        if (fileInfo.Length != signature.FileSize)
        {
            return false;
        }

        byte[] hash =
            CalculateFileHash(filePath);

        return Verify(
            hash,
            signature);
    }

    // ============================================================
    // СОХРАНЕНИЕ ПОДПИСИ
    // ============================================================

    public void SaveSignature(
        string path,
        DigitalSignature signature)
    {
        string json =
            JsonSerializer.Serialize(
                signature,
                new JsonSerializerOptions
                {
                    WriteIndented = true
                });

        File.WriteAllText(
            path,
            json);
    }

    // ============================================================
    // ЗАГРУЗКА ПОДПИСИ
    // ============================================================

    public DigitalSignature LoadSignature(
        string path)
    {
        string json =
            File.ReadAllText(path);

        DigitalSignature? signature =
            JsonSerializer.Deserialize<DigitalSignature>(
                json);

        if (signature == null)
        {
            throw new InvalidDataException(
                "Некорректный файл подписи.");
        }

        return signature;
    }

    // ============================================================
    // СОХРАНЕНИЕ КЛЮЧЕЙ
    // ============================================================

    public void SaveKeys(
        string path)
    {
        KeyData data = new()
        {
            PrivateKey = PrivateKey.ToString(),
            PublicKey = PublicKey.ToString()
        };

        string json =
            JsonSerializer.Serialize(
                data,
                new JsonSerializerOptions
                {
                    WriteIndented = true
                });

        File.WriteAllText(
            path,
            json);
    }
    
    // ============================================================
// ИЗВЛЕЧЕНИЕ ОРИГИНАЛЬНОГО ФАЙЛА
// ============================================================

    public void ExtractSignedFile(
        string signedFilePath,
        string outputFilePath)
    {
        if (!File.Exists(signedFilePath))
        {
            throw new FileNotFoundException(
                "Подписанный файл не найден.",
                signedFilePath);
        }

        using FileStream stream =
            new(
                signedFilePath,
                FileMode.Open,
                FileAccess.Read);

        using BinaryReader reader =
            new(stream);

        // Размер исходного файла.
        long fileSize =
            reader.ReadInt64();

        if (fileSize < 0 ||
            fileSize > stream.Length)
        {
            throw new InvalidDataException(
                "Некорректный подписанный файл.");
        }

        // Исходный файл.
        byte[] fileBytes =
            reader.ReadBytes(
                checked((int)fileSize));

        if (fileBytes.Length != fileSize)
        {
            throw new InvalidDataException(
                "Файл повреждён.");
        }

        // Размер подписи.
        long signatureSize =
            reader.ReadInt64();

        if (signatureSize < 0 ||
            signatureSize > stream.Length - stream.Position)
        {
            throw new InvalidDataException(
                "Некорректный размер подписи.");
        }

        // Пропускаем подпись.
        reader.ReadBytes(
            checked((int)signatureSize));

        File.WriteAllBytes(
            outputFilePath,
            fileBytes);
    }

    // ============================================================
    // ЗАГРУЗКА КЛЮЧЕЙ
    // ============================================================

    public void LoadKeys(
        string path)
    {
        string json =
            File.ReadAllText(path);

        KeyData? data =
            JsonSerializer.Deserialize<KeyData>(
                json);

        if (data == null)
        {
            throw new InvalidDataException(
                "Некорректный файл ключей.");
        }

        PrivateKey =
            BigInteger.Parse(
                data.PrivateKey);

        PublicKey =
            BigInteger.Parse(
                data.PublicKey);
    }

    // ============================================================
    // ГЕНЕРАЦИЯ СЛУЧАЙНОГО ЧИСЛА
    // ============================================================

    private static BigInteger RandomBigInteger(
        BigInteger max)
    {
        byte[] bytes =
            max.ToByteArray(
                isUnsigned: true,
                isBigEndian: true);

        while (true)
        {
            RandomNumberGenerator.Fill(
                bytes);

            BigInteger value =
                new(
                    bytes,
                    isUnsigned: true,
                    isBigEndian: true);

            if (value > 0 &&
                value < max)
            {
                return value;
            }
        }
    }

    // ============================================================
    // ОБРАТНЫЙ ЭЛЕМЕНТ ПО MOD
    // ============================================================

    private static BigInteger ModInverse(
        BigInteger value,
        BigInteger modulus)
    {
        value %= modulus;

        BigInteger oldR = value;
        BigInteger r = modulus;

        BigInteger oldS = 1;
        BigInteger s = 0;

        while (r != 0)
        {
            BigInteger quotient =
                oldR / r;

            (oldR, r) =
                (
                    r,
                    oldR - quotient * r
                );

            (oldS, s) =
                (
                    s,
                    oldS - quotient * s
                );
        }

        if (oldR != 1)
        {
            throw new ArithmeticException(
                "Обратного элемента не существует.");
        }

        BigInteger result =
            oldS % modulus;

        if (result < 0)
        {
            result += modulus;
        }

        return result;
    }

    // ============================================================
    // ВЛОЖЕННЫЕ КЛАССЫ ДАННЫХ
    // ============================================================

    public class SignaturePart
    {
        public BigInteger R { get; set; }

        public BigInteger S { get; set; }
    }

    public class DigitalSignature
    {
        public List<SignaturePart> Parts { get; set; } = [];

        public long FileSize { get; set; }
    }

    private class KeyData
    {
        public string PrivateKey { get; set; } = "";

        public string PublicKey { get; set; } = "";
    }
}