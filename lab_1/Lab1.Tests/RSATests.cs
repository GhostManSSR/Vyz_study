using System.Numerics;

namespace Lab1.Tests;

public class _rsaTests
{
    private readonly FastModularExponentiation _fastModularExponentiation;
    private readonly NumberTheoryFerma _numberTheoryFerma;
    private readonly RSA _rsa;

    public _rsaTests()
    {
        _fastModularExponentiation =
            new FastModularExponentiation();

        _numberTheoryFerma =
            new NumberTheoryFerma(
                _fastModularExponentiation);

        _rsa =
            new RSA(
                _fastModularExponentiation,
                _numberTheoryFerma);
    }

    [Fact]
    public void GenerateKeys_ShouldCreateValidKeys()
    {
        // Arrange

        // Act
        _rsa.GenerateKeys(
            257,
            1000);

        // Assert
        Assert.True(_rsa.P > 1);
        Assert.True(_rsa.Q > 1);

        Assert.NotEqual(
            _rsa.P,
            _rsa.Q);

        Assert.Equal(
            _rsa.P * _rsa.Q,
            _rsa.N);

        Assert.Equal(
            (_rsa.P - 1) * (_rsa.Q - 1),
            _rsa.Phi);

        Assert.True(_rsa.E > 1);
        Assert.True(_rsa.D > 1);

        Assert.Equal(
            1,
            (_rsa.E * _rsa.D) % _rsa.Phi);
    }

    [Fact]
    public void GenerateKeys_WithPAndQAndD_ShouldCreateValidKeys()
    {
        // Arrange
        long p = 61;
        long q = 53;
        long d = 2753;

        // Act
        _rsa.GenerateKeys(
            p,
            q,
            d);

        // Assert
        Assert.Equal(
            p,
            _rsa.P);

        Assert.Equal(
            q,
            _rsa.Q);

        Assert.Equal(
            3233,
            _rsa.N);

        Assert.Equal(
            3120,
            _rsa.Phi);

        Assert.Equal(
            d,
            _rsa.D);

        Assert.Equal(
            17,
            _rsa.E);

        Assert.Equal(
            1,
            (_rsa.E * _rsa.D) % _rsa.Phi);
    }

    [Fact]
    public void EncryptDecryptByte_ShouldRestoreOriginalValue()
    {
        // Arrange
        _rsa.GenerateKeys(
            61,
            53,
            2753);

        byte original = 123;

        // Act
        long encrypted =
            _rsa.EncryptByte(original);

        byte decrypted =
            _rsa.DecryptByte(encrypted);

        // Assert
        Assert.NotEqual(
            original,
            encrypted);

        Assert.Equal(
            original,
            decrypted);
    }

    [Fact]
    public void EncryptByte_ShouldProduceDifferentValue()
    {
        // Arrange
        _rsa.GenerateKeys(
            61,
            53,
            2753);

        byte original = 100;

        // Act
        long encrypted =
            _rsa.EncryptByte(original);

        // Assert
        Assert.NotEqual(
            original,
            encrypted);
    }

    [Fact]
    public void EncryptDecryptFile_ShouldRestoreOriginalFile()
    {
        // Arrange
        string directory =
            CreateTestDirectory();

        string sourceFile =
            Path.Combine(
                directory,
                "source.txt");

        string encryptedFile =
            Path.Combine(
                directory,
                "encrypted._rsa");

        string decryptedFile =
            Path.Combine(
                directory,
                "decrypted.txt");

        byte[] originalData =
        {
            0,
            1,
            2,
            10,
            50,
            100,
            127,
            128,
            200,
            254,
            255
        };

        File.WriteAllBytes(
            sourceFile,
            originalData);

        _rsa.GenerateKeys(
            61,
            53,
            2753);

        _rsa.EncryptFile(
            sourceFile,
            encryptedFile);

        _rsa.DecryptFile(
            encryptedFile,
            decryptedFile);

        Assert.True(
            File.Exists(encryptedFile));

        Assert.True(
            File.Exists(decryptedFile));

        byte[] decryptedData =
            File.ReadAllBytes(
                decryptedFile);

        Assert.Equal(
            originalData,
            decryptedData);
    }

    [Fact]
    public void EncryptDecryptBinaryFile_ShouldRestoreOriginalBytes()
    {
        string directory =
            CreateTestDirectory();

        string sourceFile =
            Path.Combine(
                directory,
                "image.bin");

        string encryptedFile =
            Path.Combine(
                directory,
                "image._rsa");

        string decryptedFile =
            Path.Combine(
                directory,
                "image_restored.bin");

        byte[] originalData =
            new byte[1024];

        Random random =
            new(12345);

        random.NextBytes(
            originalData);

        File.WriteAllBytes(
            sourceFile,
            originalData);

        _rsa.GenerateKeys(
            61,
            53,
            2753);

        _rsa.EncryptFile(
            sourceFile,
            encryptedFile);

        _rsa.DecryptFile(
            encryptedFile,
            decryptedFile);

        Assert.True(
            File.Exists(encryptedFile));

        Assert.True(
            File.Exists(decryptedFile));

        Assert.Equal(
            originalData,
            File.ReadAllBytes(
                decryptedFile));
    }

    [Fact]
    public void EncryptDecryptEmptyFile_ShouldRestoreEmptyFile()
    {
        string directory =
            CreateTestDirectory();

        string sourceFile =
            Path.Combine(
                directory,
                "empty.dat");

        string encryptedFile =
            Path.Combine(
                directory,
                "empty._rsa");

        string decryptedFile =
            Path.Combine(
                directory,
                "empty_restored.dat");

        File.WriteAllBytes(
            sourceFile,
            Array.Empty<byte>());

        _rsa.GenerateKeys(
            61,
            53,
            2753);

        _rsa.EncryptFile(
            sourceFile,
            encryptedFile);

        _rsa.DecryptFile(
            encryptedFile,
            decryptedFile);

        Assert.True(
            File.Exists(encryptedFile));

        Assert.True(
            File.Exists(decryptedFile));

        Assert.Empty(
            File.ReadAllBytes(
                decryptedFile));
    }

    [Fact]
    public void GenerateKeysFromConsole_ShouldReadP_Q_D()
    {
        TextReader originalInput =
            Console.In;

        try
        {
            Console.SetIn(
                new StringReader(
                    "61\n" +
                    "53\n" +
                    "2753\n"));

            _rsa.GenerateKeysFromConsole();

            Assert.Equal(
                61,
                _rsa.P);

            Assert.Equal(
                53,
                _rsa.Q);

            Assert.Equal(
                3233,
                _rsa.N);

            Assert.Equal(
                3120,
                _rsa.Phi);

            Assert.Equal(
                2753,
                _rsa.D);

            Assert.Equal(
                17,
                _rsa.E);

            Assert.Equal(
                1,
                (_rsa.E * _rsa.D) % _rsa.Phi);
        }
        finally
        {
            Console.SetIn(
                originalInput);
        }
    }

    [Fact]
    public void DecryptFile_WithWrongKey_ShouldThrow()
    {
        string directory =
            CreateTestDirectory();

        string sourceFile =
            Path.Combine(
                directory,
                "source.txt");

        string encryptedFile =
            Path.Combine(
                directory,
                "encrypted._rsa");

        string decryptedFile =
            Path.Combine(
                directory,
                "wrong-key.txt");

        byte[] originalData =
        {
            10,
            20,
            30,
            40,
            50,
            100,
            150,
            200,
            250
        };

        File.WriteAllBytes(
            sourceFile,
            originalData);

        _rsa.GenerateKeys(
            61,
            53,
            2753);

        _rsa.EncryptFile(
            sourceFile,
            encryptedFile);

        FastModularExponentiation fastModular2 =
            new FastModularExponentiation();

        NumberTheoryFerma ferma2 =
            new NumberTheoryFerma(
                fastModular2);

        RSA rsa2 =
            new RSA(
                fastModular2,
                ferma2);

        rsa2.GenerateKeys(
            59,
            47,
            157);

        Assert.Throws<InvalidOperationException>(
            () =>
                rsa2.DecryptFile(
                    encryptedFile,
                    decryptedFile));
    }

    [Fact]
    public void GenerateKeys_WithNonPrimeP_ShouldThrow()
    {
        // Arrange
        long p = 60;
        long q = 53;
        long d = 2753;

        Assert.Throws<ArgumentException>(
            () =>
                _rsa.GenerateKeys(
                    p,
                    q,
                    d));
    }

    [Fact]
    public void GenerateKeys_WithNonPrimeQ_ShouldThrow()
    {
        long p = 61;
        long q = 60;
        long d = 2753;

        Assert.Throws<ArgumentException>(
            () =>
                _rsa.GenerateKeys(
                    p,
                    q,
                    d));
    }

    [Fact]
    public void GenerateKeys_WithSamePAndQ_ShouldThrow()
    {
        long p = 61;
        long q = 61;
        long d = 2753;

        Assert.Throws<ArgumentException>(
            () =>
                _rsa.GenerateKeys(
                    p,
                    q,
                    d));
    }

    [Fact]
    public void GenerateKeys_WithInvalidD_ShouldThrow()
    {
        long p = 61;
        long q = 53;

        // φ(n) = 3120.
        // D = 3120 недопустимо,
        // потому что должно выполняться:
        // 1 < D < φ(n).

        long d = 3120;

        Assert.Throws<ArgumentException>(
            () =>
                _rsa.GenerateKeys(
                    p,
                    q,
                    d));
    }

    [Fact]
    public void GenerateKeys_WithDNotCoprimeToPhi_ShouldThrow()
    {
        long p = 61;
        long q = 53;

        // φ(n) = 3120.
        // gcd(6, 3120) != 1.

        long d = 6;

        Assert.Throws<ArgumentException>(
            () =>
                _rsa.GenerateKeys(
                    p,
                    q,
                    d));
    }

    [Fact]
    public void GenerateKeys_WithSmallPAndQ_ShouldThrow()
    {
        long p = 11;
        long q = 13;
        long d = 7;

        // N = 143 <= 255.

        Assert.Throws<ArgumentException>(
            () =>
                _rsa.GenerateKeys(
                    p,
                    q,
                    d));
    }

    [Fact]
    public void EncryptByte_WithoutKeys_ShouldThrow()
    {
        byte value = 100;

        Assert.Throws<InvalidOperationException>(
            () =>
                _rsa.EncryptByte(value));
    }

    [Fact]
    public void DecryptByte_WithoutKeys_ShouldThrow()
    {
        long encrypted = 100;

        Assert.Throws<InvalidOperationException>(
            () =>
                _rsa.DecryptByte(encrypted));
    }

    [Fact]
    public void EncryptDecrypt_AllByteValues_ShouldRestoreOriginal()
    {
        _rsa.GenerateKeys(
            61,
            53,
            2753);

        for (int i = byte.MinValue;
             i <= byte.MaxValue;
             i++)
        {
            byte original =
                (byte)i;

            long encrypted =
                _rsa.EncryptByte(
                    original);

            byte decrypted =
                _rsa.DecryptByte(
                    encrypted);

            Assert.Equal(
                original,
                decrypted);
        }
    }

    private static string CreateTestDirectory()
    {
        string directory =
            Path.Combine(
                AppContext.BaseDirectory,
                "_rsa_Tests");

        Directory.CreateDirectory(
            directory);

        return directory;
    }
}