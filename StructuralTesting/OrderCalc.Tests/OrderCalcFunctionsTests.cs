using Xunit;
using OrderCalc;

namespace OrderCalc.Tests;

public class OrderCalcFunctionsTests
{
    [Fact]
    public void ApplyDiscount_NegativeTotal_ReturnsError()
    {
        double result = OrderCalcFunctions.ApplyDiscount(-1.0, false);
        Assert.Equal(-1.0, result);
    }

    [Fact]
    public void ApplyDiscount_TotalBelow5000_NoDiscount()
    {
        double result = OrderCalcFunctions.ApplyDiscount(3000.0, true);
        Assert.Equal(3000.0, result);
    }

    [Fact]
    public void ApplyDiscount_Total5000_Regular_5PercentDiscount()
    {
        double result = OrderCalcFunctions.ApplyDiscount(6000.0, false);
        Assert.Equal(5700.0, result);
    }

    [Fact]
    public void ApplyDiscount_Total5000_Premium_10PercentDiscount()
    {
        double result = OrderCalcFunctions.ApplyDiscount(6000.0, true);
        Assert.Equal(5400.0, result);
    }

    [Fact]
    public void ApplyDiscount_Total10000_Regular_15PercentDiscount()
    {
        double result = OrderCalcFunctions.ApplyDiscount(12000.0, false);
        Assert.Equal(10200.0, result);
    }

    [Fact]
    public void ApplyDiscount_Total10000_Premium_20PercentDiscount()
    {
        double result = OrderCalcFunctions.ApplyDiscount(12000.0, true);
        Assert.Equal(9600.0, result);
    }

    [Fact]
    public void CalcShipping_NegativeTotal_ReturnsError()
    {
        double result = OrderCalcFunctions.CalcShipping(-5.0);
        Assert.Equal(-1.0, result);
    }

    [Fact]
    public void CalcShipping_TotalUpTo5000_ShippingIs300()
    {
        double result = OrderCalcFunctions.CalcShipping(1000.0);
        Assert.Equal(300.0, result);
    }

    [Fact]
    public void CalcShipping_TotalAbove5000_FreeShipping()
    {
        double result = OrderCalcFunctions.CalcShipping(8000.0);
        Assert.Equal(0.0, result);
    }

    [Fact]
    public void FinalPrice_NegativeArgument_ReturnsError()
    {
        double result = OrderCalcFunctions.FinalPrice(-1.0, 0.0, 0.0);
        Assert.Equal(-1.0, result);
    }

    [Fact]
    public void FinalPrice_ResultNegative_ClampedToZero()
    {
        double result = OrderCalcFunctions.FinalPrice(500.0, 700.0, 0.0);
        Assert.Equal(0.0, result);
    }

    [Fact]
    public void FinalPrice_NormalCalculation_ReturnsRounded()
    {
        double result = OrderCalcFunctions.FinalPrice(1000.0, 200.0, 50.0);
        Assert.Equal(850.00, result);
    }

    [Fact]
    public void CountExpensiveItems_EmptyVector_ReturnsZero()
    {
        double[] prices = { };
        int result = OrderCalcFunctions.CountExpensiveItems(prices, 100.0);
        Assert.Equal(0, result);
    }

    [Fact]
    public void CountExpensiveItems_ItemsAboveThreshold_Counted()
    {
        double[] prices = { 50.0, 150.0, 200.0 };
        int result = OrderCalcFunctions.CountExpensiveItems(prices, 100.0);
        Assert.Equal(2, result);
    }
}