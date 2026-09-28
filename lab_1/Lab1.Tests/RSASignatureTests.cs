namespace Lab1.Tests;

public class RSASignatureTests
{
    private RSASignature CreateRsa()
    {
        return new RSASignature(32503, 32507);
    }
    
    private string CreateTestDirectory()
    {
        string directory = Path.Combine(
            AppContext.BaseDirectory,
            "TestFilesRSAEncryption");

        Directory.CreateDirectory(directory);

        return directory;
    }

    [Fact]
    public void EncryptFile_AndDecryptFile_ReturnsOriginalData()
    {
        RSASignature rsa = CreateRsa();
        string directory = CreateTestDirectory();

        string file = Path.Combine(directory, Guid.NewGuid() + ".bin");
        string encryptedFile = Path.Combine(directory, Guid.NewGuid() + ".enc");

        try
        {
            byte[] originalData = 
            { 
                0, 1, 2, 3, 10, 20, 50, 100, 200, 255 
            };

            File.WriteAllBytes(file, originalData);

            rsa.SaveEncryptedFile(file, encryptedFile);

            byte[] decryptedData = rsa.DecryptFile(encryptedFile);

            Assert.Equal(originalData, decryptedData);
        }
        finally
        {
            // if (File.Exists(file))
            //     File.Delete(file);
            //
            // if (File.Exists(encryptedFile))
            //     File.Delete(encryptedFile);
        }
    }

    [Fact]
    public void EncryptFile_ReturnsEncryptedArray()
    {
        RSASignature rsa = CreateRsa();
        string directory = CreateTestDirectory();

        string file = Path.Combine(directory, Guid.NewGuid() + ".txt");

        try
        {
            File.WriteAllText(file, "Hello");

            long[] encrypted = rsa.EncryptFile(file);

            Assert.Equal(5, encrypted.Length);

            foreach (long value in encrypted)
            {
                Assert.True(value > 0);
                Assert.True(value < rsa.N);
            }
        }
        finally
        {
            // if (File.Exists(file))
            //     File.Delete(file);
        }
    }

    [Fact]
    public void DecryptFile_WithWrongFile_ThrowsException()
    {
        RSASignature rsa = CreateRsa();

        string directory = CreateTestDirectory();
        string nonExistentFile = Path.Combine(directory, "nonexistent.enc");

        Assert.Throws<FileNotFoundException>(() => 
            rsa.DecryptFile(nonExistentFile));
    }

    [Fact]
    public void EncryptFile_WithNonExistentFile_ThrowsException()
    {
        RSASignature rsa = CreateRsa();
        
        string directory = CreateTestDirectory();
        string nonExistentFile = Path.Combine(directory, "nonexistent.bin");

        Assert.Throws<FileNotFoundException>(() => 
            rsa.EncryptFile(nonExistentFile));
    }

    [Fact]
    public void SaveEncryptedFile_CreatesValidFile()
    {
        RSASignature rsa = CreateRsa();
        string directory = CreateTestDirectory();

        string file = Path.Combine(directory, Guid.NewGuid() + ".txt");
        string encryptedFile = Path.Combine(directory, Guid.NewGuid() + ".enc");

        try
        {
            File.WriteAllText(file, "Test data");

            rsa.SaveEncryptedFile(file, encryptedFile);

            Assert.True(File.Exists(encryptedFile));

            string content = File.ReadAllText(encryptedFile);
            Assert.False(string.IsNullOrWhiteSpace(content));

            string[] values = content.Split(' ', StringSplitOptions.RemoveEmptyEntries);
            Assert.True(values.Length > 0);
        }
        finally
        {
            // if (File.Exists(file))
            //     File.Delete(file);
            //
            // if (File.Exists(encryptedFile))
            //     File.Delete(encryptedFile);
        }
    }

    [Fact]
    public void DifferentFiles_ProduceDifferentEncryptedData()
    {
        RSASignature rsa = CreateRsa();
        string directory = CreateTestDirectory();

        string file1 = Path.Combine(directory, Guid.NewGuid() + ".txt");
        string file2 = Path.Combine(directory, Guid.NewGuid() + ".txt");

        try
        {
            File.WriteAllText(file1, "First file content");
            File.WriteAllText(file2, "Second file content");

            long[] encrypted1 = rsa.EncryptFile(file1);
            long[] encrypted2 = rsa.EncryptFile(file2);

            Assert.NotEqual(encrypted1, encrypted2);
        }
        finally
        {
            // if (File.Exists(file1))
            //     File.Delete(file1);
            //
            // if (File.Exists(file2))
            //     File.Delete(file2);
        }
    }

    [Fact]
    public void EmptyFile_EncryptsAndDecryptsCorrectly()
    {
        RSASignature rsa = CreateRsa();
        string directory = CreateTestDirectory();

        string file = Path.Combine(directory, Guid.NewGuid() + ".bin");
        string encryptedFile = Path.Combine(directory, Guid.NewGuid() + ".enc");

        try
        {
            File.WriteAllBytes(file, Array.Empty<byte>());

            rsa.SaveEncryptedFile(file, encryptedFile);

            byte[] decrypted = rsa.DecryptFile(encryptedFile);

            Assert.Empty(decrypted);
        }
        finally
        {
            // if (File.Exists(file))
            //     File.Delete(file);
            //
            // if (File.Exists(encryptedFile))
            //     File.Delete(encryptedFile);
        }
    }

    [Fact]
    public void LargeFile_EncryptsAndDecryptsCorrectly()
    {
        RSASignature rsa = CreateRsa();
        string directory = CreateTestDirectory();

        string file = Path.Combine(directory, Guid.NewGuid() + ".bin");
        string encryptedFile = Path.Combine(directory, Guid.NewGuid() + ".enc");

        try
        {
            byte[] originalData = new byte[1000];
            new Random(42).NextBytes(originalData);

            File.WriteAllBytes(file, originalData);

            rsa.SaveEncryptedFile(file, encryptedFile);

            byte[] decryptedData = rsa.DecryptFile(encryptedFile);

            Assert.Equal(originalData, decryptedData);
        }
        finally
        {
            // if (File.Exists(file))
            //     File.Delete(file);
            //
            // if (File.Exists(encryptedFile))
            //     File.Delete(encryptedFile);
        }
    }

    [Fact]
    public void DecryptFile_WithCorruptedData_ThrowsException()
    {
        RSASignature rsa = CreateRsa();
        string directory = CreateTestDirectory();

        string corruptedFile = Path.Combine(directory, Guid.NewGuid() + ".enc");

        try
        {
            File.WriteAllText(corruptedFile, "not a number abc xyz");

            Assert.Throws<InvalidDataException>(() => 
                rsa.DecryptFile(corruptedFile));
        }
        finally
        {
            // if (File.Exists(corruptedFile))
            //     File.Delete(corruptedFile);
        }
    }
}