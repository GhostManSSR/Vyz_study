namespace Lab1.Tests;

public class UnitTest1
{
    private readonly FastModularExponentiation _fastModular;
    private readonly GcdEvklid _gcdEvklid;
    private readonly NumberTheoryFerma _numberTheoryFerma;

    public UnitTest1()
    {
        _fastModular = new FastModularExponentiation();
        _gcdEvklid = new GcdEvklid(_fastModular);

        _numberTheoryFerma = new NumberTheoryFerma(
            _fastModular,
            _gcdEvklid);
    }

    [Fact]
    public void FastModularExponentiationTest()
    {
        Assert.Equal(6, _fastModular.Solver(2, 8, 10));

        Assert.Equal(9, _fastModular.Solver(3, 10, 10));
    }

    [Fact]
    public void NumberTheoryFermaTest()
    {
        Assert.False(_numberTheoryFerma.IsPrimeFermat(6));
        Assert.True(_numberTheoryFerma.IsPrimeFermat(11));
    }

    // ============================================================ // Тест обычного алгоритма Евклида // ============================================================ [Fact] public void Gcd_ShouldReturnCorrectValue() { Assert.Equal(6, _gcd.Gcd(48, 18)); Assert.Equal(5, _gcd.Gcd(25, 10)); Assert.Equal(1, _gcd.Gcd(17, 5)); } [Fact] public void Gcd_ShouldWorkWithZero() { Assert.Equal(10, _gcd.Gcd(10, 0)); Assert.Equal(10, _gcd.Gcd(0, 10)); Assert.Equal(0, _gcd.Gcd(0, 0)); } [Fact] public void Gcd_ShouldWorkWithNegativeNumbers() { Assert.Equal(6, _gcd.Gcd(-48, 18)); Assert.Equal(6, _gcd.Gcd(48, -18)); Assert.Equal(6, _gcd.Gcd(-48, -18)); } // ============================================================ // Тест расширенного алгоритма Евклида // ============================================================
    [Fact]
    public void ExtendedGcd_ShouldReturnCorrectGcd()
    {
        long result = _gcdEvklid.ExtendedGcd( 48, 18, out long x, out long y); Assert.Equal(6, result);
    } 
    
    [Fact] 
    public void ExtendedGcd_ShouldSatisfyBezoutIdentity() 
    { 
        long a = 48; long b = 18; long gcd = _gcdEvklid.ExtendedGcd( a, b, out long x, out long y); // Проверяем: // // a*x + b*y = gcd //
        Assert.Equal(gcd, a * x + b * y); 
    } 
    
    [Fact] public void ExtendedGcd_ShouldWorkWithNegativeNumbers() 
    { long a = -48; long b = 18; long gcd = _gcdEvklid.ExtendedGcd( a, b, out long x, out long y); 
        Assert.Equal(6, gcd); Assert.Equal(gcd, a * x + b * y); 
    } 
    
    
    // ============================================================ // Тест генерации одного числа // ============================================================
    
    
    [Fact] public void GenerateNumber_ShouldReturnNumberInRange() 
    { 
        long min = 10; long max = 100; long number = _gcdEvklid.GenerateNumber(min, max); 
        Assert.InRange(number, min, max); 
    } 
    
    
    // ============================================================ // Тест генерации двух чисел // ============================================================


    [Fact]
    public void GenerateNumbers_ShouldReturnNumbersInRange()
    {
        long min = 10; long max = 100; 
        var (a, b) = _gcdEvklid.GenerateNumbers(min, max); 
        Assert.InRange(a, min, max); Assert.InRange(b, min, max);
    } 
    
    // ============================================================ // Тест RandomLong // ============================================================
      
    [Fact] public void RandomLong_ShouldReturnNumberInRange() 
    { long min = 1; long max = 10; long result = _gcdEvklid.RandomLong(min, max); 
        Assert.InRange(result, min, max); 
    }
}

