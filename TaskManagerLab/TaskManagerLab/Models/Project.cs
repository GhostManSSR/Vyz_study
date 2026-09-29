namespace TaskManagerLab.Models;

public class Project
{
    public int Id { get; }
    public string Name { get; private set; }

    public Project(int id, string name)
    {
        Id = id;
        Name = name;
    }

    public void SetName(string name) => Name = name;

    public override string ToString() => $"[{Id}] {Name}";
}