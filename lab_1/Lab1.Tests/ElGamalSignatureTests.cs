namespace Lab1.Tests;

public class ElGamalSignatureTests
{
    private string CreateTestDirectory()
    {
        string directory =
            Path.Combine(
                AppContext.BaseDirectory,
                "TestFilesElGamalSignature");

        Directory.CreateDirectory(directory);

        return directory;
    }

    private ElGamalSignature CreateElGamal()
    {
        /*
         * 1000003 — простое число.
         *
         * 2 является примитивным корнем
         * по модулю 1000003.
         *
         * Закрытый ключ задаём явно,
         * чтобы тесты были воспроизводимыми.
         */
        return new ElGamalSignature(
            1000003,
            2,
            123457);
    }

    [Fact]
    public void Constructor_GeneratesCorrectParameters()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        Assert.Equal(
            1000003,
            elGamal.P);

        Assert.Equal(
            2,
            elGamal.G);

        Assert.Equal(
            123457,
            elGamal.PrivateKey);

        Assert.True(
            elGamal.PublicKey > 1);

        Assert.True(
            elGamal.PublicKey < elGamal.P);
    }

    [Fact]
    public void P_ShouldBePrime()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        Assert.True(
            elGamal.IsPrime(
                elGamal.P));
    }

    [Fact]
    public void Gcd_ShouldWorkCorrectly()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        Assert.Equal(
            1,
            elGamal.Gcd(
                17,
                10));

        Assert.Equal(
            6,
            elGamal.Gcd(
                18,
                24));

        Assert.Equal(
            1,
            elGamal.Gcd(
                7,
                20));
    }

    [Fact]
    public void SignHash_AndVerifyHash_ShouldReturnTrue()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        byte[] hash =
        {
            10,
            20,
            30,
            40,
            50,
            100,
            200,
            255
        };

        ElGamalSignaturePair[] signature =
            elGamal.SignHash(hash);

        bool result =
            elGamal.VerifyHash(
                hash,
                signature);

        Assert.True(result);
    }

    [Fact]
    public void ModifiedHash_ShouldFailVerification()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        byte[] hash =
        {
            10,
            20,
            30,
            40,
            50
        };

        ElGamalSignaturePair[] signature =
            elGamal.SignHash(hash);

        hash[2]++;

        bool result =
            elGamal.VerifyHash(
                hash,
                signature);

        Assert.False(result);
    }

    [Fact]
    public void ModifiedSignature_ShouldFailVerification()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        byte[] hash =
        {
            10,
            20,
            30,
            40,
            50
        };

        ElGamalSignaturePair[] signature =
            elGamal.SignHash(hash);

        signature[2] =
            new ElGamalSignaturePair(
                signature[2].R,
                signature[2].S + 1);

        bool result =
            elGamal.VerifyHash(
                hash,
                signature);

        Assert.False(result);
    }

    [Fact]
    public void SignatureLength_ShouldEqualHashLength()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        byte[] hash =
        {
            1, 2, 3, 4, 5,
            6, 7, 8, 9, 10
        };

        ElGamalSignaturePair[] signature =
            elGamal.SignHash(hash);

        Assert.Equal(
            hash.Length,
            signature.Length);
    }

    [Fact]
    public void Sha256_ShouldProduce32Bytes()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        string directory =
            CreateTestDirectory();

        string file =
            Path.Combine(
                directory,
                Guid.NewGuid() + ".txt");

        try
        {
            File.WriteAllText(
                file,
                "Hello ElGamal!");

            byte[] hash =
                elGamal.CalculateHash(
                    file);

            Assert.Equal(
                32,
                hash.Length);
        }
        finally
        {
            // if (File.Exists(file))
            //     File.Delete(file);
        }
    }

    [Fact]
    public void SignFile_AndVerifyFile_ShouldReturnTrue()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        string directory =
            CreateTestDirectory();

        string file =
            Path.Combine(
                directory,
                Guid.NewGuid() + ".txt");

        try
        {
            File.WriteAllText(
                file,
                "Документ для Эль-Гамаля.");

            ElGamalSignaturePair[] signature =
                elGamal.SignFile(file);

            bool result =
                elGamal.VerifyFile(
                    file,
                    signature);

            Assert.True(result);
        }
        finally
        {
            // if (File.Exists(file))
            //     File.Delete(file);
        }
    }

    [Fact]
    public void ModifiedFile_ShouldFailVerification()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        string directory =
            CreateTestDirectory();

        string file =
            Path.Combine(
                directory,
                Guid.NewGuid() + ".txt");

        try
        {
            File.WriteAllText(
                file,
                "Исходный документ.");

            ElGamalSignaturePair[] signature =
                elGamal.SignFile(file);

            File.WriteAllText(
                file,
                "Изменённый документ.");

            bool result =
                elGamal.VerifyFile(
                    file,
                    signature);

            Assert.False(result);
        }
        finally
        {
            // if (File.Exists(file))
            //     File.Delete(file);
        }
    }

    [Fact]
    public void SaveAndLoadSignature_ShouldWork()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        string directory =
            CreateTestDirectory();

        string signatureFile =
            Path.Combine(
                directory,
                Guid.NewGuid() + ".sig");

        try
        {
            byte[] hash =
            {
                10,
                20,
                30,
                40,
                50
            };

            ElGamalSignaturePair[] original =
                elGamal.SignHash(hash);

            elGamal.SaveSignature(
                signatureFile,
                original);

            ElGamalSignaturePair[] loaded =
                elGamal.LoadSignature(
                    signatureFile);

            Assert.Equal(
                original,
                loaded);
        }
        finally
        {
            // if (File.Exists(signatureFile))
            //     File.Delete(signatureFile);
        }
    }

    [Fact]
    public void SignFile_SaveLoadAndVerify_ShouldWork()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        string directory =
            CreateTestDirectory();

        string file =
            Path.Combine(
                directory,
                Guid.NewGuid() + ".bin");

        string signatureFile =
            Path.Combine(
                directory,
                Guid.NewGuid() + ".sig");

        try
        {
            byte[] data =
            {
                0,
                1,
                2,
                3,
                10,
                20,
                50,
                100,
                200,
                255
            };

            File.WriteAllBytes(
                file,
                data);

            ElGamalSignaturePair[] signature =
                elGamal.SignFile(file);

            elGamal.SaveSignature(
                signatureFile,
                signature);

            ElGamalSignaturePair[] loaded =
                elGamal.LoadSignature(
                    signatureFile);

            bool result =
                elGamal.VerifyFile(
                    file,
                    loaded);

            Assert.True(result);
        }
        finally
        {
            // if (File.Exists(file))
            //     File.Delete(file);
            //
            // if (File.Exists(signatureFile))
            //     File.Delete(signatureFile);
        }
    }

    [Fact]
    public void DifferentFiles_ShouldProduceDifferentSignatures()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        string directory =
            CreateTestDirectory();

        string file1 =
            Path.Combine(
                directory,
                Guid.NewGuid() + ".txt");

        string file2 =
            Path.Combine(
                directory,
                Guid.NewGuid() + ".txt");

        try
        {
            File.WriteAllText(
                file1,
                "First file");

            File.WriteAllText(
                file2,
                "Second file");

            ElGamalSignaturePair[] signature1 =
                elGamal.SignFile(file1);

            ElGamalSignaturePair[] signature2 =
                elGamal.SignFile(file2);

            Assert.NotEqual(
                signature1,
                signature2);
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
    public void WrongSignatureLength_ShouldFail()
    {
        ElGamalSignature elGamal =
            CreateElGamal();

        byte[] hash =
        {
            1,
            2,
            3,
            4
        };

        ElGamalSignaturePair[] signature =
        {
            new(100, 200),
            new(300, 400)
        };

        Assert.False(
            elGamal.VerifyHash(
                hash,
                signature));
    }
}