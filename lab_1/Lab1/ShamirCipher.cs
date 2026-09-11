namespace Lab1;

public class ShamirCipher
{
    private readonly FastModularExponentiation _fastModular;
    private readonly Random _random = new();

    public ShamirCipher()
    {
        _fastModular = new FastModularExponentiation();
    }

    /// <summary>
    /// Ввод p, CA и CB с клавиатуры.
    /// </summary>
    public (long p, long ca, long cb) ReadNumbers()
    {
        long p = _fastModular.ReadNumber("Введите p: ");
        long ca = _fastModular.ReadNumber("Введите CA: ");
        long cb = _fastModular.ReadNumber("Введите CB: ");

        return (p, ca, cb);
    }

    /// <summary>
    /// Генерация p, CA, CB и вычисление DA, DB.
    /// </summary>
    public (long p, long ca, long cb, long da, long db) GenerateNumbers(long min = 257, long max = 1000)
    {
        if (min < 257)
        {
            min = 257;
        }

        while (true)
        {
            long p =
                GeneratePrime(min, max);

            long phi = p - 1;

            long ca =
                GenerateCoprimeNumber(phi);

            long cb =
                GenerateCoprimeNumber(phi);

            long da =
                ModularInverse(ca, phi);

            long db =
                ModularInverse(cb, phi);

            return (
                p,
                ca,
                cb,
                da,
                db);
        }
    }

    /// <summary>
    /// Вычисление недостающих секретных параметров
    /// DA и DB для заданных p, CA и CB.
    /// </summary>
    public (long da, long db) CalculatePrivateKeys(long p, long ca, long cb)
    {
        ValidateParameters(
            p,
            ca,
            cb);

        long phi = p - 1;

        long da =
            ModularInverse(ca, phi);

        long db =
            ModularInverse(cb, phi);

        return (da, db);
    }

    /// <summary>
    /// Шифрование одного числа по схеме Шамира.
    /// </summary>
    public long EncryptNumber(long message, long p, long ca, long cb)
    {
        ValidateParameters(
            p,
            ca,
            cb);

        if (message < 0 || message >= p)
        {
            throw new ArgumentException(
                "Сообщение должно удовлетворять условию 0 <= M < p.");
        }

        // Шаг 1. Алиса:
        // C1 = M^CA mod p
        long c1 =
            _fastModular.Solver(
                message,
                ca,
                p);

        // Шаг 2. Боб:
        // C2 = C1^CB mod p
        long c2 =
            _fastModular.Solver(
                c1,
                cb,
                p);

        return c2;
    }

    /// <summary>
    /// Расшифрование одного числа по схеме Шамира.
    /// </summary>
    public long DecryptNumber(long encrypted, long p, long da, long db)
    {
        if (p <= 2)
        {
            throw new ArgumentException(
                "p должно быть простым числом больше 2.");
        }

        if (encrypted < 0 || encrypted >= p)
        {
            throw new ArgumentException(
                "Зашифрованное значение должно удовлетворять условию 0 <= C < p.");
        }

        // Шаг 3. Алиса:
        // C3 = C2^DA mod p
        long c3 =
            _fastModular.Solver(
                encrypted,
                da,
                p);

        // Шаг 4. Боб:
        // M = C3^DB mod p
        long message =
            _fastModular.Solver(
                c3,
                db,
                p);

        return message;
    }

    /// <summary>
    /// Шифрование файла.
    /// </summary>
    public void EncryptFile(string inputFile, string outputFile, long p, long ca, long cb)
    {
        if (!File.Exists(inputFile))
        {
            throw new FileNotFoundException(
                "Исходный файл не найден.",
                inputFile);
        }

        ValidateParameters(
            p,
            ca,
            cb);

        byte[] data =
            File.ReadAllBytes(inputFile);

        using FileStream output =
            new FileStream(
                outputFile,
                FileMode.Create,
                FileAccess.Write);

        foreach (byte value in data)
        {
            long encrypted =
                EncryptNumber(
                    value,
                    p,
                    ca,
                    cb);

            // Для хранения результата записываем
            // 8 байт long.
            byte[] encryptedBytes =
                BitConverter.GetBytes(encrypted);

            output.Write(
                encryptedBytes,
                0,
                encryptedBytes.Length);
        }
    }

    /// <summary>
    /// Расшифрование файла.
    /// </summary>
    public void DecryptFile(string inputFile, string outputFile, long p, long da, long db)
    {
        if (!File.Exists(inputFile))
        {
            throw new FileNotFoundException(
                "Зашифрованный файл не найден.",
                inputFile);
        }

        using FileStream input =
            new FileStream(
                inputFile,
                FileMode.Open,
                FileAccess.Read);

        using FileStream output =
            new FileStream(
                outputFile,
                FileMode.Create,
                FileAccess.Write);

        byte[] buffer =
            new byte[sizeof(long)];

        while (true)
        {
            int bytesRead =
                input.Read(
                    buffer,
                    0,
                    buffer.Length);

            if (bytesRead == 0)
            {
                break;
            }

            if (bytesRead != sizeof(long))
            {
                throw new InvalidDataException(
                    "Повреждённый зашифрованный файл.");
            }

            long encrypted =
                BitConverter.ToInt64(
                    buffer,
                    0);

            long decrypted =
                DecryptNumber(
                    encrypted,
                    p,
                    da,
                    db);

            if (decrypted < byte.MinValue ||
                decrypted > byte.MaxValue)
            {
                throw new InvalidDataException(
                    "Некорректный расшифрованный байт.");
            }

            output.WriteByte(
                (byte)decrypted);
        }
    }

    /// <summary>
    /// Генерация простого p.
    /// </summary>
    private long GeneratePrime(long min, long max)
    {
        while (true)
        {
            long number =
                _random.NextInt64(
                    min,
                    max + 1);

            if (IsPrime(number))
            {
                return number;
            }
        }
    }

    /// <summary>
    /// Проверка простого числа.
    /// </summary>
    private bool IsPrime(long number)
    {
        if (number < 2)
        {
            return false;
        }

        if (number == 2)
        {
            return true;
        }

        if (number % 2 == 0)
        {
            return false;
        }

        for (long i = 3;
             i * i <= number;
             i += 2)
        {
            if (number % i == 0)
            {
                return false;
            }
        }

        return true;
    }

    /// <summary>
    /// Генерация числа, взаимно простого с phi.
    /// </summary>
    private long GenerateCoprimeNumber(long phi)
    {
        while (true)
        {
            long number =
                _random.NextInt64(
                    2,
                    phi);

            if (Gcd(number, phi) == 1)
            {
                return number;
            }
        }
    }

    /// <summary>
    /// Нахождение обратного элемента:
    ///
    /// a * x ≡ 1 (mod m)
    /// </summary>
    private long ModularInverse(long a, long m)
    {
        long oldR = a;
        long r = m;

        long oldX = 1;
        long x = 0;

        while (r != 0)
        {
            long q = oldR / r;

            long tempR =
                oldR - q * r;

            oldR = r;
            r = tempR;

            long tempX =
                oldX - q * x;

            oldX = x;
            x = tempX;
        }

        if (oldR != 1)
        {
            throw new ArgumentException(
                "Обратный элемент не существует.");
        }

        long result =
            oldX % m;

        if (result < 0)
        {
            result += m;
        }

        return result;
    }

    /// <summary>
    /// НОД двух чисел.
    /// </summary>
    private long Gcd(long a, long b)
    {
        a = Math.Abs(a);
        b = Math.Abs(b);

        while (b != 0)
        {
            (a, b) =
                (b, a % b);
        }

        return a;
    }

    /// <summary>
    /// Проверка параметров шифра.
    /// </summary>
    private void ValidateParameters(long p, long ca, long cb)
    {
        if (!IsPrime(p))
        {
            throw new ArgumentException(
                "p должно быть простым числом.");
        }

        if (p <= 255)
        {
            throw new ArgumentException(
                "Для шифрования любых файлов p должно быть больше 255.");
        }

        long phi = p - 1;

        if (ca <= 1 || ca >= phi)
        {
            throw new ArgumentException(
                "CA должно удовлетворять условию 1 < CA < p - 1.");
        }

        if (cb <= 1 || cb >= phi)
        {
            throw new ArgumentException(
                "CB должно удовлетворять условию 1 < CB < p - 1.");
        }

        if (Gcd(ca, phi) != 1)
        {
            throw new ArgumentException(
                "CA должно быть взаимно простым с p - 1.");
        }

        if (Gcd(cb, phi) != 1)
        {
            throw new ArgumentException(
                "CB должно быть взаимно простым с p - 1.");
        }
    }
}