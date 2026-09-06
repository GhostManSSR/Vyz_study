using System;

namespace Lab1;

class Program
{
    static void Main()
    {
        FastModularExponentiation _fastModular = new FastModularExponentiation();

        Console.WriteLine(_fastModular.Solver(3, 10, 10));

    }
}
