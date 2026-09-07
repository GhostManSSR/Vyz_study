using OrderCalc;
using Xunit;

namespace OrderCalc.Tests;

public class ArrayCalculationsTests
{
    [Fact]
    public void RotatePricesRight_EmptyArray_DoesNothing()
    {
        double[] prices = { };
        double[] expected = { };

        ArrayCalculations.RotatePricesRight(prices, 2);

        Assert.Equal(expected, prices);
    }

    [Fact]
    public void RotatePricesRight_NullArray_DoesNothing()
    {
        double[] prices = null!;

        ArrayCalculations.RotatePricesRight(prices, 1);

        Assert.Null(prices);
    }

    [Fact]
    public void RotatePricesRight_NonPositiveShift_DoesNothing()
    {
        double[] prices = { 1.0, 2.0, 3.0 };
        double[] expected = { 1.0, 2.0, 3.0 };

        ArrayCalculations.RotatePricesRight(prices, 0);

        Assert.Equal(expected, prices);
    }

    [Fact]
    public void RotatePricesRight_ShiftMultipleOfLength_DoesNothing()
    {
        double[] prices = { 1.0, 2.0, 3.0 };
        double[] expected = { 1.0, 2.0, 3.0 };

        ArrayCalculations.RotatePricesRight(prices, 3);

        Assert.Equal(expected, prices);
    }

    [Fact]
    public void RotatePricesRight_ValidShift_RotatesArray()
    {
        double[] prices = { 1.0, 2.0, 3.0, 4.0 };
        double[] expected = { 3.0, 4.0, 1.0, 2.0 };

        ArrayCalculations.RotatePricesRight(prices, 2);

        Assert.Equal(expected, prices);
    }
}