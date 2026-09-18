using System;
namespace Lab1;

public class FastModularExponentiation
{
    private int _lenAction;
    private readonly Random _random = new();
    

    public (long a, long b, long c) ReadNumbers()
    {
        long a = ReadNumber("Введите a: ");
        long b = ReadNumber("Введите x: ");
        long c = ReadNumber("Введите p: ");

        return (a, b, c);
    }
    
    public (long a, long b, long c) GenerateNumbers(
        long min,
        long max)
    {
        long a = GenerateNumber(min, max);
        long b = GenerateNumber(min, max);
        long c =  GenerateNumber(min, max);

        return (a,b, c);
    }
    
    public (long a, long b, long c) GetNumbers(int mode, long min = 2, long max = 1000, int iterations = 20)
    {
        return mode switch
        {
            1 => ReadNumbers(),

            2 => GenerateNumbers(min, max),

            3 => GeneratePrimeNumbers(
                min,
                max,
                iterations),

            _ => throw new ArgumentException(
                "Неизвестный режим генерации.")
        };
    }
    
        public long GeneratePrime(long min, long max, int iterations = 20)
    {
        if (min < 2)
        {
            min = 2;
        }

        if (min > max)
        {
            throw new ArgumentException(
                "Минимальное значение не может быть больше максимального.");
        }

        while (true)
        {
            long number = GenerateNumber(min, max);

            if (IsPrimeFermat(number, iterations))
            {
                return number;
            }
        }
    }
    
    public bool IsPrimeFermat(long n, int iterations = 20)
    {
        if (n < 2)
            return false;

        if (n == 2 || n == 3)
            return true;

        if (n % 2 == 0)
            return false;

        Random random = new();

        for (int i = 0; i < iterations; i++)
        {
            long a = random.NextInt64(2, n - 1);

            if (Gcd(a, n) != 1)
                return false;

            if (Solver(a, n - 1, n) != 1)
                return false;
        }

        return true;
    }
    
    private static long Gcd(long a, long b)
    {
        a = Math.Abs(a);
        b = Math.Abs(b);

        while (b != 0)
        {
            (a, b) = (b, a % b);
        }

        return a;
    }
    
    
    public (long a, long b, long c) GeneratePrimeNumbers(
        long min,
        long max,
        int iterations = 20)
    {
        long a = GeneratePrime(
            min,
            max,
            iterations);

        long b = GeneratePrime(
            min,
            max,
            iterations);
        
        long c = GeneratePrime(min, max, iterations);

        return (a, b, c);
    }
    
    public long GenerateNumber(long min, long max)
    {
        return RandomLong(min, max);
    }
    
    public long RandomLong(long min, long max)
    {
        if (min > max)
        {
            throw new ArgumentException(
                "Минимальное значение не может быть больше максимального.");
        }

        if (min == max)
        {
            return min;
        }

        return _random.NextInt64(min, max + 1);
    }
    
    public long ReadNumber(string message)
    {
        while (true)
        {
            Console.Write(message);

            string? input = Console.ReadLine();

            if (long.TryParse(input, out long number))
            {
                return number;
            }

            Console.WriteLine(
                "Ошибка: необходимо ввести целое число.");
        }
    }
    
    public static long MultiplyModulo(long a, long b, long mod)
    {
        a %= mod;
        b %= mod;

        long result = 0;

        while (b > 0)
        {
            if ((b & 1) != 0)
                result = AddModulo(result, a, mod);

            a = AddModulo(a, a, mod);
            b >>= 1;
        }

        return result;
    }

    private static long AddModulo(long a, long b, long mod)
    {
        // a и b уже в диапазоне [0, mod - 1]
        return a >= mod - b
            ? a - (mod - b)
            : a + b;
    }
    
    
    public long Solver(long parameterA, long parameterX, long parameterP)
    {
        string binary = Convert.ToString(parameterX, 2);
        _lenAction = binary.Length;

        
        long result = 1;
        long current = parameterA % parameterP;

        for (var i = 0; i < _lenAction; i++)
        {
            int bit = binary[_lenAction - 1 - i] - '0';

            if (bit == 1)
            {
                result = result * current % parameterP;
            }

            current = current * current % parameterP;
        }

        return result;
        
    }


}