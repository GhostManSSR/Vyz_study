using System.Numerics;

namespace Lab1.Tests;

public class DiscreteLogTest
{
    [Theory]
    [InlineData(2, 0, 13, 1)]
    [InlineData(2, 10, 1000, 24)]
    [InlineData(3, 5, 7, 5)]
    [InlineData(15, 3, 7, 1)]
    public void ModPow_ReturnsCorrectValue(
        int value,
        int exponent,
        int modulus,
        int expected)
    {
        var result = DiscreteLog.ModPow(value, exponent, modulus);

        Assert.Equal(new BigInteger(expected), result);
    }

    [Fact]
    public void BabyStepGiantStep_Generates()
    {
        Assert.True(DiscreteLog.RunWithGeneratedParameters());
    }

    [Fact]
    public void RunInteractive_WhenSolutionExists_PrintsFoundX()
    {
        // Arrange
        // Ищем x в выражении:
        // 2^x ≡ 8 (mod 11)
        // Верный ответ: x = 3.
        var input = new StringReader(
            "2" + Environment.NewLine +
            "8" + Environment.NewLine +
            "11" + Environment.NewLine
        );

        var output = new StringWriter();

        var originalIn = Console.In;
        var originalOut = Console.Out;

        try
        {
            Console.SetIn(input);
            Console.SetOut(output);

            DiscreteLog.RunInteractive();

            var result = output.ToString();

            Assert.Contains("Введите a:", result);
            Assert.Contains("Введите y:", result);
            Assert.Contains("Введите p (простое):", result);
            Assert.Contains("Найдено x = 3", result);
        }
        finally
        {
            Console.SetIn(originalIn);
            Console.SetOut(originalOut);
        }
    }

    [Fact]
    public void BabyStepGiantStep_SmallPrime_ReturnsValidLogarithm()
    {
        // 3^5 mod 17 = 5
        BigInteger a = 3;
        BigInteger p = 17;
        BigInteger y = 5;

        var x = DiscreteLog.BabyStepGiantStep(a, y, p);

        Assert.NotNull(x);
        Assert.Equal(y, DiscreteLog.ModPow(a, x!.Value, p));
    }

    [Fact]
    public void BabyStepGiantStep_WhenExponentIsZero_ReturnsZero()
    {
        BigInteger a = 5;
        BigInteger p = 23;
        BigInteger y = 1; // a^0 mod p

        var x = DiscreteLog.BabyStepGiantStep(a, y, p);

        Assert.NotNull(x);
        Assert.Equal(BigInteger.Zero, x!.Value);
    }

    [Fact]
    public void BabyStepGiantStep_WhenExponentIsOne_ReturnsOne()
    {
        BigInteger a = 7;
        BigInteger p = 29;
        BigInteger y = 7; // 7^1 mod 29

        var x = DiscreteLog.BabyStepGiantStep(a, y, p);

        Assert.NotNull(x);
        Assert.Equal(BigInteger.One, x!.Value);
    }

    [Fact]
    public void BabyStepGiantStep_LargerNumbers_ReturnsValidLogarithm()
    {
        BigInteger p = 104729;
        BigInteger a = 12345;
        BigInteger originalX = 50000;
        BigInteger y = DiscreteLog.ModPow(a, originalX, p);

        var foundX = DiscreteLog.BabyStepGiantStep(a, y, p);

        Assert.NotNull(foundX);
        Assert.Equal(y, DiscreteLog.ModPow(a, foundX!.Value, p));
    }

    [Theory]
    [InlineData(2, 3, 1)]
    [InlineData(2, 3, 0)]
    [InlineData(2, 3, -10)]
    public void BabyStepGiantStep_WhenPIsInvalid_ThrowsArgumentException(
        int a,
        int y,
        int p)
    {
        Assert.Throws<ArgumentException>(
            () => DiscreteLog.BabyStepGiantStep(a, y, p));
    }

    [Fact]
    public void BabyStepGiantStep_WhenAIsZeroAndYIsOne_ReturnsZero()
    {
        var x = DiscreteLog.BabyStepGiantStep(0, 1, 17);

        Assert.Equal(BigInteger.Zero, x);
    }

    [Fact]
    public void BabyStepGiantStep_WhenAIsZeroAndYIsZero_ReturnsOne()
    {
        var x = DiscreteLog.BabyStepGiantStep(0, 0, 17);

        Assert.Equal(BigInteger.One, x);
    }

    [Fact]
    public void BabyStepGiantStep_WhenAIsZeroAndYIsOther_ReturnsNull()
    {
        var x = DiscreteLog.BabyStepGiantStep(0, 5, 17);

        Assert.Null(x);
    }

    [Fact]
    public void BabyStepGiantStep_WhenYIsZeroAndAIsNonZero_ReturnsNull()
    {
        // Для простого p и ненулевого a a^x mod p никогда не равно 0.
        var x = DiscreteLog.BabyStepGiantStep(3, 0, 17);

        Assert.Null(x);
    }

    [Fact]
    public void BabyStepGiantStep_WhenYIsNotInGeneratedSubgroup_ReturnsNull()
    {
        // Порядок 4 mod 17 равен 4:
        // 4^0=1, 4^1=4, 4^2=16, 4^3=13, 4^4=1.
        // Значение 2 среди степеней 4 не встречается.
        BigInteger a = 4;
        BigInteger y = 2;
        BigInteger p = 17;

        var x = DiscreteLog.BabyStepGiantStep(a, y, p);

        Assert.Null(x);
    }

    [Fact]
    public void BabyStepGiantStep_ForManyInputs_ReturnsValueThatSatisfiesEquation()
    {
        BigInteger p = 101;
        BigInteger a = 2;

        for (BigInteger originalX = 0; originalX < p - 1; originalX++)
        {
            var y = DiscreteLog.ModPow(a, originalX, p);

            var foundX = DiscreteLog.BabyStepGiantStep(a, y, p);

            Assert.NotNull(foundX);
            Assert.Equal(y, DiscreteLog.ModPow(a, foundX!.Value, p));
        }
    }

    [Fact]
    public void BabyStepGiantStep_RandomizedInputs_ReturnsValidLogarithm()
    {
        var random = new Random(42);
        BigInteger p = 1009;

        for (var i = 0; i < 50; i++)
        {
            BigInteger a = random.Next(2, 1008);
            BigInteger originalX = random.Next(0, 1008);
            BigInteger y = DiscreteLog.ModPow(a, originalX, p);

            var foundX = DiscreteLog.BabyStepGiantStep(a, y, p);

            Assert.NotNull(foundX);
            Assert.Equal(y, DiscreteLog.ModPow(a, foundX!.Value, p));
        }
    }
}