using System.Collections.Generic;

namespace TaskManagerLab.Models;

public class Repository<T>
{
    private readonly List<T> _items = new();

    public int Count => _items.Count;

    public void Add(T item) => _items.Add(item);

    public void Update(int index, T item) => _items[index] = item;

    public void Remove(int index) => _items.RemoveAt(index);

    public List<T> GetAll() => new(_items);

    public T Get(int index) => _items[index];
}