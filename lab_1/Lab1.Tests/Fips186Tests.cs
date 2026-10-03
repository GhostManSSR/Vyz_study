using System.Numerics;
using System.Security.Cryptography;

namespace Lab1.Tests;

public class Fips186Tests
{
    // ============================================================
    // 1. ГЕНЕРАЦИЯ КЛЮЧЕЙ
    // ============================================================

    [Fact]
    public void GenerateKeys_ShouldCreateKeys()
    {
        Fips186 fips = new();

        fips.GenerateKeys();

        Assert.True(fips.PrivateKey > 0);
        Assert.True(fips.PublicKey > 0);
    }

    // ============================================================
    // 2. SHA-256
    // ============================================================

    [Fact]
    public void CalculateHash_ShouldReturnSha256()
    {
        Fips186 fips = new();

        byte[] data =
            "Hello world"u8.ToArray();

        byte[] actual =
            fips.CalculateHash(data);

        byte[] expected =
            SHA256.HashData(data);

        Assert.Equal(expected, actual);
    }

    // ============================================================
    // 3. SHA-256 ДОЛЖЕН БЫТЬ 32 БАЙТА
    // ============================================================

    [Fact]
    public void CalculateHash_ShouldReturn32Bytes()
    {
        Fips186 fips = new();

        byte[] hash =
            fips.CalculateHash(
                "Test"u8.ToArray());

        Assert.Equal(
            32,
            hash.Length);
    }

    // ============================================================
    // 4. ПОДПИСЬ
    // ============================================================

    [Fact]
    public void Sign_ShouldCreate32Parts()
    {
        Fips186 fips = new();

        fips.GenerateKeys();

        byte[] hash =
            fips.CalculateHash(
                "Hello world"u8.ToArray());

        Fips186.DigitalSignature signature =
            fips.Sign(hash);

        Assert.Equal(
            32,
            signature.Parts.Count);
    }

    // ============================================================
    // 5. ПОДПИСЬ + ПРОВЕРКА
    // ============================================================

    [Fact]
    public void SignAndVerify_ShouldReturnTrue()
    {
        Fips186 fips = new();

        fips.GenerateKeys();

        byte[] hash =
            fips.CalculateHash(
                "Hello world"u8.ToArray());

        Fips186.DigitalSignature signature =
            fips.Sign(hash);

        bool result =
            fips.Verify(
                hash,
                signature);

        Assert.True(result);
    }

    // ============================================================
    // 6. ИЗМЕНЕНИЕ ДОКУМЕНТА
    // ============================================================

    [Fact]
    public void ModifiedDocument_ShouldInvalidateSignature()
    {
        Fips186 fips = new();

        fips.GenerateKeys();

        byte[] original =
            "Original document"u8.ToArray();

        byte[] modified =
            "Modified document"u8.ToArray();

        byte[] originalHash =
            fips.CalculateHash(original);

        byte[] modifiedHash =
            fips.CalculateHash(modified);

        Fips186.DigitalSignature signature =
            fips.Sign(originalHash);

        bool result =
            fips.Verify(
                modifiedHash,
                signature);

        Assert.False(result);
    }

    // ============================================================
    // 7. ИЗМЕНЕНИЕ ПОДПИСИ
    // ============================================================

    [Fact]
    public void ModifiedSignature_ShouldInvalidateSignature()
    {
        Fips186 fips = new();

        fips.GenerateKeys();

        byte[] hash =
            fips.CalculateHash(
                "Hello world"u8.ToArray());

        Fips186.DigitalSignature signature =
            fips.Sign(hash);

        signature.Parts[0].R++;

        bool result =
            fips.Verify(
                hash,
                signature);

        Assert.False(result);
    }

    // ============================================================
    // 8. ДРУГОЙ КЛЮЧ
    // ============================================================

    [Fact]
    public void DifferentKey_ShouldInvalidateSignature()
    {
        Fips186 signer = new();
        Fips186 anotherUser = new();

        signer.GenerateKeys();
        anotherUser.GenerateKeys();

        byte[] hash =
            signer.CalculateHash(
                "Hello world"u8.ToArray());

        Fips186.DigitalSignature signature =
            signer.Sign(hash);

        bool result =
            anotherUser.Verify(
                hash,
                signature);

        Assert.False(result);
    }

    // ============================================================
    // 9. ПУСТОЙ ХЭШ
    // ============================================================

    [Fact]
    public void EmptyHash_ShouldCreateEmptySignature()
    {
        Fips186 fips = new();

        fips.GenerateKeys();

        byte[] hash = [];

        Fips186.DigitalSignature signature =
            fips.Sign(hash);

        Assert.Empty(
            signature.Parts);
    }

    // ============================================================
    // 15. РАЗНЫЕ ФАЙЛЫ
    // ============================================================

    [Fact]
    public void DifferentFiles_ShouldHaveDifferentHashes()
    {
        Fips186 fips = new();

        byte[] hash1 =
            fips.CalculateHash(
                "File 1"u8.ToArray());

        byte[] hash2 =
            fips.CalculateHash(
                "File 2"u8.ToArray());

        Assert.NotEqual(
            hash1,
            hash2);
    }

    // ============================================================
    // 16. ПРОВЕРКА НЕКОРРЕКТНОЙ ПОДПИСИ
    // ============================================================

    [Fact]
    public void InvalidSignature_ShouldReturnFalse()
    {
        Fips186 fips = new();

        fips.GenerateKeys();

        byte[] hash =
            fips.CalculateHash(
                "Hello world"u8.ToArray());

        Fips186.DigitalSignature signature =
            fips.Sign(hash);

        signature.Parts[0] =
            new Fips186.SignaturePart
            {
                R = 1,
                S = 1
            };

        bool result =
            fips.Verify(
                hash,
                signature);

        Assert.False(result);
    }
    
    // ============================================================
    // 10. ПОДПИСЬ ФАЙЛА
    // ============================================================

    [Fact]
    public void SignAndVerifyFile_ShouldReturnTrue()
    {
        string directory = CreateTestDirectory();

        string filePath =
            Path.Combine(
                directory,
                "source.txt");

        try
        {
            File.WriteAllText(
                filePath,
                "Test document");

            Fips186 fips = new();

            fips.GenerateKeys();

            Fips186.DigitalSignature signature =
                fips.SignFile(filePath);

            bool result =
                fips.VerifyFile(
                    filePath,
                    signature);

            Assert.True(result);
        }
        finally
        {
            // if (Directory.Exists(directory))
            //     Directory.Delete(
            //         directory,
            //         true);
        }
    }


    // ============================================================
    // 11. ИЗМЕНЕНИЕ ФАЙЛА
    // ============================================================

    [Fact]
    public void ModifiedFile_ShouldInvalidateSignature()
    {
        string directory = CreateTestDirectory();

        string filePath =
            Path.Combine(
                directory,
                "source.txt");

        try
        {
            File.WriteAllText(
                filePath,
                "Original");

            Fips186 fips = new();

            fips.GenerateKeys();

            Fips186.DigitalSignature signature =
                fips.SignFile(filePath);

            File.WriteAllText(
                filePath,
                "Modified");

            bool result =
                fips.VerifyFile(
                    filePath,
                    signature);

            Assert.False(result);
        }
        finally
        {
            // if (Directory.Exists(directory))
            //     Directory.Delete(
            //         directory,
            //         true);
        }
    }


    // ============================================================
    // 12. ИЗМЕНЕНИЕ ФАЙЛА ПРИ ТОМ ЖЕ РАЗМЕРЕ
    // ============================================================

    [Fact]
    public void SameSizeModifiedFile_ShouldInvalidateSignature()
    {
        string directory = CreateTestDirectory();

        string filePath =
            Path.Combine(
                directory,
                "source.txt");

        try
        {
            File.WriteAllText(
                filePath,
                "AAAAAAAAAA");

            Fips186 fips = new();

            fips.GenerateKeys();

            Fips186.DigitalSignature signature =
                fips.SignFile(filePath);

            File.WriteAllText(
                filePath,
                "BBBBBBBBBB");

            bool result =
                fips.VerifyFile(
                    filePath,
                    signature);

            Assert.False(result);
        }
        finally
        {
            // if (Directory.Exists(directory))
            //     Directory.Delete(
            //         directory,
            //         true);
        }
    }

    


    // ============================================================
    // 14. СОХРАНЕНИЕ И ЗАГРУЗКА КЛЮЧЕЙ
    // ============================================================

    [Fact]
    public void SaveAndLoadKeys_ShouldRestoreKeys()
    {
        string directory = CreateTestDirectory();

        string keyPath =
            Path.Combine(
                directory,
                "keys.dat");

        try
        {
            Fips186 first = new();

            first.GenerateKeys();

            BigInteger privateKey =
                first.PrivateKey;

            BigInteger publicKey =
                first.PublicKey;

            first.SaveKeys(
                keyPath);

            Fips186 second = new();

            second.LoadKeys(
                keyPath);

            Assert.Equal(
                privateKey,
                second.PrivateKey);

            Assert.Equal(
                publicKey,
                second.PublicKey);
        }
        finally
        {
            // if (Directory.Exists(directory))
            //     Directory.Delete(
            //         directory,
            //         true);
        }
    }


    // ============================================================
    // 17. СОЗДАНИЕ .SIGNED И .SIG
    // ============================================================

    [Fact]
    public void SignFileToFiles_ShouldCreateSignedAndSignatureFiles()
    {
        string directory = CreateTestDirectory();

        string filePath =
            Path.Combine(
                directory,
                "source.txt");

        string signedFilePath =
            Path.Combine(
                directory,
                "source.txt.signed");

        string signatureFilePath =
            Path.Combine(
                directory,
                "source.txt.sig");

        try
        {
            File.WriteAllText(
                filePath,
                "Test document");

            Fips186 fips = new();

            fips.GenerateKeys();

            fips.SignFileToFiles(
                filePath,
                signedFilePath,
                signatureFilePath);

            Assert.True(
                File.Exists(
                    signedFilePath));

            Assert.True(
                File.Exists(
                    signatureFilePath));

            Assert.True(
                new FileInfo(
                    signedFilePath).Length > 0);

            Assert.True(
                new FileInfo(
                    signatureFilePath).Length > 0);
        }
        finally
        {
            // if (Directory.Exists(directory))
            //     Directory.Delete(
            //         directory,
            //         true);
        }
    }

    // ============================================================
    // 19. ИЗМЕНЕНИЕ ОРИГИНАЛЬНЫХ ДАННЫХ В .SIGNED
    // ============================================================

    [Fact]
    public void ModifiedSignedFile_ShouldInvalidateSignature()
    {
        string directory = CreateTestDirectory();

        string filePath =
            Path.Combine(
                directory,
                "source.txt");

        string signedFilePath =
            Path.Combine(
                directory,
                "source.txt.signed");

        string signatureFilePath =
            Path.Combine(
                directory,
                "source.txt.sig");

        try
        {
            File.WriteAllText(
                filePath,
                "Original document");

            Fips186 fips = new();

            fips.GenerateKeys();

            fips.SignFileToFiles(
                filePath,
                signedFilePath,
                signatureFilePath);

            byte[] signedData =
                File.ReadAllBytes(
                    signedFilePath);

            /*
             * Первые 8 байт содержат размер
             * исходного файла.
             *
             * Поэтому начинаем изменять
             * данные с позиции 8.
             */
            int originalFileStart = 8;

            signedData[originalFileStart] =
                signedData[originalFileStart] == 0
                    ? (byte)1
                    : (byte)0;

            File.WriteAllBytes(
                signedFilePath,
                signedData);

            bool result =
                fips.VerifySignedFile(
                    signedFilePath);

            Assert.False(result);
        }
        finally
        {
            // if (Directory.Exists(directory))
            //     Directory.Delete(
            //         directory,
            //         true);
        }
    }


    // ============================================================
    // 20. ИЗВЛЕЧЕНИЕ ОРИГИНАЛЬНОГО ФАЙЛА
    // ============================================================

    [Fact]
    public void ExtractSignedFile_ShouldRestoreOriginalFile()
    {
        string directory = CreateTestDirectory();

        string filePath =
            Path.Combine(
                directory,
                "source.txt");

        string signedFilePath =
            Path.Combine(
                directory,
                "source.txt.signed");

        string signatureFilePath =
            Path.Combine(
                directory,
                "source.txt.sig");

        string extractedFilePath =
            Path.Combine(
                directory,
                "source.txt.extracted");

        try
        {
            string originalText =
                "Original test document";

            File.WriteAllText(
                filePath,
                originalText);

            Fips186 fips = new();

            fips.GenerateKeys();

            fips.SignFileToFiles(
                filePath,
                signedFilePath,
                signatureFilePath);

            fips.ExtractSignedFile(
                signedFilePath,
                extractedFilePath);

            Assert.True(
                File.Exists(
                    extractedFilePath));

            string extractedText =
                File.ReadAllText(
                    extractedFilePath);

            Assert.Equal(
                originalText,
                extractedText);
        }
        finally
        {
            // if (Directory.Exists(directory))
            //     Directory.Delete(
            //         directory,
            //         true);
        }
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЙ МЕТОД
    // ============================================================

    private static string CreateTestDirectory()
    {
        string directory =
            Path.Combine(
                AppContext.BaseDirectory,
                "Fips186Tests",
                Guid.NewGuid().ToString());

        Directory.CreateDirectory(
            directory);

        return directory;
    }
    
}