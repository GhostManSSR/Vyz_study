using System.Numerics;
using System.Security.Cryptography;

namespace Lab1;

public class Vernam
{
private readonly FastModularExponentiation _fastModularExponentiation;

    public Vernam(
        FastModularExponentiation fastModularExponentiation)
    {
        _fastModularExponentiation =
            fastModularExponentiation
            ?? throw new ArgumentNullException(
                nameof(fastModularExponentiation));
    }

    /// <summary>
    /// Шифрование алгоритмом Вернама.
    ///
    /// C = M XOR K
    /// </summary>
    public byte[] Encrypt(
        byte[] data,
        byte[] key)
    {
        ValidateInput(data, key);

        byte[] result = new byte[data.Length];

        for (int i = 0; i < data.Length; i++)
        {
            result[i] = (byte)(data[i] ^ key[i]);
        }

        return result;
    }

    /// <summary>
    /// Расшифрование алгоритмом Вернама.
    ///
    /// M = C XOR K
    ///
    /// XOR является взаимообратной операцией,
    /// поэтому Encrypt и Decrypt выполняют
    /// одну и ту же операцию.
    /// </summary>
    public byte[] Decrypt(
        byte[] encryptedData,
        byte[] key)
    {
        return Encrypt(encryptedData, key);
    }

    /// <summary>
    /// Генерация случайного ключа.
    ///
    /// Каждый байт принимает значение от 0 до 255.
    /// </summary>
    public byte[] GenerateKey(int length)
    {
        if (length < 0)
        {
            throw new ArgumentOutOfRangeException(
                nameof(length),
                "Длина ключа не может быть отрицательной.");
        }

        byte[] key = new byte[length];

        RandomNumberGenerator.Fill(key);

        return key;
    }

    /// <summary>
    /// Генерация ключа Вернама на основе
    /// общего секрета, полученного по Диффи-Хеллману.
    ///
    /// Один и тот же sharedSecret всегда приводит
    /// к одному и тому же ключу.
    ///
    /// Для расширения секрета используется:
    ///
    /// SHA256(sharedSecret || counter)
    ///
    /// Поэтому можно получить ключ любой длины.
    /// </summary>
    /// <param name="sharedSecret">
    /// Общий секрет Диффи-Хеллмана.
    /// </param>
    /// <param name="length">
    /// Размер ключа в байтах.
    /// </param>
    public byte[] GenerateKeyFromDiffieHellman(
        long sharedSecret,
        int length)
    {
        if (sharedSecret < 0)
        {
            throw new ArgumentOutOfRangeException(
                nameof(sharedSecret),
                "Общий секрет не может быть отрицательным.");
        }

        if (length < 0)
        {
            throw new ArgumentOutOfRangeException(
                nameof(length),
                "Длина ключа не может быть отрицательной.");
        }

        if (length == 0)
        {
            return Array.Empty<byte>();
        }

        /*
         * Преобразуем long в фиксированное
         * 8-байтовое представление.
         */
        byte[] secretBytes =
            BitConverter.GetBytes(sharedSecret);

        if (BitConverter.IsLittleEndian)
        {
            Array.Reverse(secretBytes);
        }

        byte[] key = new byte[length];

        int offset = 0;
        long counter = 0;

        while (offset < length)
        {
            byte[] counterBytes =
                BitConverter.GetBytes(counter);

            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(counterBytes);
            }

            /*
             * Формируем:
             *
             * sharedSecret || counter
             */
            byte[] input = new byte[
                secretBytes.Length +
                counterBytes.Length];

            Buffer.BlockCopy(
                secretBytes,
                0,
                input,
                0,
                secretBytes.Length);

            Buffer.BlockCopy(
                counterBytes,
                0,
                input,
                secretBytes.Length,
                counterBytes.Length);

            byte[] hash =
                SHA256.HashData(input);

            int bytesToCopy = Math.Min(
                hash.Length,
                length - offset);

            Buffer.BlockCopy(
                hash,
                0,
                key,
                offset,
                bytesToCopy);

            offset += bytesToCopy;
            counter++;
        }

        return key;
    }

    /// <summary>
    /// Шифрование файла.
    /// </summary>
    public void EncryptFile(
        string inputFile,
        string outputFile,
        byte[] key)
    {
        if (!File.Exists(inputFile))
        {
            throw new FileNotFoundException(
                "Исходный файл не найден.",
                inputFile);
        }

        byte[] data =
            File.ReadAllBytes(inputFile);

        byte[] encryptedData =
            Encrypt(data, key);

        File.WriteAllBytes(
            outputFile,
            encryptedData);
    }

    /// <summary>
    /// Расшифрование файла.
    /// </summary>
    public void DecryptFile(
        string inputFile,
        string outputFile,
        byte[] key)
    {
        if (!File.Exists(inputFile))
        {
            throw new FileNotFoundException(
                "Зашифрованный файл не найден.",
                inputFile);
        }

        byte[] encryptedData =
            File.ReadAllBytes(inputFile);

        byte[] decryptedData =
            Decrypt(encryptedData, key);

        File.WriteAllBytes(
            outputFile,
            decryptedData);
    }

    private void ValidateInput(
        byte[] data,
        byte[] key)
    {
        if (data == null)
        {
            throw new ArgumentNullException(
                nameof(data));
        }

        if (key == null)
        {
            throw new ArgumentNullException(
                nameof(key));
        }

        if (data.Length != key.Length)
        {
            throw new ArgumentException(
                "Длина ключа должна совпадать " +
                "с длиной данных.");
        }
    }
}