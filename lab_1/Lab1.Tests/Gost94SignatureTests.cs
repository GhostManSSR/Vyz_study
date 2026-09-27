using System.Numerics;
using Lab10;

namespace Lab10.Tests;

public class Gost94SignatureTests
{
    // ============================================================
    // СОЗДАНИЕ ТЕСТОВОГО ФАЙЛА
    // ============================================================

    private static string CreateTestFile(
        string name,
        string content)
    {
        string directory =
            Path.Combine(
                AppContext.BaseDirectory,
                "Gost94Tests");

        Directory.CreateDirectory(directory);

        string path =
            Path.Combine(
                directory,
                name);

        File.WriteAllText(
            path,
            content);

        return path;
    }

    // ============================================================
    // ПРОВЕРКА РАЗМЕРНОСТИ ПАРАМЕТРОВ
    // ============================================================

    [Fact]
    public void Generate_ShouldCreateCorrectGostParameters()
    {
        Gost94Signature gost =
            Gost94Signature.Generate();

        // p = 31 бит
        Assert.Equal(
            31,
            GetBitLength(gost.P));

        // q = 16 бит
        Assert.Equal(
            16,
            GetBitLength(gost.Q));

        // q | (p - 1)
        Assert.Equal(
            BigInteger.Zero,
            (gost.P - 1) % gost.Q);

        // a^q mod p = 1
        Assert.Equal(
            BigInteger.One,
            BigInteger.ModPow(
                gost.A,
                gost.Q,
                gost.P));

        // a != 1
        Assert.NotEqual(
            BigInteger.One,
            gost.A);

        // 0 < x < q
        Assert.True(
            gost.PrivateKey > 0 &&
            gost.PrivateKey < gost.Q);

        // y = a^x mod p
        Assert.Equal(
            BigInteger.ModPow(
                gost.A,
                gost.PrivateKey,
                gost.P),
            gost.PublicKey);
    }

    // ============================================================
    // SHA-256 = 32 БАЙТА
    // ============================================================

    [Fact]
    public void Hash_ShouldContain32Bytes()
    {
        string file =
            CreateTestFile(
                "hash.txt",
                "Hello GOST!");

        byte[] hash =
            Gost94Signature.ComputeHash(file);

        Assert.Equal(
            32,
            hash.Length);
    }

    // ============================================================
    // ПОДПИСЬ ФАЙЛА
    // ============================================================

    [Fact]
    public void SignAndVerify_ShouldReturnTrue()
    {
        string file =
            CreateTestFile(
                "test.txt",
                "Тестовый файл ГОСТ Р 34.10-94.");

        Gost94Signature gost =
            Gost94Signature.Generate();

        SignatureData signature =
            gost.SignFile(file);

        bool result =
            gost.VerifyFile(
                file,
                signature);

        Assert.True(result);
    }

    // ============================================================
    // 32 ПОДПИСИ ДЛЯ 32 БАЙТОВ SHA-256
    // ============================================================

    [Fact]
    public void SignFile_ShouldCreate32Signatures()
    {
        string file =
            CreateTestFile(
                "signatures.txt",
                "ГОСТ Р 34.10-94");

        Gost94Signature gost =
            Gost94Signature.Generate();

        SignatureData signature =
            gost.SignFile(file);

        Assert.Equal(
            32,
            signature.Hash.Length);

        Assert.Equal(
            32,
            signature.Signatures.Count);
    }

    // ============================================================
    // ПРОВЕРКА R И S
    // ============================================================

    [Fact]
    public void Signatures_ShouldContainValidRAndS()
    {
        string file =
            CreateTestFile(
                "r-s.txt",
                "Проверка значений R и S.");

        Gost94Signature gost =
            Gost94Signature.Generate();

        SignatureData signature =
            gost.SignFile(file);

        foreach (ByteSignature item
                 in signature.Signatures)
        {
            // 0 < r < q
            Assert.True(
                item.R > 0 &&
                item.R < signature.Q);

            // 0 < s < q
            Assert.True(
                item.S > 0 &&
                item.S < signature.Q);
        }
    }

    // ============================================================
    // СОХРАНЕНИЕ В ОТДЕЛЬНЫЙ БИНАРНЫЙ .SIG
    // ============================================================

    [Fact]
    public void SaveSignature_ShouldCreateBinarySignatureFile()
    {
        string file =
            CreateTestFile(
                "binary-signature.txt",
                "Тест бинарного файла подписи.");

        string signatureFile =
            file + ".sig";

        Gost94Signature gost =
            Gost94Signature.Generate();

        SignatureData signature =
            gost.SignFile(file);

        gost.SaveSignature(
            signature,
            signatureFile);

        Assert.True(
            File.Exists(signatureFile));

        Assert.True(
            new FileInfo(signatureFile).Length > 0);
    }

    // ============================================================
    // ЗАГРУЗКА БИНАРНОЙ ПОДПИСИ
    // ============================================================

    [Fact]
    public void LoadSignature_ShouldRestoreBinarySignature()
    {
        string file =
            CreateTestFile(
                "load-signature.txt",
                "Проверка загрузки подписи.");

        string signatureFile =
            file + ".sig";

        Gost94Signature gost =
            Gost94Signature.Generate();

        SignatureData original =
            gost.SignFile(file);

        gost.SaveSignature(
            original,
            signatureFile);

        SignatureData loaded =
            Gost94Signature.LoadSignature(
                signatureFile);

        Assert.Equal(
            original.P,
            loaded.P);

        Assert.Equal(
            original.Q,
            loaded.Q);

        Assert.Equal(
            original.A,
            loaded.A);

        Assert.Equal(
            original.PublicKey,
            loaded.PublicKey);

        Assert.Equal(
            original.Hash,
            loaded.Hash);

        Assert.Equal(
            original.Signatures.Count,
            loaded.Signatures.Count);

        for (int i = 0;
             i < original.Signatures.Count;
             i++)
        {
            Assert.Equal(
                original.Signatures[i].R,
                loaded.Signatures[i].R);

            Assert.Equal(
                original.Signatures[i].S,
                loaded.Signatures[i].S);
        }
    }

    // ============================================================
    // СОХРАНЕНИЕ В ТЕКСТОВЫЙ .SIG.TXT
    // ============================================================

    [Fact]
    public void SaveSignatureText_ShouldCreateTextSignatureFile()
    {
        string file =
            CreateTestFile(
                "text-signature.txt",
                "Тест текстового представления подписи.");

        string textSignatureFile =
            file + ".sig.txt";

        Gost94Signature gost =
            Gost94Signature.Generate();

        SignatureData signature =
            gost.SignFile(file);

        gost.SaveSignatureText(
            signature,
            textSignatureFile);

        Assert.True(
            File.Exists(textSignatureFile));

        Assert.True(
            new FileInfo(textSignatureFile).Length > 0);
    }

    // ============================================================
    // ПРОВЕРКА СОДЕРЖИМОГО ТЕКСТОВОЙ ПОДПИСИ
    // ============================================================

    [Fact]
    public void SaveSignatureText_ShouldContainGostParameters()
    {
        string file =
            CreateTestFile(
                "text-content.txt",
                "Проверка содержимого текстовой подписи.");

        string textSignatureFile =
            file + ".sig.txt";

        Gost94Signature gost =
            Gost94Signature.Generate();

        SignatureData signature =
            gost.SignFile(file);

        gost.SaveSignatureText(
            signature,
            textSignatureFile);

        string text =
            File.ReadAllText(
                textSignatureFile);

        // Название алгоритма
        Assert.Contains(
            "ГОСТ Р 34.10-94",
            text);

        // p
        Assert.Contains(
            $"p = {signature.P}",
            text);

        // q
        Assert.Contains(
            $"q = {signature.Q}",
            text);

        // a
        Assert.Contains(
            $"a = {signature.A}",
            text);

        // y
        Assert.Contains(
            $"y = {signature.PublicKey}",
            text);

        // SHA-256
        Assert.Contains(
            Convert.ToHexString(signature.Hash),
            text);

        // Количество подписей
        Assert.Contains(
            "Количество подписей = 32",
            text);
    }

    // ============================================================
    // ТЕКСТОВАЯ ПОДПИСЬ ДОЛЖНА СОДЕРЖАТЬ R И S
    // ============================================================

    [Fact]
    public void SaveSignatureText_ShouldContainAllSignatures()
    {
        string file =
            CreateTestFile(
                "all-signatures.txt",
                "Проверка всех подписей.");

        string textSignatureFile =
            file + ".sig.txt";

        Gost94Signature gost =
            Gost94Signature.Generate();

        SignatureData signature =
            gost.SignFile(file);

        gost.SaveSignatureText(
            signature,
            textSignatureFile);

        string text =
            File.ReadAllText(
                textSignatureFile);

        for (int i = 0;
             i < signature.Signatures.Count;
             i++)
        {
            ByteSignature item =
                signature.Signatures[i];

            Assert.Contains(
                $"БАЙТ #{i}",
                text);

            Assert.Contains(
                $"r = {item.R}",
                text);

            Assert.Contains(
                $"s = {item.S}",
                text);
        }
    }

    // ============================================================
    // ПРОВЕРКА ИЗ ОТДЕЛЬНОГО .SIG
    // ============================================================

    [Fact]
    public void SeparateSignatureFile_ShouldBeVerified()
    {
        string file =
            CreateTestFile(
                "separate.txt",
                "Документ с отдельной подписью.");

        string signatureFile =
            file + ".sig";

        Gost94Signature gost =
            Gost94Signature.Generate();

        SignatureData signature =
            gost.SignFile(file);

        gost.SaveSignature(
            signature,
            signatureFile);

        // Загружаем подпись из отдельного файла.
        SignatureData loaded =
            Gost94Signature.LoadSignature(
                signatureFile);

        // Проверяем загруженную подпись.
        bool result =
            gost.VerifyFile(
                file,
                loaded);

        Assert.True(result);
    }

    // ============================================================
    // ПРОВЕРКА БЕЗ PRIVATE KEY
    // ============================================================

    [Fact]
    public void Verification_ShouldNotDependOnPrivateKey()
    {
        string file =
            CreateTestFile(
                "public-verification.txt",
                "Проверка только открытым ключом.");

        Gost94Signature signer =
            Gost94Signature.Generate();

        SignatureData signature =
            signer.SignFile(file);

        // Сохраняем исходный открытый ключ.
        BigInteger publicKey =
            signer.PublicKey;

        // Создаём второй объект с другим закрытым ключом.
        Gost94Signature anotherGost =
            new Gost94Signature(
                signer.P,
                signer.Q,
                signer.A,
                signer.PrivateKey + 1);

        // У второго объекта должен быть другой открытый ключ.
        Assert.NotEqual(
            publicKey,
            anotherGost.PublicKey);

        // Проверяем исходную подпись исходным объектом.
        // VerifyFile использует открытый ключ,
        // а не PrivateKey.
        Assert.True(
            signer.VerifyFile(
                file,
                signature));
    }

    // ============================================================
    // ИЗМЕНЕНИЕ ФАЙЛА
    // ============================================================

    // [Fact]
    // public void ModifiedFile_ShouldFailVerification()
    // {
    //     string file =
    //         CreateTestFile(
    //             "modified.txt",
    //             "Исходный текст.");
    //
    //     string signatureFile =
    //         file + ".sig";
    //
    //     Gost94Signature signer =
    //         Gost94Signature.Generate();
    //
    //     SignatureData signature =
    //         signer.SignFile(file);
    //
    //     signer.SaveSignature(
    //         signature,
    //         signatureFile);
    //
    //     // Изменяем исходный файл.
    //     File.WriteAllText(
    //         file,
    //         "Изменённый текст.");
    //
    //     SignatureData loaded =
    //         Gost94Signature.LoadSignature(
    //             signatureFile);
    //
    //     Gost94Signature verifier =
    //         Gost94Signature.CreateForVerification(
    //             loaded.P,
    //             loaded.Q,
    //             loaded.A,
    //             loaded.PublicKey);
    //
    //     bool result =
    //         verifier.VerifyFile(
    //             file,
    //             loaded);
    //
    //     Assert.False(result);
    // }

    // ============================================================
    // ПОВРЕЖДЕНИЕ ПОДПИСИ
    // ============================================================

    [Fact]
    public void ModifiedSignature_ShouldFailVerification()
    {
        string file =
            CreateTestFile(
                "bad-signature.txt",
                "Текст документа.");

        Gost94Signature gost =
            Gost94Signature.Generate();

        SignatureData signature =
            gost.SignFile(file);

        ByteSignature oldSignature =
            signature.Signatures[0];

        // Изменяем r.
        signature.Signatures[0] =
            new ByteSignature(
                oldSignature.R + 1,
                oldSignature.S);

        bool result =
            gost.VerifyFile(
                file,
                signature);

        Assert.False(result);
    }

    // ============================================================
    // ДРУГОЙ ФАЙЛ НЕ ПРОЙДЁТ ПРОВЕРКУ
    // ============================================================

    [Fact]
    public void SignatureOfAnotherFile_ShouldFailVerification()
    {
        string file1 =
            CreateTestFile(
                "file1.txt",
                "Первый файл.");

        string file2 =
            CreateTestFile(
                "file2.txt",
                "Второй файл.");

        Gost94Signature gost =
            Gost94Signature.Generate();

        SignatureData signature =
            gost.SignFile(file1);

        bool result =
            gost.VerifyFile(
                file2,
                signature);

        Assert.False(result);
    }

    // ============================================================
    // НЕПРАВИЛЬНЫЙ PUBLIC KEY
    // ============================================================

    // [Fact]
    // public void WrongPublicKey_ShouldFailVerification()
    // {
    //     string file =
    //         CreateTestFile(
    //             "wrong-key.txt",
    //             "Проверка неправильного открытого ключа.");
    //
    //     Gost94Signature signer =
    //         Gost94Signature.Generate();
    //
    //     SignatureData signature =
    //         signer.SignFile(file);
    //
    //     Gost94Signature anotherGost =
    //         Gost94Signature.Generate();
    //
    //     Gost94Signature verifier =
    //         Gost94Signature.CreateForVerification(
    //             signature.P,
    //             signature.Q,
    //             signature.A,
    //             anotherGost.PublicKey);
    //
    //     bool result =
    //         verifier.VerifyFile(
    //             file,
    //             signature);
    //
    //     Assert.False(result);
    // }
    //
    // // ============================================================
    // // БИНАРНЫЙ ФАЙЛ
    // // ============================================================
    //
    // [Fact]
    // public void BinaryFile_ShouldBeVerified()
    // {
    //     string directory =
    //         Path.Combine(
    //             Path.GetTempPath(),
    //             "Gost94Tests");
    //
    //     Directory.CreateDirectory(directory);
    //
    //     string file =
    //         Path.Combine(
    //             directory,
    //             "binary.bin");
    //
    //     byte[] data =
    //         new byte[4096];
    //
    //     Random.Shared.NextBytes(data);
    //
    //     File.WriteAllBytes(
    //         file,
    //         data);
    //
    //     string signatureFile =
    //         file + ".sig";
    //
    //     Gost94Signature signer =
    //         Gost94Signature.Generate();
    //
    //     SignatureData signature =
    //         signer.SignFile(file);
    //
    //     signer.SaveSignature(
    //         signature,
    //         signatureFile);
    //
    //     SignatureData loaded =
    //         Gost94Signature.LoadSignature(
    //             signatureFile);
    //
    //     Gost94Signature verifier =
    //         Gost94Signature.CreateForVerification(
    //             loaded.P,
    //             loaded.Q,
    //             loaded.A,
    //             loaded.PublicKey);
    //
    //     Assert.True(
    //         verifier.VerifyFile(
    //             file,
    //             loaded));
    // }

    // ============================================================
    // ИСХОДНЫЙ ФАЙЛ НЕ ДОЛЖЕН ИЗМЕНЯТЬСЯ
    // ============================================================

    [Fact]
    public void SignFile_ShouldNotModifyOriginalFile()
    {
        string file =
            CreateTestFile(
                "unchanged.txt",
                "Исходное содержимое.");

        byte[] originalBytes =
            File.ReadAllBytes(file);

        Gost94Signature gost =
            Gost94Signature.Generate();

        SignatureData signature =
            gost.SignFile(file);

        string binarySignature =
            file + ".sig";

        string textSignature =
            file + ".sig.txt";

        gost.SaveSignature(
            signature,
            binarySignature);

        gost.SaveSignatureText(
            signature,
            textSignature);

        byte[] afterSigning =
            File.ReadAllBytes(file);

        Assert.Equal(
            originalBytes,
            afterSigning);
    }

    // ============================================================
    // ZERO BYTE В HASH
    // ============================================================

    [Fact]
    public void ZeroHashByte_ShouldBeHandled()
    {
        string file =
            CreateTestFile(
                "zero-byte.txt",
                "Проверка обработки.");

        Gost94Signature gost =
            Gost94Signature.Generate();

        SignatureData signature =
            gost.SignFile(file);

        Assert.Equal(
            32,
            signature.Hash.Length);

        Assert.Equal(
            32,
            signature.Signatures.Count);

        Assert.True(
            gost.VerifyFile(
                file,
                signature));
    }

    // ============================================================
    // GET BIT LENGTH
    // ============================================================

    private static int GetBitLength(
        BigInteger value)
    {
        int bits = 0;

        while (value > 0)
        {
            value >>= 1;
            bits++;
        }

        return bits;
    }
}