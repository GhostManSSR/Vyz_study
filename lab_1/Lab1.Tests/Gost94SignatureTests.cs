using System.Numerics;
using Lab10;

namespace Lab1.Tests;

public class Gost94SignatureTests
{
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

    [Fact]
    public void Generate_ShouldCreateCorrectGostParameters()
    {
        Gost94Signature gost =
            Gost94Signature.Generate();

        Assert.Equal(
            31,
            GetBitLength(gost.P));

        Assert.Equal(
            16,
            GetBitLength(gost.Q));

        Assert.Equal(
            BigInteger.Zero,
            (gost.P - 1) % gost.Q);

        Assert.Equal(
            BigInteger.One,
            BigInteger.ModPow(
                gost.A,
                gost.Q,
                gost.P));

        Assert.NotEqual(
            BigInteger.One,
            gost.A);

        Assert.True(
            gost.PrivateKey > 0 &&
            gost.PrivateKey < gost.Q);

        Assert.Equal(
            BigInteger.ModPow(
                gost.A,
                gost.PrivateKey,
                gost.P),
            gost.PublicKey);
    }

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
            Assert.True(
                item.R > 0 &&
                item.R < signature.Q);

            Assert.True(
                item.S > 0 &&
                item.S < signature.Q);
        }
    }

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

        Assert.Contains(
            "ГОСТ Р 34.10-94",
            text);

        Assert.Contains(
            $"p = {signature.P}",
            text);
        Assert.Contains(
            $"q = {signature.Q}",
            text);

        Assert.Contains(
            $"a = {signature.A}",
            text);

        Assert.Contains(
            $"y = {signature.PublicKey}",
            text);

        Assert.Contains(
            Convert.ToHexString(signature.Hash),
            text);

        Assert.Contains(
            "Количество подписей = 32",
            text);
    }

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

        SignatureData loaded =
            Gost94Signature.LoadSignature(
                signatureFile);

        bool result =
            gost.VerifyFile(
                file,
                loaded);

        Assert.True(result);
    }

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

        BigInteger publicKey =
            signer.PublicKey;

        Gost94Signature anotherGost =
            new Gost94Signature(
                signer.P,
                signer.Q,
                signer.A,
                signer.PrivateKey + 1);

        Assert.NotEqual(
            publicKey,
            anotherGost.PublicKey);

        Assert.True(
            signer.VerifyFile(
                file,
                signature));
    }

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

        signature.Signatures[0] = new ByteSignature(oldSignature.R + 1, oldSignature.S);

        bool result =
            gost.VerifyFile(
                file,
                signature);

        Assert.False(result);
    }

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