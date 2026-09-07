using System;

namespace Lab1;

class Program
{
    static void Main()
    {
        FastModularExponentiation _fastModular = new FastModularExponentiation();

        Console.WriteLine(_fastModular.Solver(3, 10, 10));

        GcdEvklid _gcdEvklid = new GcdEvklid(_fastModular);

        var (a ,b) = _gcdEvklid.GetNumbers(1);
        
        Console.WriteLine(_gcdEvklid.Gcd(a, b));
    }
}
