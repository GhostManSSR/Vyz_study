namespace OrderCalc;

public class ArrayCalculations
{
    public static double ProductOddIndices(double[] prices)
    {
        if (prices == null || prices.Length == 0)
            return 1.0;

        double product = 1.0;
        bool found = false;

        for (int i = 0; i < prices.Length; i++)
        {
            if (i % 2 != 0)
            {
                product *= prices[i];
                found = true;
            }
        }

        if (!found)
            return 1.0;

        return product;
    }
}