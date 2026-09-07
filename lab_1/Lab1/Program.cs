using System;

namespace Lab1;

class Program
{
    static void Main()
    {
        DiscreteLog.RunInteractive();

        // Вариант 2: запуск на сгенерированных параметрах (для тестов/демо)
        DiscreteLog.RunWithGeneratedParameters();

        // Вариант 3: вызов алгоритма напрямую с конкретными значениями
        var a = 2;
        var y = 8;
        var p = 17;

        var x = DiscreteLog.BabyStepGiantStep(a, y, p);
        if (x.HasValue)
            Console.WriteLine($"Для a={a}, y={y}, p={p} найдено x = {x}");
        else
            Console.WriteLine($"Решение не найдено для a={a}, y={y}, p={p}");

        // Вариант 4: проверка на больших числах (пример)
        var pBig = 104729;
        var aBig = 12345;
        var xTrue = 6789;
        var yBig = DiscreteLog.ModPow(aBig, xTrue, pBig); // y = a^x mod p

        var xFound = DiscreteLog.BabyStepGiantStep(aBig, yBig, pBig);
        Console.WriteLine($"p = {pBig}, a = {aBig}, x_true = {xTrue}, y = {yBig}");
        Console.WriteLine($"Найдено x = {xFound}, совпадает с истинным: {xFound == xTrue}");
        // Console.WriteLine(_fastModular.Solver(2, 8, 10));
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
    }
}
