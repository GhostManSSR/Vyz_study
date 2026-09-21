using System;

namespace Lab1;

class Program
{
    static void Main()
    {
        // DiscreteLog.RunInteractive();
        //
        // DiscreteLog.RunWithGeneratedParameters();
        //
        // var a = 2;
        // var y = 8;
        // var p = 17;
        //
        // var x = DiscreteLog.BabyStepGiantStep(a, y, p);
        // if (x.HasValue)
        //     Console.WriteLine($"Для a={a}, y={y}, p={p} найдено x = {x}");
        // else
        //     Console.WriteLine($"Решение не найдено для a={a}, y={y}, p={p}");
        //
        // var pBig = 104729;
        // var aBig = 12345;
        // var xTrue = 6789;
        // var yBig = DiscreteLog.ModPow(aBig, xTrue, pBig); // y = a^x mod p
        //
        // var xFound = DiscreteLog.BabyStepGiantStep(aBig, yBig, pBig);
        // Console.WriteLine($"p = {pBig}, a = {aBig}, x_true = {xTrue}, y = {yBig}");
        // Console.WriteLine($"Найдено x = {xFound}, совпадает с истинным: {xFound == xTrue}");

        // FastModularExponentiation _fastModular =  new FastModularExponentiation();
        //
        //
        // Console.WriteLine(_fastModular.Solver(2, 8, 10));
        //
        // var (h, x, p) = _fastModular.GetNumbers(1);
        //
        // Console.WriteLine(_fastModular.Solver(h, x, p));
        //
        // GcdEvklid _gcdEvklid = new GcdEvklid(_fastModular);
        //
        // var (a ,b) = _gcdEvklid.GetNumbers(1);
        //
        // Console.WriteLine(_gcdEvklid.Gcd(a, b));
        //
        // var (c, d) = _gcdEvklid.GenerateCoprimeNumbers(100, 1000);
        // Console.WriteLine((c,d));
        //
        // Console.WriteLine(_gcdEvklid.Gcd(c, d));
        FastModularExponentiation _fastModularExponentiation = new FastModularExponentiation();
        ElGamel _elGamel = new ElGamel(_fastModularExponentiation);

        _elGamel.SolverElGamel();
        //     var diffieHellman = new DiffieHellman();
        //
        //     Console.WriteLine("Алгоритм Диффи-Хеллмана");
        //     Console.WriteLine();
        //
        //     Console.Write("Введите p: ");
        //     long p = long.Parse(Console.ReadLine()!);
        //
        //     Console.Write("Введите g: ");
        //     long g = long.Parse(Console.ReadLine()!);
        //
        //     Console.Write("Введите закрытый ключ Xa: ");
        //     long xa = long.Parse(Console.ReadLine()!);
        //
        //     Console.Write("Введите закрытый ключ Xb: ");
        //     long xb = long.Parse(Console.ReadLine()!);
        //
        //     try
        //     {
        //         DiffieHellman _diffieHellman = new DiffieHellman();
        //
        //         var (Ya, Yb, keyA, keyB) = _diffieHellman.GenerateSharedKey(p, g, xa, xb);
        //         
        //         Console.WriteLine();
        //         Console.WriteLine("Результат:");
        //         Console.WriteLine();
        //
        //         Console.WriteLine($"p  = {p}");
        //         Console.WriteLine($"g  = {g}");
        //
        //         Console.WriteLine();
        //         Console.WriteLine("Абонент A:");
        //         Console.WriteLine($"Xa = {xa}");
        //         Console.WriteLine($"Ya = {Ya}");
        //
        //         Console.WriteLine();
        //         Console.WriteLine("Абонент B:");
        //         Console.WriteLine($"Xb = {xb}");
        //         Console.WriteLine($"Yb = {Yb}");
        //
        //         Console.WriteLine();
        //         Console.WriteLine($"Общий ключ A: {keyA}");
        //         Console.WriteLine($"Общий ключ B: {keyB}");
        //
        //         Console.WriteLine();
        //
        //         if (keyA == keyB)
        //         {
        //             Console.WriteLine(
        //                 "Общий ключ успешно сформирован.");
        //         }
        //         else
        //         {
        //             Console.WriteLine(
        //                 "Ошибка: ключи не совпадают.");
        //         }
        //     }
        //     catch (ArgumentException ex)
        //     {
        //         Console.WriteLine($"Ошибка: {ex.Message}");
        //     }
        // }
    }
}
