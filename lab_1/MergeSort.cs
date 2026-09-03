namespace SortingAlgorithms
{
    public class MergeSort
    {
        public int[] SortAscending(int[] array)
        {
            if (array.Length <= 1)
            {
                return (int[])array.Clone();
            }

            int middle = array.Length / 2;

            int[] left = new int[middle];
            int[] right = new int[array.Length - middle];

            Array.Copy(array, 0, left, 0, middle);
            Array.Copy(array, middle, right, 0, right.Length);

            left = SortAscending(left);
            right = SortAscending(right);

            return MergeAscending(left, right);
        }

        private int[] MergeAscending(int[] left, int[] right)
        {
            int[] result = new int[left.Length + right.Length];

            int i = 0;
            int j = 0;
            int k = 0;

            while (i < left.Length && j < right.Length)
            {
                if (left[i] <= right[j])
                {
                    result[k] = left[i];
                    i++;
                }
                else
                {
                    result[k] = right[j];
                    j++;
                }

                k++;
            }

            while (i < left.Length)
            {
                result[k] = left[i];
                i++;
                k++;
            }

            while (j < right.Length)
            {
                result[k] = right[j];
                j++;
                k++;
            }

            return result;
        }

        public int[] SortDescending(int[] array)
        {
            if (array.Length <= 1)
            {
                return (int[])array.Clone();
            }

            int middle = array.Length / 2;

            int[] left = new int[middle];
            int[] right = new int[array.Length - middle];

            Array.Copy(array, 0, left, 0, middle);
            Array.Copy(array, middle, right, 0, right.Length);

            left = SortDescending(left);
            right = SortDescending(right);

            return MergeDescending(left, right);
        }

        private int[] MergeDescending(int[] left, int[] right)
        {
            int[] result = new int[left.Length + right.Length];

            int i = 0;
            int j = 0;
            int k = 0;

            while (i < left.Length && j < right.Length)
            {
                if (left[i] >= right[j])
                {
                    result[k] = left[i];
                    i++;
                }
                else
                {
                    result[k] = right[j];
                    j++;
                }

                k++;
            }

            while (i < left.Length)
            {
                result[k] = left[i];
                i++;
                k++;
            }

            while (j < right.Length)
            {
                result[k] = right[j];
                j++;
                k++;
            }

            return result;
        }
    }
}