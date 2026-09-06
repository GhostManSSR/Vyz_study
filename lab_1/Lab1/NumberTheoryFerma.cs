namespace Lab1;

public class NumberTheoryFerma
{
    private readonly FastModularExponentiation _fastModularExponentiation; 
    private readonly GcdEvklid _gcdEvklid;

    public NumberTheoryFerma(FastModularExponentiation fastModularExponentiation, GcdEvklid gcdEvklid)
    {
        _fastModularExponentiation = fastModularExponentiation;
        _gcdEvklid = gcdEvklid;
    }
    
    
    public bool IsPrimeFermat(long n, int iterations = 20)
    {
        if (n < 2)
            return false;

        if (n == 2 || n == 3)
            return true;

        if (n % 2 == 0)
            return false;

        for (int i = 0; i < iterations; i++)
        {
            long a = _gcdEvklid.RandomLong(2, n - 2);

            if (_gcdEvklid.Gcd(a, n) != 1)
                return false;

            if (_fastModularExponentiation.Solver(a, n - 1, n) != 1)
                return false;
        }

        return true;
    }
}