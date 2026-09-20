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
        // Arrange
        byte[] original =
            Encoding.UTF8.GetBytes(
                "Hello, Vernam cipher!");

        byte[] key =
            _vernam.GenerateKey(
                original.Length);

        // Act
        byte[] encrypted =
            _vernam.Encrypt(
                original,
                key);

        byte[] decrypted =
            _vernam.Decrypt(
                encrypted,
                key);

        // Assert
        Assert.Equal(
            original,
            decrypted);
    }

    [Fact]
    public void Encrypt_ShouldPerformXor()
    {
        // Arrange
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

        // Act
        byte[] encrypted =
            _vernam.Encrypt(
                data,
                key);

        // Assert
        Assert.Equal(
            expected,
            encrypted);
    }

    [Fact]
    public void Encrypt_WithDifferentKey_ShouldNotRestoreOriginalData()
    {
        // Arrange
        byte[] original =
            Encoding.UTF8.GetBytes(
                "Secret information");

        byte[] key =
            _vernam.GenerateKey(
                original.Length);

        byte[] wrongKey =
            _vernam.GenerateKey(
                original.Length);

        // Act
        byte[] encrypted =
            _vernam.Encrypt(
                original,
                key);

        byte[] decrypted =
            _vernam.Decrypt(
                encrypted,
                wrongKey);

        // Assert
        Assert.NotEqual(
            original,
            decrypted);
    }

    [Fact]
    public void GenerateKey_ShouldHaveRequestedLength()
    {
        // Arrange
        int length = 1024;

        // Act
        byte[] key =
            _vernam.GenerateKey(length);

        // Assert
        Assert.Equal(
            length,
            key.Length);
    }

    [Fact]
    public void GenerateKey_ShouldGenerateDifferentKeys()
    {
        // Arrange
        int length = 1000;

        // Act
        byte[] key1 =
            _vernam.GenerateKey(length);

        byte[] key2 =
            _vernam.GenerateKey(length);

        // Assert
        Assert.NotEqual(
            key1,
            key2);
    }

    [Fact]
    public void GenerateKey_ShouldGenerateBytesInValidRange()
    {
        // Arrange
        byte[] key =
            _vernam.GenerateKey(10000);

        // Assert
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
        // Arrange
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

        // Assert
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
        // Arrange
        long secret1 =
            123456789;

        long secret2 =
            987654321;

        int keyLength = 1024;

        // Act
        byte[] key1 =
            _vernam.GenerateKeyFromDiffieHellman(
                secret1,
                keyLength);

        byte[] key2 =
            _vernam.GenerateKeyFromDiffieHellman(
                secret2,
                keyLength);

        // Assert
        Assert.NotEqual(
            key1,
            key2);
    }

    [Fact]
    public void DiffieHellman_TwoParticipants_ShouldGenerateSameVernamKey()
    {
        // Arrange
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

        // Act
        byte[] keyAlice =
            _vernam.GenerateKeyFromDiffieHellman(
                secretAlice,
                1024);

        byte[] keyBob =
            _vernam.GenerateKeyFromDiffieHellman(
                secretBob,
                1024);

        // Assert
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
        // Arrange
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

        // Act
        byte[] encrypted =
            _vernam.Encrypt(
                original,
                aliceKey);

        byte[] decrypted =
            _vernam.Decrypt(
                encrypted,
                bobKey);

        // Assert
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
        // Arrange
        byte[] data =
            Array.Empty<byte>();

        byte[] key =
            Array.Empty<byte>();

        // Act
        byte[] encrypted =
            _vernam.Encrypt(
                data,
                key);

        // Assert
        Assert.Empty(
            encrypted);
    }

    [Fact]
    public void Encrypt_WithDifferentKeyLength_ShouldThrowException()
    {
        // Arrange
        byte[] data =
        {
            1, 2, 3, 4, 5
        };

        byte[] key =
        {
            1, 2, 3
        };

        // Act & Assert
        Assert.Throws<ArgumentException>(
            () => _vernam.Encrypt(
                data,
                key));
    }

    [Fact]
    public void EncryptFile_And_DecryptFile_ShouldRestoreOriginalFile()
    {
        // Arrange
        string directory =
            Path.Combine(
                AppContext.BaseDirectory,
                "TestFiles");

        Directory.CreateDirectory(directory);

        string sourceFile =
            Path.Combine(
                directory,
                "source.bin");

        string encryptedFile =
            Path.Combine(
                directory,
                "encrypted.enc");

        string decryptedFile =
            Path.Combine(
                directory,
                "decrypted.bin");

        try
        {
            /*
             * Создаём бинарный файл.
             *
             * RandomNumberGenerator гарантирует,
             * что внутри будут любые значения байтов
             * от 0 до 255.
             */
            byte[] originalData =
                new byte[1024];

            RandomNumberGenerator.Fill(
                originalData);

            File.WriteAllBytes(
                sourceFile,
                originalData);

            // Ключ должен быть равен размеру файла.
            byte[] key =
                _vernam.GenerateKey(
                    originalData.Length);

            // Act

            _vernam.EncryptFile(
                sourceFile,
                encryptedFile,
                key);

            _vernam.DecryptFile(
                encryptedFile,
                decryptedFile,
                key);

            // Assert

            Assert.True(
                File.Exists(encryptedFile));

            Assert.True(
                File.Exists(decryptedFile));

            byte[] encryptedData =
                File.ReadAllBytes(
                    encryptedFile);

            byte[] decryptedData =
                File.ReadAllBytes(
                    decryptedFile);

            // Зашифрованный файл должен отличаться
            // от исходного.
            Assert.NotEqual(
                originalData,
                encryptedData);

            // После расшифровки должен получиться
            // полностью исходный файл.
            Assert.Equal(
                originalData,
                decryptedData);
        }
        finally
        {
            /*
             * Удаляем файлы после теста.
             *
             * Если хочешь посмотреть их вручную,
             * временно закомментируй этот блок.
             */
            if (File.Exists(sourceFile))
                File.Delete(sourceFile);

            if (File.Exists(encryptedFile))
                File.Delete(encryptedFile);

            if (File.Exists(decryptedFile))
                File.Delete(decryptedFile);

            if (Directory.Exists(directory))
                Directory.Delete(directory);
        }
    }
    [Fact]
    public void EncryptFile_WithDiffieHellmanKey_ShouldRestoreOriginalFile()
    {
        // Arrange

        string directory =
            Path.Combine(
                AppContext.BaseDirectory,
                "TestFiles");

        Directory.CreateDirectory(directory);

        string sourceFile =
            Path.Combine(
                directory,
                "source.bin");

        string encryptedFile =
            Path.Combine(
                directory,
                "encrypted.enc");

        string decryptedFile =
            Path.Combine(
                directory,
                "decrypted.bin");

        try
        {
            /*
             * ==========================================
             * 1. Создаём исходный бинарный файл
             * ==========================================
             */

            byte[] originalData =
                new byte[4096];

            RandomNumberGenerator.Fill(
                originalData);

            File.WriteAllBytes(
                sourceFile,
                originalData);

            /*
             * ==========================================
             * 2. Диффи-Хеллман
             * ==========================================
             */

            long p = 23;
            long g = 5;

            long privateAlice = 6;
            long privateBob = 15;

            /*
             * Открытый ключ Алисы:
             *
             * A = g^a mod p
             */
            long publicAlice =
                _fastModularExponentiation.Solver(
                    g,
                    privateAlice,
                    p);

            /*
             * Открытый ключ Боба:
             *
             * B = g^b mod p
             */
            long publicBob =
                _fastModularExponentiation.Solver(
                    g,
                    privateBob,
                    p);

            /*
             * Алиса получает:
             *
             * K = B^a mod p
             */
            long secretAlice =
                _fastModularExponentiation.Solver(
                    publicBob,
                    privateAlice,
                    p);

            /*
             * Боб получает:
             *
             * K = A^b mod p
             */
            long secretBob =
                _fastModularExponentiation.Solver(
                    publicAlice,
                    privateBob,
                    p);

            /*
             * Оба участника должны получить
             * одинаковый секрет.
             */
            Assert.Equal(
                secretAlice,
                secretBob);

            /*
             * ==========================================
             * 3. Генерируем ключ Вернама
             * ==========================================
             */

            byte[] aliceKey =
                _vernam.GenerateKeyFromDiffieHellman(
                    secretAlice,
                    originalData.Length);

            byte[] bobKey =
                _vernam.GenerateKeyFromDiffieHellman(
                    secretBob,
                    originalData.Length);

            /*
             * Ключи должны быть одинаковыми.
             */
            Assert.Equal(
                aliceKey,
                bobKey);

            /*
             * ==========================================
             * 4. Алиса шифрует файл
             * ==========================================
             */

            _vernam.EncryptFile(
                sourceFile,
                encryptedFile,
                aliceKey);

            /*
             * ==========================================
             * 5. Боб расшифровывает файл
             * ==========================================
             */

            _vernam.DecryptFile(
                encryptedFile,
                decryptedFile,
                bobKey);

            /*
             * ==========================================
             * 6. Проверяем результат
             * ==========================================
             */

            Assert.True(
                File.Exists(encryptedFile));

            Assert.True(
                File.Exists(decryptedFile));

            byte[] encryptedData =
                File.ReadAllBytes(
                    encryptedFile);

            byte[] decryptedData =
                File.ReadAllBytes(
                    decryptedFile);

            /*
             * Шифротекст должен отличаться
             * от исходного файла.
             */
            Assert.NotEqual(
                originalData,
                encryptedData);

            /*
             * Расшифрованный файл должен
             * полностью совпадать с исходным.
             */
            Assert.Equal(
                originalData,
                decryptedData);
        }
        finally
        {
            /*
             * Удаляем тестовые файлы.
             */
            if (File.Exists(sourceFile))
                File.Delete(sourceFile);

            if (File.Exists(encryptedFile))
                File.Delete(encryptedFile);

            if (File.Exists(decryptedFile))
                File.Delete(decryptedFile);

            if (Directory.Exists(directory))
                Directory.Delete(directory);
        }
    }
    
    [Fact]
    public void EncryptAndDecryptTxtFile_ShouldRestoreOriginalText()
    {
        // Arrange
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
            // Записываем исходный текстовый файл.
            File.WriteAllText(
                sourceFile,
                originalText,
                Encoding.UTF8);

            // Читаем байты исходного файла.
            byte[] originalData =
                File.ReadAllBytes(sourceFile);

            // Генерируем ключ такой же длины,
            // как исходный файл.
            byte[] key =
                _vernam.GenerateKey(
                    originalData.Length);

            // Act

            // Шифруем файл.
            _vernam.EncryptFile(
                sourceFile,
                encryptedFile,
                key);

            // Расшифровываем файл.
            _vernam.DecryptFile(
                encryptedFile,
                decryptedFile,
                key);

            // Assert

            Assert.True(
                File.Exists(encryptedFile));

            Assert.True(
                File.Exists(decryptedFile));

            // Читаем расшифрованный текст.
            string decryptedText =
                File.ReadAllText(
                    decryptedFile,
                    Encoding.UTF8);

            // Проверяем, что текст полностью восстановился.
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