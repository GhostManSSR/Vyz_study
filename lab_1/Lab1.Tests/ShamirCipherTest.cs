namespace Lab1.Tests;

public class ShamirCipherTest
{
    private readonly ShamirCipher _shamir = new ShamirCipher();

    [Theory]
    [InlineData(257,3,5, 100)]
    public void Shamir_EncryptDecryptNumberTest(long p, long ca, long cb, long message)
    {

        var (da, db) = _shamir.CalculatePrivateKeys(p, ca, cb);

        long encrypted = _shamir.EncryptNumber(message, p, ca, cb);

        long decrypted = _shamir.DecryptNumber(encrypted, p, da, db);

        Console.WriteLine($"p = {p}");

        Console.WriteLine($"CA = {ca}");

        Console.WriteLine($"DA = {da}");

        Console.WriteLine($"CB = {cb}");

        Console.WriteLine($"DB = {db}");

        Console.WriteLine($"M = {message}");

        Console.WriteLine($"C = {encrypted}");

        Console.WriteLine($"M' = {decrypted}");

        Assert.Equal(message, decrypted);
    }
    
    [Fact]
    public void Shamir_EncryptDecryptNumber_Test_ReadNumbersTest()
    {
        Console.SetIn(new StringReader(
            "257" + Environment.NewLine +
            "3" + Environment.NewLine +
            "5"
        ));

        var (p, ca, cb) = _shamir.ReadNumbers();

        var (da, db) = _shamir.CalculatePrivateKeys(p, ca, cb);

        long message = 100;

        long encrypted = _shamir.EncryptNumber(message, p, ca, cb);

        long decrypted = _shamir.DecryptNumber(encrypted, p, da, db);

        Console.WriteLine($"p = {p}");
        Console.WriteLine($"CA = {ca}");
        Console.WriteLine($"DA = {da}");
        Console.WriteLine($"CB = {cb}");
        Console.WriteLine($"DB = {db}");
        Console.WriteLine($"M = {message}");
        Console.WriteLine($"C = {encrypted}");
        Console.WriteLine($"M' = {decrypted}");

        Assert.Equal(message, decrypted);
    }

    [Fact]
    public void Shamir_EncryptDecryptNumber_Test_GenerateNumbersTest()
    {
        var (p, ca, cb, da, db) = _shamir.GenerateNumbers();
        long message = 100;
        
        long encrypted = _shamir.EncryptNumber(message, p, ca, cb);

        long decrypted = _shamir.DecryptNumber(encrypted, p, da, db);

        Console.WriteLine($"p = {p}");
        Console.WriteLine($"CA = {ca}");
        Console.WriteLine($"DA = {da}");
        Console.WriteLine($"CB = {cb}");
        Console.WriteLine($"DB = {db}");
        Console.WriteLine($"M = {message}");
        Console.WriteLine($"C = {encrypted}");
        Console.WriteLine($"M' = {decrypted}");

        Assert.Equal(message, decrypted);
    }
    
    [Fact]
    public void Shamir_EncryptDecryptFile_Test()
    {
        string inputFile = "test.txt";
        string encryptedFile = "test.shamir";
        string decryptedFile = "test_decrypted.txt";

        // Создаём исходный файл
        File.WriteAllText(
            inputFile,
            "Привет! Это тестовый файл для шифра Шамира."
        );

        // Ввод p, CA, CB
        Console.SetIn(new StringReader(
            "257" + Environment.NewLine +
            "3" + Environment.NewLine +
            "5"
        ));

        // Получаем p, CA, CB
        var (p, ca, cb) =
            _shamir.ReadNumbers();

        // Получаем DA и DB
        var (da, db) =
            _shamir.CalculatePrivateKeys(
                p,
                ca,
                cb
            );

        // Вывод параметров
        Console.WriteLine($"p = {p}");
        Console.WriteLine($"CA = {ca}");
        Console.WriteLine($"DA = {da}");
        Console.WriteLine($"CB = {cb}");
        Console.WriteLine($"DB = {db}");

        // Шифруем файл
        _shamir.EncryptFile(
            inputFile,
            encryptedFile,
            p,
            ca,
            cb
        );

        Console.WriteLine();
        Console.WriteLine("Файл зашифрован.");

        // Расшифровываем файл
        _shamir.DecryptFile(
            encryptedFile,
            decryptedFile,
            p,
            da,
            db
        );

        Console.WriteLine("Файл расшифрован.");

        // Читаем исходный и расшифрованный файлы
        byte[] original =
            File.ReadAllBytes(inputFile);

        byte[] decrypted =
            File.ReadAllBytes(decryptedFile);

        Console.WriteLine();
        Console.WriteLine($"Размер исходного файла: {original.Length} байт");
        Console.WriteLine($"Размер зашифрованного файла: {new FileInfo(encryptedFile).Length} байт");
        Console.WriteLine($"Размер расшифрованного файла: {decrypted.Length} байт");

        // Проверяем, что файлы полностью совпадают
        Assert.Equal(
            original,
            decrypted
        );

        // Удаляем тестовые файлы
        //File.Delete(inputFile);
       // File.Delete(encryptedFile);
       // File.Delete(decryptedFile);
    }
    
}