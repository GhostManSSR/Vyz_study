using TaskManagerLab.Models;

using TaskInManager = TaskManagerLab.Models.Task;
namespace TaskMangerLabTest;

public class TaskManagerTest
{
    private readonly TaskManager _manager;
    
    public TaskManagerTest()
    {
        _manager = new TaskManager();
    }
    
    // ---------- Проекты ----------

    [Fact]
    public void Constructor_CreatesDefaultProjectsAndTasks()
    {
        Assert.Equal(2, _manager.GetProjects().Count);
        Assert.Equal(2, _manager.GetTasks().Count);
    }

    [Fact]
    public void AddProject_AddsNewProject()
    {
        _manager.AddProject("test");

        List<Project> projects = _manager.GetProjects();

        Assert.Equal(3, projects.Count);
        Assert.Contains(projects, p => p.Name == "test");
    }

    [Fact]
    public void AddProject_AssignsIncrementingId()
    {
        _manager.AddProject("test");

        Project added = _manager.GetProjects().Last();

        Assert.Equal(3, added.Id);
    }

    [Fact]
    public void DeleteProject_RemovesProject()
    {
        _manager.DeleteProject(0);

        List<Project> projects = _manager.GetProjects();

        Assert.Equal(1, projects.Count);
        Assert.DoesNotContain(projects, p => p.Id == 1);
    }

    [Fact]
    public void DeleteProject_DeletesTasksOfProject()
    {
        _manager.AddTask(1, "Новая задача", "Описание", "High", "Todo");

        _manager.DeleteProject(0);

        List<TaskInManager> tasks = _manager.GetTasks();

        Assert.DoesNotContain(tasks, t => t.ProjectId == 1);
    }

    [Fact]
    public void DeleteProject_InvalidIndex_DoesNothing()
    {
        _manager.DeleteProject(100);

        Assert.Equal(2, _manager.GetProjects().Count);
    }

    // ---------- Задачи ----------

    [Fact]
    public void AddTask_AddsTask()
    {
        _manager.AddTask(1, "Новая задача", "Описание", "High", "Todo");

        TaskInManager task = _manager.GetTaskById(3);

        Assert.NotNull(task);
        Assert.Equal("Новая задача", task.Title);
        Assert.Equal(1, task.ProjectId);
        Assert.Equal("High", task.Priority);
        Assert.Equal("Todo", task.Status);
    }

    [Fact]
    public void GetTasksByProject_ReturnsOnlyProjectTasks()
    {
        List<TaskInManager> tasks = _manager.GetTasksByProject(1);

        Assert.Single(tasks);
        Assert.All(tasks, t => Assert.Equal(1, t.ProjectId));
    }

    [Fact]
    public void GetTaskById_ReturnsExistingTask()
    {
        TaskInManager task = _manager.GetTaskById(1);

        Assert.NotNull(task);
        Assert.Equal("Лабораторная работа", task.Title);
    }

    [Fact]
    public void GetTaskById_ReturnsNullForMissingTask()
    {
        TaskInManager task = _manager.GetTaskById(999);

        Assert.Null(task);
    }

    [Fact]
    public void UpdateTaskById_ChangesTaskFields()
    {
        _manager.UpdateTaskById(1, "Обновлённая задача", "Новое описание", "Low", "Done");

        TaskInManager task = _manager.GetTaskById(1);

        Assert.Equal("Обновлённая задача", task.Title);
        Assert.Equal("Новое описание", task.Description);
        Assert.Equal("Low", task.Priority);
        Assert.Equal("Done", task.Status);
    }

    [Fact]
    public void UpdateTaskById_InvalidId_DoesNothing()
    {
        _manager.UpdateTaskById(999, "Тест", "Тест", "Low", "Done");

        Assert.Equal(2, _manager.GetTasks().Count);
    }

    [Fact]
    public void DeleteTaskById_RemovesTask()
    {
        _manager.DeleteTaskById(1);

        Assert.Null(_manager.GetTaskById(1));
        Assert.Equal(1, _manager.GetTasks().Count);
    }

    [Fact]
    public void DeleteTaskById_InvalidId_DoesNothing()
    {
        _manager.DeleteTaskById(999);

        Assert.Equal(2, _manager.GetTasks().Count);
    }
}
