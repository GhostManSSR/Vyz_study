using System.Collections.Generic;
using System.Linq;
using TaskManagerLab;
using TaskManagerLab.Models;

public class TaskManager
{
    private readonly Repository<Task> _taskRepository = new();
    private readonly Repository<Project> _projectRepository = new();

    private int _nextTaskId = 1;
    private int _nextProjectId = 1;

    public Task AddTask(int projectId, string title, string? description = null,
                        string? priority = null, string? status = null)
    {
        ValidateProjectExists(projectId);

        var task = new Task.Builder(_nextTaskId++, title)
            .WithProjectId(projectId)
            .WithDescription(description ?? "")
            .WithPriority(priority ?? "Medium")
            .WithStatus(status ?? "Todo")
            .Build();

        _taskRepository.Add(task);
        return task;
    }

    public void UpdateTaskById(int taskId, string? title = null, string? description = null,
                               string? priority = null, string? status = null)
    {
        var task = GetTaskById(taskId) 
            ?? throw new TaskManagerException($"задача с id={taskId} не найдена");

        if (title != null) task.SetTitle(title);
        if (description != null) task.SetDescription(description);
        if (priority != null) task.SetPriority(priority);
        if (status != null) task.SetStatus(status);

        UpdateTaskInRepository(task);
    }

    public void DeleteTaskById(int taskId)
    {
        int index = IndexOfTask(taskId);
        if (index < 0)
            throw new TaskManagerException($"задача с id={taskId} не найдена");

        _taskRepository.Remove(index);
    }

    public Task? GetTaskById(int taskId)
    {
        int index = IndexOfTask(taskId);
        return index < 0 ? null : _taskRepository.Get(index);
    }

    public List<Task> GetTasks() => _taskRepository.GetAll();

    public List<Task> GetTasksByProject(int projectId) => _taskRepository.GetAll().Where(t => t.ProjectId == projectId).ToList();

    public bool MoveTaskToProject(int taskId, int projectId)
    {
        var task = GetTaskById(taskId);
        if (task == null)
            return false;

        if (projectId != 0 && !ProjectExists(projectId))
            return false;

        task.SetProjectId(projectId);
        UpdateTaskInRepository(task);
        return true;
    }

    public int CountTasksInProject(int projectId) =>
        _taskRepository.GetAll().Count(t => t.ProjectId == projectId);

    public int CountTasksInProjectByStatus(int projectId, string status) =>
        _taskRepository.GetAll()
            .Count(t => t.ProjectId == projectId && t.Status == status);

    public double CompletionPercent(int projectId)
    {
        int total = CountTasksInProject(projectId);
        if (total == 0) return 0.0;

        int done = CountTasksInProjectByStatus(projectId, "Done");
        return 100.0 * done / total;
    }

    public Project AddProject(string name)
    {
        var project = new Project(_nextProjectId++, name);
        _projectRepository.Add(project);
        return project;
    }

    public void DeleteProject(int projectId)
    {
        var project = GetProjectById(projectId)
            ?? throw new TaskManagerException($"проект с id={projectId} не найден");

        var tasksToDelete = _taskRepository.GetAll()
            .Where(t => t.ProjectId == project.Id)
            .ToList();

        foreach (var task in tasksToDelete)
        {
            int index = IndexOfTask(task.Id);
            if (index >= 0)
                _taskRepository.Remove(index);
        }

        int projectIndex = IndexOfProject(projectId);
        if (projectIndex >= 0)
            _projectRepository.Remove(projectIndex);
    }

    public List<Project> GetProjects() => _projectRepository.GetAll();

    public Project? GetProjectById(int projectId)
    {
        int index = IndexOfProject(projectId);
        return index < 0 ? null : _projectRepository.Get(index);
    }

    private int IndexOfTask(int taskId)
    {
        var all = _taskRepository.GetAll();
        for (int i = 0; i < all.Count; i++)
            if (all[i].Id == taskId)
                return i;
        return -1;
    }

    private int IndexOfProject(int projectId)
    {
        var all = _projectRepository.GetAll();
        for (int i = 0; i < all.Count; i++)
            if (all[i].Id == projectId)
                return i;
        return -1;
    }

    private bool ProjectExists(int projectId) =>
        _projectRepository.GetAll().Any(p => p.Id == projectId);

    private void ValidateProjectExists(int projectId)
    {
        if (projectId != 0 && !ProjectExists(projectId))
            throw new TaskManagerException($"проект с id={projectId} не существует");
    }

    private void UpdateTaskInRepository(Task task)
    {
        int index = IndexOfTask(task.Id);
        if (index >= 0)
            _taskRepository.Update(index, task);
    }
}