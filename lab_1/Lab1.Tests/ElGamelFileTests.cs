using System.Security.Cryptography;

namespace Lab1.Tests;

public class ElGamelFileTests
{
    private readonly FastModularExponentiation _fastModularExponentiation;
    private readonly ElGamel _elGamel;

    public ElGamelFileTests()
    {
        _fastModularExponentiation =
            new FastModularExponentiation();

        _elGamel =
            new ElGamel(_fastModularExponentiation);
    }

    [Fact]
    public void EncryptAndDecrypt_TextFile_ShouldRestoreOriginalFile()
    {
        // Arrange
        string directory = CreateTestDirectory();

        string sourceFile =
            Path.Combine(directory, "source.txt");

        string encryptedFile =
            Path.Combine(directory, "encrypted.enc");

        string decryptedFile =
            Path.Combine(directory, "decrypted.txt");

        string originalText =
            "Привет! Это тест шифрования ElGamal.";

        File.WriteAllText(sourceFile, originalText);

        ElGamalKeys keys = _elGamel.GenerateKeys();

        // Act
        _elGamel.EncryptFile(
            sourceFile,
            encryptedFile,
            keys);

        _elGamel.DecryptFile(
            encryptedFile,
            decryptedFile,
            keys);

        // Assert
        string decryptedText =
            File.ReadAllText(decryptedFile);
        
        Assert.Equal(
            File.ReadAllText(sourceFile),
            File.ReadAllText(decryptedFile));
        
        Assert.Equal(
            originalText,
            decryptedText);
    }

    [Fact]
    public void EncryptAndDecrypt_BinaryFile_ShouldRestoreOriginalBytes()
    {
        // Arrange
        string directory = CreateTestDirectory();

        string sourceFile =
            Path.Combine(directory, "image.jpg");

        string encryptedFile =
            Path.Combine(directory, "image.enc");

        string decryptedFile =
            Path.Combine(directory, "image-restored.jpg");

        // Создаём набор всех возможных значений byte.
        byte[] originalBytes = new byte[256];

        for (int i = 0; i < 256; i++)
        {
            originalBytes[i] = (byte)i;
        }

        File.WriteAllBytes(
            sourceFile,
            originalBytes);

        ElGamalKeys keys =
            _elGamel.GenerateKeys();

        // Act
        _elGamel.EncryptFile(
            sourceFile,
            encryptedFile,
            keys);

        _elGamel.DecryptFile(
            encryptedFile,
            decryptedFile,
            keys);

        byte[] decryptedBytes =
            File.ReadAllBytes(decryptedFile);

        // Assert
        Assert.Equal(
            originalBytes,
            decryptedBytes);
    }

    [Fact]
    public void EncryptAndDecrypt_RandomBinaryFile_ShouldRestoreOriginal()
    {
        // Arrange
        string directory = CreateTestDirectory();

        string sourceFile =
            Path.Combine(directory, "random.bin");

        string encryptedFile =
            Path.Combine(directory, "random.enc");

        string decryptedFile =
            Path.Combine(directory, "random-restored.bin");

        byte[] originalBytes = new byte[4096];

        RandomNumberGenerator.Fill(originalBytes);

        File.WriteAllBytes(
            sourceFile,
            originalBytes);

        ElGamalKeys keys =
            _elGamel.GenerateKeys();

        // Act
        _elGamel.EncryptFile(
            sourceFile,
            encryptedFile,
            keys);

        _elGamel.DecryptFile(
            encryptedFile,
            decryptedFile,
            keys);

        byte[] decryptedBytes =
            File.ReadAllBytes(decryptedFile);

        // Assert
        Assert.Equal(
            originalBytes,
            decryptedBytes);
    }

    [Fact]
    public void EncryptAndDecrypt_EmptyFile_ShouldRestoreEmptyFile()
    {
        // Arrange
        string directory = CreateTestDirectory();

        string sourceFile =
            Path.Combine(directory, "empty.dat");

        string encryptedFile =
            Path.Combine(directory, "empty.enc");

        string decryptedFile =
            Path.Combine(directory, "empty-restored.dat");

        File.WriteAllBytes(
            sourceFile,
            Array.Empty<byte>());

        ElGamalKeys keys =
            _elGamel.GenerateKeys();

        // Act
        _elGamel.EncryptFile(
            sourceFile,
            encryptedFile,
            keys);

        _elGamel.DecryptFile(
            encryptedFile,
            decryptedFile,
            keys);

        // Assert
        Assert.True(File.Exists(decryptedFile));

        byte[] decryptedBytes =
            File.ReadAllBytes(decryptedFile);

        Assert.Empty(decryptedBytes);
    }

    [Fact]
    public void EncryptFile_ShouldCreateEncryptedFile()
    {
        // Arrange
        string directory = CreateTestDirectory();

        string sourceFile =
            Path.Combine(directory, "source.bin");

        string encryptedFile =
            Path.Combine(directory, "encrypted.enc");

        byte[] data =
        {
            0,
            1,
            2,
            100,
            127,
            128,
            200,
            254,
            255
        };

        File.WriteAllBytes(
            sourceFile,
            data);

        ElGamalKeys keys =
            _elGamel.GenerateKeys();

        // Act
        _elGamel.EncryptFile(
            sourceFile,
            encryptedFile,
            keys);

        // Assert
        Assert.True(
            File.Exists(encryptedFile));

        Assert.True(
            new FileInfo(encryptedFile).Length > 0);

        byte[] encrypted =
            File.ReadAllBytes(encryptedFile);

        // Зашифрованный файл не должен
        // совпадать с исходным.
        Assert.NotEqual(
            data,
            encrypted);
    }

    [Fact]
    public void DecryptFile_WithWrongKey_ShouldThrow()
    {
        // Arrange
        string directory = CreateTestDirectory();

        string sourceFile =
            Path.Combine(directory, "source.txt");

        string encryptedFile =
            Path.Combine(directory, "encrypted.enc");

        string decryptedFile =
            Path.Combine(directory, "decrypted.txt");

        byte[] originalBytes =
        {
            10, 20, 30, 40, 50,
            100, 150, 200, 250
        };

        File.WriteAllBytes(
            sourceFile,
            originalBytes);

        ElGamalKeys encryptionKeys =
            _elGamel.GenerateKeys();

        ElGamalKeys wrongKeys =
            _elGamel.GenerateKeys();

        // Act
        _elGamel.EncryptFile(
            sourceFile,
            encryptedFile,
            encryptionKeys);

        // Assert
        Assert.Throws<InvalidOperationException>(
            () => _elGamel.DecryptFile(
                encryptedFile,
                decryptedFile,
                wrongKeys));
    }

    [Fact]
    public void EncryptFile_WithMissingInputFile_ShouldThrow()
    {
        // Arrange
        string directory = CreateTestDirectory();

        string sourceFile =
            Path.Combine(directory, "not-exists.bin");

        string encryptedFile =
            Path.Combine(directory, "encrypted.enc");

        ElGamalKeys keys =
            _elGamel.GenerateKeys();

        // Act + Assert
        Assert.Throws<FileNotFoundException>(
            () => _elGamel.EncryptFile(
                sourceFile,
                encryptedFile,
                keys));
    }

    [Fact]
    public void DecryptFile_WithInvalidFile_ShouldThrow()
    {
        // Arrange
        string directory = CreateTestDirectory();

        string invalidFile =
            Path.Combine(directory, "invalid.enc");

        string decryptedFile =
            Path.Combine(directory, "decrypted.bin");

        File.WriteAllBytes(
            invalidFile,
            new byte[]
            {
                1, 2, 3, 4, 5, 6
            });

        ElGamalKeys keys =
            _elGamel.GenerateKeys();

        // Act + Assert
        Assert.Throws<InvalidOperationException>(
            () => _elGamel.DecryptFile(
                invalidFile,
                decryptedFile,
                keys));
    }

    [Fact]
    public void EncryptBlockAndDecryptBlock_ShouldRestoreMessage()
    {
        // Arrange
        ElGamalKeys keys =
            _elGamel.GenerateKeys();

        long message = 123;

        // Act
        var encrypted =
            _elGamel.EncryptBlock(
                message,
                keys);

        long decrypted =
            _elGamel.DecryptBlock(
                encrypted.u,
                encrypted.v,
                keys);

        // Assert
        Assert.Equal(
            message,
            decrypted);
    }

    private static string CreateTestDirectory()
    {
        string directory = Path.Combine(
            AppContext.BaseDirectory,
            "ElGamalTests");

        Directory.CreateDirectory(directory);

        return directory;
    }
}