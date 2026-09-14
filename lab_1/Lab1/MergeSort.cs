namespace SortingAlgorithms
{
    public class MergeSort
    {
        public int[] SortAscending(int[] array)
        {
            int[] result = (int[])array.Clone();

            for (int width = 1; width < result.Length; width *= 2)
            {
                for (int left = 0; left < result.Length; left += 2 * width)
                {
                    int middle = Math.Min(left + width, result.Length);
                    int right = Math.Min(left + 2 * width, result.Length);

                    MergeAscending(result, left, middle, right);
                }
            }

            return result;
        }

        private void MergeAscending(int[] array, int left, int middle, int right)
        {
            int[] temp = new int[right - left];

            int i = left;
            int j = middle;
            int k = 0;

            while (i < middle && j < right)
            {
                if (array[i] <= array[j])
                {
                    temp[k] = array[i];
                    i++;
                }
                else
                {
                    temp[k] = array[j];
                    j++;
                }

                k++;
            }

            while (i < middle)
            {
                temp[k] = array[i];
                i++;
                k++;
            }

            while (j < right)
            {
                temp[k] = array[j];
                j++;
                k++;
            }

            Array.Copy(temp, 0, array, left, temp.Length);
        }


        public int[] SortDescending(int[] array)
        {
            int[] result = (int[])array.Clone();

            for (int width = 1; width < result.Length; width *= 2)
            {
                for (int left = 0; left < result.Length; left += 2 * width)
                {
                    int middle = Math.Min(left + width, result.Length);
                    int right = Math.Min(left + 2 * width, result.Length);

                    MergeDescending(result, left, middle, right);
                }
            }

            return result;
        }

        private void MergeDescending(int[] array, int left, int middle, int right)
        {
            int[] temp = new int[right - left];

            int i = left;
            int j = middle;
            int k = 0;

            while (i < middle && j < right)
            {
                if (array[i] >= array[j])
                {
                    temp[k] = array[i];
                    i++;
                }
                else
                {
                    temp[k] = array[j];
                    j++;
                }

                k++;
            }

            while (i < middle)
            {
                temp[k] = array[i];
                i++;
                k++;
            }

            while (j < right)
            {
                temp[k] = array[j];
                j++;
                k++;
            }

            Array.Copy(temp, 0, array, left, temp.Length);
        }
    }
}