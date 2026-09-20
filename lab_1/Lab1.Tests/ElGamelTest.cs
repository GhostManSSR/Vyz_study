namespace Lab1.Tests;

public class ElGamelTest
{
    private ElGamel _elgamel;
    private FastModularExponentiation _fastModularExponentiation;
    private NumberTheoryFerma _numberTheoryFerma;

    public ElGamelTest()
    {
        _fastModularExponentiation = new FastModularExponentiation();
        _numberTheoryFerma = new NumberTheoryFerma(_fastModularExponentiation);
        _elgamel = new ElGamel(_fastModularExponentiation);
    }
    
    [Fact]
    public void SolverElGamel_DecryptsMessageCorrectly()
    {
        var result = _elgamel.SolverElGamel();

        Assert.Equal(result.OriginalMessage, result.DecryptedMessage);
    }
    
    [Theory]
    [InlineData(1)]
    [InlineData(5)]
    [InlineData(20)]
    public void SolverElGamel_MultipleRuns_AllDecryptCorrectly(int runs)
    {
        for (int i = 0; i < runs; i++)
        {
            var result = _elgamel.SolverElGamel();

            Assert.Equal(result.OriginalMessage, result.DecryptedMessage);
        }
    }
    
    [Fact]
    public void SolveFromConsole_DecryptsMessageCorrectly()
    {
        Console.SetIn(new StringReader(
            "467" + Environment.NewLine +
            "2" + Environment.NewLine +
            "127" + Environment.NewLine +
            "32" + Environment.NewLine +
            "311"
        ));

        try
        {
            var result = _elgamel.SolveFromConsole();

            Assert.Equal(123, result);
        }
        finally
        {
            Console.SetIn(new StreamReader(Console.OpenStandardInput()));
        }
    }
}