namespace OrderCalc;

public class ArrayCalculations
{
    public static void RotatePricesRight(double[] prices, int shift)
    {
        if (prices == null || prices.Length == 0 || shift <= 0)
            return;

        shift = shift % prices.Length;

        if (shift == 0)
            return;

        double[] copy = new double[prices.Length];
        for (int i = 0; i < prices.Length; i++)
        {
            copy[i] = prices[i];
        }

        for (int i = 0; i < prices.Length; i++)
        {
            int newIndex = (i + shift) % prices.Length;
            prices[newIndex] = copy[i];
        }
    }
}