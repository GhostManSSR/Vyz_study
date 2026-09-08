namespace OrderCalc.Tests;

public class MatrixAndListTest
{
        [Fact]
        public void MaxPriceEvenValueEvenIndex_C0_FoundElement()
        {
            var prices = new List<double> { 2.0, 3.0, 4.0, 5.0, 6.0 };

            double result = MatrixAndListOperations.MaxPriceEvenValueEvenIndex(prices);
            Assert.Equal(6.0, result);
        }

        [Fact]
        public void MaxPriceEvenValueEvenIndex_C1_NullList_ThrowsException()
        {
            List<double> prices = null!;
            Assert.Throws<ArgumentNullException>(() => MatrixAndListOperations.MaxPriceEvenValueEvenIndex(prices));
        }

        [Fact]
        public void MaxPriceEvenValueEvenIndex_C1_EmptyList_ReturnsZero()
        {
            var prices = new List<double>();
            double result = MatrixAndListOperations.MaxPriceEvenValueEvenIndex(prices);
            Assert.Equal(0.0, result);
        }

        [Fact]
        public void MaxPriceEvenValueEvenIndex_C1_NoMatchingElements_ReturnsZero()
        {
            var prices = new List<double> { 1.0, 3.0, 5.0, 7.0 };

            double result = MatrixAndListOperations.MaxPriceEvenValueEvenIndex(prices);
            Assert.Equal(0.0, result);
        }

        [Fact]
        public void MaxPriceEvenValueEvenIndex_C2_UpdateMaximum()
        {
            var prices = new List<double> { 10.0, 1.0, 4.0, 3.0, 8.0 };

            double result = MatrixAndListOperations.MaxPriceEvenValueEvenIndex(prices);
            Assert.Equal(10.0, result);
        }

        [Fact]
        public void MaxPriceEvenValueEvenIndex_C2_FirstFoundThenUpdated()
        {
            var prices = new List<double> { 2.0, 1.0, 10.0, 3.0, 6.0 };
            double result = MatrixAndListOperations.MaxPriceEvenValueEvenIndex(prices);
            Assert.Equal(10.0, result);
        }

        [Fact]
        public void MaxPriceEvenValueEvenIndex_C2_OddIndicesIgnored()
        {
            var prices = new List<double> { 1.0, 100.0, 3.0, 200.0, 5.0 };

            double result = MatrixAndListOperations.MaxPriceEvenValueEvenIndex(prices);
            Assert.Equal(0.0, result);
        }

        [Fact]
        public void SumOddBelowMainDiagonal_C0_NormalMatrix()
        {
            var matrix = new int[][]
            {
                new int[] { 1, 2, 3 },
                new int[] { 5, 6, 7 },
                new int[] { 9, 10, 11 }
            };

            int result = MatrixAndListOperations.SumOddBelowMainDiagonal(matrix);
            Assert.Equal(14, result);
        }

        [Fact]
        public void SumOddBelowMainDiagonal_C1_NullMatrix_ReturnsZero()
        {
            int[][] matrix = null!;
            int result = MatrixAndListOperations.SumOddBelowMainDiagonal(matrix);
            Assert.Equal(0, result);
        }

        [Fact]
        public void SumOddBelowMainDiagonal_C1_EmptyMatrix_ReturnsZero()
        {
            var matrix = new int[0][];
            int result = MatrixAndListOperations.SumOddBelowMainDiagonal(matrix);
            Assert.Equal(0, result);
        }

        [Fact]
        public void SumOddBelowMainDiagonal_C1_NoOddElements_ReturnsZero()
        {
            var matrix = new int[][]
            {
                new int[] { 2, 4, 6 },
                new int[] { 8, 10, 12 },
                new int[] { 14, 16, 18 }
            };
            int result = MatrixAndListOperations.SumOddBelowMainDiagonal(matrix);
            Assert.Equal(0, result);
        }

        [Fact]
        public void SumOddBelowMainDiagonal_C2_AllOddBelowDiagonal()
        {
            var matrix = new int[][]
            {
                new int[] { 1, 2, 3 },
                new int[] { 3, 5, 6 },
                new int[] { 5, 7, 9 }
            };
            int result = MatrixAndListOperations.SumOddBelowMainDiagonal(matrix);
            Assert.Equal(15, result);
        }

        [Fact]
        public void SumOddBelowMainDiagonal_C2_MixedOddEven()
        {
            var matrix = new int[][]
            {
                new int[] { 1, 2, 3 },
                new int[] { 4, 6, 7 },
                new int[] { 7, 8, 10 }
            };
            int result = MatrixAndListOperations.SumOddBelowMainDiagonal(matrix);
            Assert.Equal(7, result);
        }

        [Fact]
        public void SumOddBelowMainDiagonal_C2_RectangularMatrix_MoreRows()
        {
            var matrix = new int[][]
            {
                new int[] { 1, 2 },
                new int[] { 3, 4 },
                new int[] { 5, 6 }
            };
            int result = MatrixAndListOperations.SumOddBelowMainDiagonal(matrix);
            Assert.Equal(8, result);
        }

        [Fact]
        public void SumOddBelowMainDiagonal_C2_RectangularMatrix_MoreCols()
        {
            var matrix = new int[][]
            {
                new int[] { 1, 2, 3, 4 },
                new int[] { 5, 6, 7, 8 }
            };
            int result = MatrixAndListOperations.SumOddBelowMainDiagonal(matrix);
            Assert.Equal(5, result);
        }

        [Fact]
        public void SumOddBelowMainDiagonal_C2_NullRow_Handled()
        {
            var matrix = new int[][]
            {
                new int[] { 1, 2, 3 },
                null,
                new int[] { 7, 8, 9 }
            };
            int result = MatrixAndListOperations.SumOddBelowMainDiagonal(matrix);
            Assert.Equal(7, result);
        }
}