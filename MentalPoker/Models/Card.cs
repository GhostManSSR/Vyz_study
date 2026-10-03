namespace MentalPoker.Models;

public class Card
{
    public int Id { get; }

    public string Name { get; }

    public Card(int id, string name)
    {
        Id = id;
        Name = name;
    }

    public override string ToString()
    {
        return Name;
    }
}