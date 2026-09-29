using System.Linq;
namespace TaskManagerLab.Models;

public class Task
{
    private static readonly string[] AllowedStatuses = { "Todo", "In Progress", "Done" };
    private static readonly string[] AllowedPriorities = { "Low", "Medium", "High" };

    public int Id { get; }
    public int ProjectId { get; private set; }
    public string Title { get; private set; }
    public string Description { get; private set; }
    public string Status { get; private set; }
    public string Priority { get; private set; }

    private Task(int id, string title, int projectId, string description,
                 string status, string priority)
    {
        Id = id;
        Title = title;
        ProjectId = projectId;
        Description = description;
        Status = status;
        Priority = priority;
    }

    public void SetProjectId(int projectId) => ProjectId = projectId;
    public void SetTitle(string title) => Title = title;
    public void SetDescription(string description) => Description = description;

    public void SetStatus(string status)
    {
        if (AllowedStatuses.Contains(status))
            Status = status;
    }

    public void SetPriority(string priority)
    {
        if (AllowedPriorities.Contains(priority))
            Priority = priority;
    }

    public override string ToString() =>
        $"[{Id}] {Title} (project={ProjectId}, status={Status}, priority={Priority})";

    public class Builder
    {
        private readonly int _id;
        private readonly string _title;
        private int _projectId;
        private string _description = "";
        private string _status = "Todo";
        private string _priority = "Medium";

        public Builder(int id, string title)
        {
            _id = id;
            _title = title;
        }

        public Builder WithProjectId(int projectId)
        {
            _projectId = projectId;
            return this;
        }

        public Builder WithDescription(string description)
        {
            _description = description;
            return this;
        }

        public Builder WithStatus(string status)
        {
            if (AllowedStatuses.Contains(status))
                _status = status;
            return this;
        }

        public Builder WithPriority(string priority)
        {
            if (AllowedPriorities.Contains(priority))
                _priority = priority;
            return this;
        }

        public Task Build() => new Task(_id, _title, _projectId, _description, _status, _priority);
    }
}
