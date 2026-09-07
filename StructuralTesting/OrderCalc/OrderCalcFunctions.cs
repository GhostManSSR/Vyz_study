using System;

namespace OrderCalc;

public static class OrderCalcFunctions
{
    public static double ApplyDiscount(double total, bool isPremium)
    {
        if (total < 0)
            return -1.0;

        if (total >= 10000)
        {
            return isPremium ? total * 0.80 : total * 0.85;
        }
        else if (total >= 5000)
        {
            return isPremium ? total * 0.90 : total * 0.95;
        }

        return total;
    }

    public static double CalcShipping(double total)
    {
        if (total < 0)
            return -1.0;

        if (total <= 5000)
            return 300.0;

        return 0.0;
    }

    public static double FinalPrice(double total, double discount, double shipping)
    {
        if (total < 0 || discount < 0 || shipping < 0)
            return -1.0;

        double result = total - discount + shipping;

        if (result < 0)
            result = 0.0;

        return Math.Round(result, 2);
    }

    public static int CountExpensiveItems(double[] prices, double threshold)
    {
        if (prices == null || prices.Length == 0)
            return 0;

        int count = 0;
        for (int i = 0; i < prices.Length; i++)
        {
            if (prices[i] > threshold)
            {
                count++;
            }
        }

        return count;
    }
}