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