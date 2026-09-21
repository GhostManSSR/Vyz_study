using System.Numerics;
using System.Security.Cryptography;
using System.Text;

namespace Lab1.Tests;

public class VernamTests
{
   private readonly Vernam _vernam;
    private readonly FastModularExponentiation _fastModularExponentiation;

    public VernamTests()
    {
        _fastModularExponentiation =
            new FastModularExponentiation();

        _vernam = new Vernam(
            _fastModularExponentiation);
    }

    [Fact]
    public void Encrypt_And_Decrypt_ShouldRestoreOriginalData()
    {
        byte[] original =
            Encoding.UTF8.GetBytes(
                "Hello, Vernam cipher!");

        byte[] key =
            _vernam.GenerateKey(
                original.Length);

        byte[] encrypted =
            _vernam.Encrypt(
                original,
                key);

        byte[] decrypted =
            _vernam.Decrypt(
                encrypted,
                key);

        Assert.Equal(
            original,
            decrypted);
    }

    [Fact]
    public void Encrypt_ShouldPerformXor()
    {
        byte[] data =
        {
            0b_1010_1010,
            0b_1111_0000,
            0b_0000_1111
        };

        byte[] key =
        {
            0b_1111_1111,
            0b_0000_1111,
            0b_1010_1010
        };

        byte[] expected =
        {
            0b_0101_0101,
            0b_1111_1111,
            0b_1010_0101
        };

        byte[] encrypted =
            _vernam.Encrypt(
                data,
                key);

        Assert.Equal(
            expected,
            encrypted);
    }

    [Fact]
    public void Encrypt_WithDifferentKey_ShouldNotRestoreOriginalData()
    {
        byte[] original =
            Encoding.UTF8.GetBytes(
                "Secret information");

        byte[] key =
            _vernam.GenerateKey(
                original.Length);

        byte[] wrongKey =
            _vernam.GenerateKey(
                original.Length);

        byte[] encrypted =
            _vernam.Encrypt(
                original,
                key);

        byte[] decrypted =
            _vernam.Decrypt(
                encrypted,
                wrongKey);

        Assert.NotEqual(
            original,
            decrypted);
    }

    [Fact]
    public void GenerateKey_ShouldHaveRequestedLength()
    {
        int length = 1024;

        byte[] key =
            _vernam.GenerateKey(length);

        Assert.Equal(
            length,
            key.Length);
    }

    [Fact]
    public void GenerateKey_ShouldGenerateDifferentKeys()
    {
        int length = 1000;

        byte[] key1 =
            _vernam.GenerateKey(length);

        byte[] key2 =
            _vernam.GenerateKey(length);

        Assert.NotEqual(
            key1,
            key2);
    }

    [Fact]
    public void GenerateKey_ShouldGenerateBytesInValidRange()
    {
        byte[] key =
            _vernam.GenerateKey(10000);

        Assert.All(
            key,
            value =>
            {
                Assert.InRange(
                    value,
                    byte.MinValue,
                    byte.MaxValue);
            });
    }

    [Fact]
    public void GenerateKeyFromDiffieHellman_ShouldGenerateSameKeyForSameSecret()
    {
        long sharedSecret =
            123456789;

        int keyLength = 1024;

        // Act
        byte[] key1 =
            _vernam.GenerateKeyFromDiffieHellman(
                sharedSecret,
                keyLength);

        byte[] key2 =
            _vernam.GenerateKeyFromDiffieHellman(
                sharedSecret,
                keyLength);

        Assert.Equal(
            keyLength,
            key1.Length);

        Assert.Equal(
            key1,
            key2);
    }

    [Fact]
    public void GenerateKeyFromDiffieHellman_DifferentSecrets_ShouldGenerateDifferentKeys()
    {
        long secret1 =
            123456789;

        long secret2 =
            987654321;

        int keyLength = 1024;

        byte[] key1 =
            _vernam.GenerateKeyFromDiffieHellman(
                secret1,
                keyLength);

        byte[] key2 =
            _vernam.GenerateKeyFromDiffieHellman(
                secret2,
                keyLength);

        Assert.NotEqual(
            key1,
            key2);
    }

    [Fact]
    public void DiffieHellman_TwoParticipants_ShouldGenerateSameVernamKey()
    {
        long p = 23;
        long g = 5;

        long privateAlice = 6;
        long privateBob = 15;

        /*
         * Алиса вычисляет:
         *
         * A = g^a mod p
         */
        long publicAlice =
            _fastModularExponentiation.Solver(
                g,
                privateAlice,
                p);

        /*
         * Боб вычисляет:
         *
         * B = g^b mod p
         */
        long publicBob =
            _fastModularExponentiation.Solver(
                g,
                privateBob,
                p);

        /*
         * Алиса получает общий секрет:
         *
         * K = B^a mod p
         */
        long secretAlice =
            _fastModularExponentiation.Solver(
                publicBob,
                privateAlice,
                p);

        /*
         * Боб получает общий секрет:
         *
         * K = A^b mod p
         */
        long secretBob =
            _fastModularExponentiation.Solver(
                publicAlice,
                privateBob,
                p);

        byte[] keyAlice =
            _vernam.GenerateKeyFromDiffieHellman(
                secretAlice,
                1024);

        byte[] keyBob =
            _vernam.GenerateKeyFromDiffieHellman(
                secretBob,
                1024);

        Assert.Equal(
            secretAlice,
            secretBob);

        Assert.Equal(
            keyAlice,
            keyBob);
    }

    [Fact]
    public void DiffieHellman_Vernam_AliceEncrypts_BobDecrypts()
    {
        long p = 23;
        long g = 5;

        long privateAlice = 6;
        long privateBob = 15;

        /*
         * Открытый ключ Алисы.
         */
        long publicAlice =
            _fastModularExponentiation.Solver(
                g,
                privateAlice,
                p);

        /*
         * Открытый ключ Боба.
         */
        long publicBob =
            _fastModularExponentiation.Solver(
                g,
                privateBob,
                p);

        /*
         * Общий секрет Алисы.
         */
        long secretAlice =
            _fastModularExponentiation.Solver(
                publicBob,
                privateAlice,
                p);

        /*
         * Общий секрет Боба.
         */
        long secretBob =
            _fastModularExponentiation.Solver(
                publicAlice,
                privateBob,
                p);

        Assert.Equal(
            secretAlice,
            secretBob);

        byte[] original =
            Encoding.UTF8.GetBytes(
                "Секретное сообщение " +
                "от Алисы Бобу.");

        /*
         * Алиса и Боб независимо генерируют
         * одинаковый ключ из общего секрета.
         */
        byte[] aliceKey =
            _vernam.GenerateKeyFromDiffieHellman(
                secretAlice,
                original.Length);

        byte[] bobKey =
            _vernam.GenerateKeyFromDiffieHellman(
                secretBob,
                original.Length);

        byte[] encrypted =
            _vernam.Encrypt(
                original,
                aliceKey);

        byte[] decrypted =
            _vernam.Decrypt(
                encrypted,
                bobKey);

        Assert.Equal(
            aliceKey,
            bobKey);

        Assert.NotEqual(
            original,
            encrypted);

        Assert.Equal(
            original,
            decrypted);
    }

    [Fact]
    public void Encrypt_EmptyData_ShouldReturnEmptyData()
    {
        byte[] data =
            Array.Empty<byte>();

        byte[] key =
            Array.Empty<byte>();

        byte[] encrypted =
            _vernam.Encrypt(
                data,
                key);

        Assert.Empty(
            encrypted);
    }

    [Fact]
    public void Encrypt_WithDifferentKeyLength_ShouldThrowException()
    {
        byte[] data =
        {
            1, 2, 3, 4, 5
        };

        byte[] key =
        {
            1, 2, 3
        };

        Assert.Throws<ArgumentException>(
            () => _vernam.Encrypt(
                data,
                key));
    }
    
    [Fact]
    public void EncryptAndDecryptTxtFile_ShouldRestoreOriginalText()
    {
        string directory =
            Path.Combine(
                AppContext.BaseDirectory,
                "TestFiles");

        Directory.CreateDirectory(directory);

        string sourceFile =
            Path.Combine(
                directory,
                "source.txt");

        string encryptedFile =
            Path.Combine(
                directory,
                "encrypted.txt");

        string decryptedFile =
            Path.Combine(
                directory,
                "decrypted.txt");

        string originalText =
            """
            Это исходный текстовый файл.

            Мы проверяем работу шифра Вернама.

            Алиса хочет передать это сообщение Бобу.
            Сначала сообщение будет зашифровано,
            затем Боб расшифрует его с помощью
            такого же ключа.

            1234567890
            ABCDEFGHIJKLMNOPQRSTUVWXYZ
            """;

        try
        {
            File.WriteAllText(
                sourceFile,
                originalText,
                Encoding.UTF8);

            byte[] originalData =
                File.ReadAllBytes(sourceFile);

            byte[] key =
                _vernam.GenerateKey(
                    originalData.Length);

            _vernam.EncryptFile(
                sourceFile,
                encryptedFile,
                key);

            _vernam.DecryptFile(
                encryptedFile,
                decryptedFile,
                key);

            Assert.True(
                File.Exists(encryptedFile));

            Assert.True(
                File.Exists(decryptedFile));

            string decryptedText =
                File.ReadAllText(
                    decryptedFile,
                    Encoding.UTF8);

            Assert.Equal(
                originalText,
                decryptedText);

            // Проверяем, что зашифрованный файл
            // отличается от исходного.
            byte[] encryptedData =
                File.ReadAllBytes(
                    encryptedFile);

            Assert.NotEqual(
                originalData,
                encryptedData);

            /*
             * Выводим пути в консоль тестов,
             * чтобы было удобно найти файлы.
             */
            Console.WriteLine(
                $"Исходный файл: {sourceFile}");

            Console.WriteLine(
                $"Зашифрованный файл: {encryptedFile}");

            Console.WriteLine(
                $"Расшифрованный файл: {decryptedFile}");

            Console.WriteLine(
                $"Размер файла: {originalData.Length} байт");
        }
        finally
        {
           
        }
    }
}