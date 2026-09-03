namespace SortingAlgorithms
{
    public class BubbleSort
    {
        public int[] SortAscending(int[] array)
        {
            int[] result = (int[])array.Clone();

            for (int i = 0; i < result.Length - 1; i++)
            {
                bool swapped = false;

                for (int j = 0; j < result.Length - 1 - i; j++)
                {
                    if (result[j] > result[j + 1])
                    {
                        int temp = result[j];
                        result[j] = result[j + 1];
                        result[j + 1] = temp;

                        swapped = true;
                    }
                }

                if (!swapped)
                {
                    break;
                }
            }

            return result;
        }

        public int[] SortDescending(int[] array)
        {
            int[] result = (int[])array.Clone();

            for (int i = 0; i < result.Length - 1; i++)
            {
                bool swapped = false;

                for (int j = 0; j < result.Length - 1 - i; j++)
                {
                    if (result[j] < result[j + 1])
                    {
                        int temp = result[j];
                        result[j] = result[j + 1];
                        result[j + 1] = temp;

                        swapped = true;
                    }
                }

                if (!swapped)
                {
                    break;
                }
            }

            return result;
        }
    }
}