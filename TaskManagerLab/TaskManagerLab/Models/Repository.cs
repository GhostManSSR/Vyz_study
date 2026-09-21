using System.Collections.Generic;

namespace TaskManagerLab.Models
{
    public class Repository<T>
    {
        private readonly List<T> items = new List<T>();

        public void Add(T item)
        {
            items.Add(item);
        }

        public void Remove(int index)
        {
            if (index >= 0 && index < items.Count)
            {
                items.RemoveAt(index);
            }
        }

        public void Update(int index, T item)
        {
            if (index >= 0 && index < items.Count)
            {
                items[index] = item;
            }
        }

        public int Size
        {
            get { return items.Count; }
        }

        public List<T> GetAll()
        {
            return new List<T>(items);
        }

        public void Clear()
        {
            items.Clear();
        }
    }
}