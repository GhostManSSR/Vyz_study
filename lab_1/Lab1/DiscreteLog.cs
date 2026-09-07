using System;
namespace Lab1;
using System.Collections.Generic;
using System.Numerics;

public static class DiscreteLog
{
    
    static BigInteger IntegerSqrt(BigInteger n)
    {
        if (n < 0)
            throw new ArgumentException("n must be non-negative", nameof(n));
        if (n == 0)
            return 0;

        // Начальное приближение
        var x = n;
        var y = (x + 1) >> 1;

        while (y < x)
        {
            x = y;
            y = (x + n / x) >> 1;
        }

        return x;
    }
    /// <summary>
    /// Находит x такой, что y ≡ a^x (mod p), используя алгоритм Baby-Step Giant-Step.
    /// Возводит null, если решения нет в диапазоне [0, p-2].
    /// </summary>
    public static BigInteger? BabyStepGiantStep(BigInteger a, BigInteger y, BigInteger p)
    {
        if (p <= 1) throw new ArgumentException("p must be > 1");
        a %= p;
        y %= p;
        if (a == 0)
        {
            // 0^x mod p: 0^0 обычно определяют как 1, 0^x (x>0) = 0
            if (y == 1) return 0;
            if (y == 0) return 1;
            return null;
        }

        var pMinus1 = p - 1;
        var m = IntegerSqrt(pMinus1);

        if (m * m < pMinus1)
            m += 1;

        // Baby steps: a^j mod p, j = 0..m-1
        var table = new Dictionary<BigInteger, BigInteger>();
        var cur = BigInteger.One;
        for (BigInteger j = 0; j < m; j++)
        {
            if (!table.ContainsKey(cur))
                table[cur] = j;
            cur = (cur * a) % p;
        }

        // Вычисляем a^(-m) mod p. Для простого p: a^(p-1-m) mod p
        // В общем случае лучше использовать расширенный Евклид для a^(-1), затем возвести в степень m.
        var aInv = ModInverse(a, p);
        if (aInv == null) return null; // a и p не взаимно просты -> нет обратного
        var factor = ModPow(aInv.Value, m, p); // a^(-m) mod p

        // Giant steps: y * (a^(-m))^i mod p
        var yi = y;
        for (BigInteger i = 0; i < m; i++)
        {
            if (table.TryGetValue(yi, out var j))
            {
                var x = i * m + j;
                // Проверка (опционально)
                if (ModPow(a, x, p) == y)
                    return x;
            }
            yi = (yi * factor) % p;
        }

        return null;
    }

    /// <summary>
    /// Модульное возведение в степень: base^exp mod mod.
    /// </summary>
    public static BigInteger ModPow(BigInteger value, BigInteger exp, BigInteger mod)
    {
        BigInteger result = 1;
        value %= mod;
        while (exp > 0)
        {
            if ((exp & 1) == 1)
                result = (result * value) % mod;
            value = (value * value) % mod;
            exp >>= 1;
        }
        return result;
    }

    /// <summary>
    /// Модульное обратное через расширенный алгоритм Евклида.
    /// Возвращает null, если обратного не существует.
    /// </summary>
    private static BigInteger? ModInverse(BigInteger a, BigInteger mod)
    {
        BigInteger t = 0, newT = 1;
        BigInteger r = mod, newR = a % mod;
        if (newR < 0) newR += mod;

        while (newR != 0)
        {
            var quotient = r / newR;
            (t, newT) = (newT, t - quotient * newT);
            (r, newR) = (newR, r - quotient * newR);
        }

        if (r > 1) return null; // нет обратного
        if (t < 0) t += mod;
        return t;
    }

    /// <summary>
    /// Демонстрация: ввод a, y, p с клавиатуры и вывод x.
    /// </summary>
    public static void RunInteractive()
    {
        Console.Write("Введите a: ");
        var str = Console.ReadLine()!;
        var a = BigInteger.Parse(str);

        Console.Write("Введите y: ");
        var str2 = Console.ReadLine()!;
        var y = BigInteger.Parse(str2);

        Console.Write("Введите p (простое): ");
        var p = BigInteger.Parse(Console.ReadLine()!);

        var x = BabyStepGiantStep(a, y, p);
        if (x.HasValue)
            Console.WriteLine($"Найдено x = {x}");
        else
            Console.WriteLine("Решение не найдено.");
    }

    /// <summary>
    /// Генерация параметров внутри функции (для тестов):
    /// - простое p (небольшое для демонстрации),
    /// - случайное a ∈ [2, p-2],
    /// - случайное x ∈ [0, p-2],
    /// - y = a^x mod p.
    /// </summary>
    public static void RunWithGeneratedParameters()
    {
        // Для реальных задач p должно быть большим простым; здесь — пример.
        var rand = new Random();
        // Простое p (можно заменить на криптографически стойкое)
        var p = new BigInteger(104729); // пример простого

        BigInteger a;
        do
        {
            a = 2 + (BigInteger)rand.Next((int)(p - 3));
        } while (BigInteger.GreatestCommonDivisor(a, p) != 1);

        var xTrue = (BigInteger)rand.Next((int)(p - 1));
        var y = ModPow(a, xTrue, p);

        Console.WriteLine($"Сгенерировано: p = {p}, a = {a}, x_true = {xTrue}, y = {y}");

        var xFound = BabyStepGiantStep(a, y, p);
        if (xFound.HasValue)
            Console.WriteLine($"Найдено x = {xFound}, проверка: {xFound == xTrue}");
        else
            Console.WriteLine("Решение не найдено.");
    }
}