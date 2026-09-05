using Xunit;
using OrderCalc;

namespace OrderCalc.Tests;

public class MatrixCalculationsTests
{
    [Fact]
    public void SumEvenAboveSecondaryDiagonal_WithEvenElements_ReturnsSum()
    {
        int[][] matrix =
        {
            new[] { 2, 4, 6, 8 },
            new[] { 1, 3, 5, 7 },
            new[] { 0, 2, 4, 6 },
            new[] { 8, 6, 4, 2 }
        };

        int result = MatrixCalculations.SumEvenAboveSecondaryDiagonal(matrix);

        Assert.Equal(12, result);
    }

    [Fact]
    public void SumEvenAboveSecondaryDiagonal_NoEvenElements_ReturnsZero()
    {
        int[][] matrix =
        {
            new[] { 1, 3, 5 },
            new[] { 7, 9, 11 },
            new[] { 13, 15, 17 }
        };

        int result = MatrixCalculations.SumEvenAboveSecondaryDiagonal(matrix);

        Assert.Equal(0, result);
    }

    [Fact]
    public void SumEvenAboveSecondaryDiagonal_EmptyMatrix_ReturnsZero()
    {
        int[][] matrix = { };

        int result = MatrixCalculations.SumEvenAboveSecondaryDiagonal(matrix);

        Assert.Equal(0, result);
    }

    [Fact]
    public void SumEvenAboveSecondaryDiagonal_SingleElement_ReturnsZero()
    {
        int[][] matrix =
        {
            new[] { 5 }
        };

        int result = MatrixCalculations.SumEvenAboveSecondaryDiagonal(matrix);

        Assert.Equal(0, result);
    }
}