namespace Lab1.Tests;

public class RSASignatureTests
{
    /*
     * p = 32503
     * q = 32507
     *
     * N = 1022117
     *
     * Этого достаточно, чтобы любой байт SHA-256
     * (0..255) был меньше N.
     */
    private RSASignature CreateRsa()
    {
        return new RSASignature(32503, 32507);
    }
    
    private string CreateTestDirectory()
    {
        string directory = Path.Combine(
            AppContext.BaseDirectory,
            "TestFilesRSASignature");

        Directory.CreateDirectory(directory);

        return directory;
    }

    [Fact]
    public void Constructor_GeneratesCorrectParameters()
    {
        RSASignature rsa = CreateRsa();

        Assert.Equal(32503, rsa.P);
        Assert.Equal(32507, rsa.Q);

        Assert.Equal(
            32503L * 32507L,
            rsa.N);

        Assert.Equal(
            (32503L - 1) * (32507L - 1),
            rsa.Phi);

        Assert.True(rsa.PublicKey > 1);
        Assert.True(rsa.PrivateKey > 1);
    }

    [Fact]
    public void Constructor_KeysAreMutuallyInverse()
    {
        RSASignature rsa = CreateRsa();

        long value =
            (long)(
                (System.Numerics.BigInteger)
                    rsa.PublicKey *
                rsa.PrivateKey %
                rsa.Phi);

        Assert.Equal(1, value);
    }

    [Fact]
    public void IsPrime_WorksCorrectly()
    {
        RSASignature rsa = CreateRsa();

        Assert.True(rsa.IsPrime(2));
        Assert.True(rsa.IsPrime(3));
        Assert.True(rsa.IsPrime(1009));
        Assert.True(rsa.IsPrime(1013));

        Assert.False(rsa.IsPrime(1));
        Assert.False(rsa.IsPrime(4));
        Assert.False(rsa.IsPrime(1000));
        Assert.False(rsa.IsPrime(1001));
    }

    [Fact]
    public void SignHash_AndVerifyHash_ReturnTrue()
    {
        RSASignature rsa = CreateRsa();

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

        long[] signature = rsa.SignHash(hash);

        bool result = rsa.VerifyHash(
            hash,
            signature);

        Assert.True(result);
    }

    [Fact]
    public void ModifiedHash_ShouldFailVerification()
    {
        RSASignature rsa = CreateRsa();

        byte[] hash =
        {
            10,
            20,
            30,
            40,
            50
        };

        long[] signature = rsa.SignHash(hash);

        hash[2]++;

        bool result = rsa.VerifyHash(
            hash,
            signature);

        Assert.False(result);
    }

    [Fact]
    public void ModifiedSignature_ShouldFailVerification()
    {
        RSASignature rsa = CreateRsa();

        byte[] hash =
        {
            10,
            20,
            30,
            40,
            50
        };

        long[] signature = rsa.SignHash(hash);

        signature[2]++;

        bool result = rsa.VerifyHash(
            hash,
            signature);

        Assert.False(result);
    }

    [Fact]
    public void SignatureLength_EqualsHashLength()
    {
        RSASignature rsa = CreateRsa();

        byte[] hash =
        {
            1, 2, 3, 4, 5,
            6, 7, 8, 9, 10
        };

        long[] signature = rsa.SignHash(hash);

        Assert.Equal(
            hash.Length,
            signature.Length);
    }

    [Fact]
    public void Sha256Hash_Has32Bytes()
    {
        RSASignature rsa = CreateRsa();

        string directory = CreateTestDirectory();

        string file =
            Path.Combine(
                directory,
                Guid.NewGuid() + ".txt");

        try
        {
            File.WriteAllText(
                file,
                "Hello RSA signature!");

            byte[] hash =
                rsa.CalculateHash(file);

            Assert.Equal(32, hash.Length);
        }
        finally
        {
            // if (File.Exists(file))
            //     File.Delete(file);
        }
    }

    [Fact]
    public void SignFile_AndVerifyFile_ReturnTrue()
    {
        RSASignature rsa = CreateRsa();
        
        string directory = CreateTestDirectory();

        string file =
            Path.Combine(
                directory,
                Guid.NewGuid() + ".txt");

        try
        {
            File.WriteAllText(
                file,
                "Документ для проверки RSA.");

            long[] signature =
                rsa.SignFile(file);

            bool result =
                rsa.VerifyFile(
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
        RSASignature rsa = CreateRsa();
        string directory = CreateTestDirectory();


        string file =
            Path.Combine(
                directory,
                Guid.NewGuid() + ".txt");

        try
        {
            File.WriteAllText(
                file,
                "Исходный документ.");

            long[] signature =
                rsa.SignFile(file);

            File.WriteAllText(
                file,
                "Изменённый документ.");

            bool result =
                rsa.VerifyFile(
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
        RSASignature rsa = CreateRsa();
        
        string directory = CreateTestDirectory();


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

            long[] original =
                rsa.SignHash(hash);

            rsa.SaveSignature(
                signatureFile,
                original);

            long[] loaded =
                rsa.LoadSignature(
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
        RSASignature rsa = CreateRsa();
        
        string directory = CreateTestDirectory();

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

            long[] signature =
                rsa.SignFile(file);

            rsa.SaveSignature(
                signatureFile,
                signature);

            long[] loadedSignature =
                rsa.LoadSignature(
                    signatureFile);

            bool result =
                rsa.VerifyFile(
                    file,
                    loadedSignature);

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
    public void WrongSignatureLength_ShouldFail()
    {
        RSASignature rsa = CreateRsa();

        byte[] hash =
        {
            1,
            2,
            3,
            4
        };

        long[] signature =
        {
            10,
            20
        };

        Assert.False(
            rsa.VerifyHash(
                hash,
                signature));
    }

    [Fact]
    public void DifferentFiles_ShouldProduceDifferentSignatures()
    {
        RSASignature rsa = CreateRsa();
        
        string directory = CreateTestDirectory();

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

            long[] signature1 =
                rsa.SignFile(file1);

            long[] signature2 =
                rsa.SignFile(file2);

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
}