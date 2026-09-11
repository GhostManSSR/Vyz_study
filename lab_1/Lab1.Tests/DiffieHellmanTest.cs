namespace Lab1.Tests;

public class DiffieHellmanTest
{
    private readonly DiffieHellman _diffieHellman = new DiffieHellman();
    
    [Theory]
    [InlineData(23, 5, 6, 15)]
    public void DiffieHellman_GenerateSharedKeyTest(long p, long g, long xa, long xb)
    {
        
        var (Ya, Yb, keyA, keyB) = _diffieHellman.GenerateSharedKey(p, g, xa, xb);
        
        Console.WriteLine();
        Console.WriteLine("Результат:");
        Console.WriteLine();

        Console.WriteLine($"p  = {p}");
        Console.WriteLine($"g  = {g}");

        Console.WriteLine();
        Console.WriteLine("Абонент A:");
        Console.WriteLine($"Xa = {xa}");
        Console.WriteLine($"Ya = {Ya}");

        Console.WriteLine();
        Console.WriteLine("Абонент B:");
        Console.WriteLine($"Xb = {xb}");
        Console.WriteLine($"Yb = {Yb}");

        Console.WriteLine();
        Console.WriteLine($"Общий ключ A: {keyA}");
        Console.WriteLine($"Общий ключ B: {keyB}");
        
        Assert.Equal(keyA, keyB);
    }

    [Fact]
    public void DiffieHellman_ReadNumbersTest()
    {
        Console.SetIn(new StringReader(
            "23" + Environment.NewLine +
            "5" + Environment.NewLine +
            "6" + Environment.NewLine +
            "15"
        ));

        var (p, g, xa, xb) =
            _diffieHellman.ReadNumbers();

        var (ya, yb, keyA, keyB) =
            _diffieHellman.GenerateSharedKey(
                p,
                g,
                xa,
                xb);

        Console.WriteLine();
        Console.WriteLine("Результат:");
        Console.WriteLine();

        Console.WriteLine($"p  = {p}");
        Console.WriteLine($"g  = {g}");

        Console.WriteLine();
        Console.WriteLine("Абонент A:");
        Console.WriteLine($"Xa = {xa}");
        Console.WriteLine($"Ya = {ya}");

        Console.WriteLine();
        Console.WriteLine("Абонент B:");
        Console.WriteLine($"Xb = {xb}");
        Console.WriteLine($"Yb = {yb}");

        Console.WriteLine();
        Console.WriteLine($"Общий ключ A: {keyA}");
        Console.WriteLine($"Общий ключ B: {keyB}");

        Assert.Equal(keyA, keyB);
    }

    [Fact]
    public void DiffieHellman_GenerateSharedKeyTestGenerateNumbers()
    {
        var (p, g, xa, xb) = _diffieHellman.GenerateNumbers();
        
        var (Ya, Yb, keyA, keyB) = _diffieHellman.GenerateSharedKey(p, g, xa, xb);
        
        Console.WriteLine();
        Console.WriteLine("Результат:");
        Console.WriteLine();

        Console.WriteLine($"p  = {p}");
        Console.WriteLine($"g  = {g}");

        Console.WriteLine();
        Console.WriteLine("Абонент A:");
        Console.WriteLine($"Xa = {xa}");
        Console.WriteLine($"Ya = {Ya}");

        Console.WriteLine();
        Console.WriteLine("Абонент B:");
        Console.WriteLine($"Xb = {xb}");
        Console.WriteLine($"Yb = {Yb}");

        Console.WriteLine();
        Console.WriteLine($"Общий ключ A: {keyA}");
        Console.WriteLine($"Общий ключ B: {keyB}");
        
        Assert.Equal(keyA, keyB);
    }
}