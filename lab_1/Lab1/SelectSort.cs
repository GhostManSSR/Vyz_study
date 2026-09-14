namespace SortingAlgorithms
{
    public class SelectSort
    {
        public int[] SortAscending(int[] array)
        {
            int[] result = (int[])array.Clone();

            for (int i = 0; i < result.Length - 1; i++)
            {
                int minIndex = i;

                for (int j = i + 1; j < result.Length; j++)
                {
                    if (result[j] < result[minIndex])
                    {
                        minIndex = j;
                    }
                }

                if (minIndex != i)
                {
                    int temp = result[i];
                    result[i] = result[minIndex];
                    result[minIndex] = temp;
                }
            }

            return result;
        }

        public int[] SortDescending(int[] array)
        {
            int[] result = (int[])array.Clone();

            for (int i = 0; i < result.Length - 1; i++)
            {
                int maxIndex = i;

                for (int j = i + 1; j < result.Length; j++)
                {
                    if (result[j] > result[maxIndex])
                    {
                        maxIndex = j;
                    }
                }

                if (maxIndex != i)
                {
                    int temp = result[i];
                    result[i] = result[maxIndex];
                    result[maxIndex] = temp;
                }
            }

            return result;
        }
    }
}