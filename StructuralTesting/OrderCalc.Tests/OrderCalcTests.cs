using OrderCalc;
using Xunit;

namespace OrderCalc.Tests;
public class ArrayCalculationsTests
{
    [Fact] 
    public void ProductOddIndices_WithOddIndices_ReturnsProduct()
    {
        double[] prices = { 1.5, 2.0, 3.5, 4.0 };
        double expected = 2.0 * 4.0;
        
        double result = ArrayCalculations.ProductOddIndices(prices);
        
        Assert.Equal(expected, result, 6);
    }

    [Fact] 
    public void ProductOddIndices_EmptyArray_ReturnsOne()
    {
        double[] prices = { };
        
        double result = ArrayCalculations.ProductOddIndices(prices);
        
        Assert.Equal(1.0, result);
    }

    [Fact] // T3: путь B
    public void ProductOddIndices_NoOddIndices_ReturnsOne()
    {
        double[] prices = { 5.0 };
        
        double result = ArrayCalculations.ProductOddIndices(prices);
        
        Assert.Equal(1.0, result);
    }

    [Fact] // T4: путь D
    public void ProductOddIndices_NullArray_ReturnsOne()
    {
        double[] prices = null!;
        
        double result = ArrayCalculations.ProductOddIndices(prices);
        
        Assert.Equal(1.0, result);
    }
}

public class MatrixCalculationsTests
{
    [Fact]
    public void SumOddAboveMainDiagonal_WithOddElements_ReturnsSum()
    {
        int[][] matrix = 
        {
            new[] { 1, 3, 5 },
            new[] { 2, 4, 7 },
            new[] { 6, 8, 9 }
        };
        // Выше главной диагонали: [0][1]=3, [0][2]=5, [1][2]=7
        // Нечётные: 3 + 5 + 7 = 15
        
        int result = MatrixCalculations.SumOddAboveMainDiagonal(matrix);
        
        Assert.Equal(15, result);
    }

    [Fact]
    public void SumOddAboveMainDiagonal_NoOddElements_ReturnsZero()
    {
        int[][] matrix = 
        {
            new[] { 2, 4, 6 },
            new[] { 8, 10, 12 },
            new[] { 14, 16, 18 }
        };
        
        int result = MatrixCalculations.SumOddAboveMainDiagonal(matrix);
        
        Assert.Equal(0, result);
    }

    [Fact]
    public void SumOddAboveMainDiagonal_EmptyMatrix_ReturnsZero()
    {
        int[][] matrix = { };
        
        int result = MatrixCalculations.SumOddAboveMainDiagonal(matrix);
        
        Assert.Equal(0, result);
    }
}
