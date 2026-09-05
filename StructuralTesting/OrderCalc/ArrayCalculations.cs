namespace OrderCalc;

public class ArrayCalculations
{
    public static void RotatePricesRight(double[] prices, int shift)
    {
        // условие 1: пустой массив или некорректный сдвиг
        if (prices == null || prices.Length == 0 || shift <= 0)
            return;

        // сдвиг больше длины — берём остаток
        shift = shift % prices.Length;

        // условие 2: если сдвиг кратен длине — без изменений
        if (shift == 0)
            return;

        // цикл 1: создаём копию
        double[] copy = new double[prices.Length];
        for (int i = 0; i < prices.Length; i++)
        {
            copy[i] = prices[i];
        }

        // цикл 2: переставляем элементы
        for (int i = 0; i < prices.Length; i++)
        {
            int newIndex = (i + shift) % prices.Length;
            prices[newIndex] = copy[i];
        }
    }
}