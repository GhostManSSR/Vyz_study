namespace TaskManagerLab.Tests;

public class TaskManagerTests
{
    [Fact]
    public void New_IsEmpty()
    {
        var m = new TaskManager();
        Assert.Empty(m.GetTasks());
        Assert.Empty(m.GetProjects());
    }

    [Fact]
    public void AddTask_LinksToProject_And_AssignsSequentialIds()
    {
        var m = new TaskManager();
        m.AddProject("P");
        int pid = m.GetProjects()[0].Id;

        m.AddTask(pid, "A", "d", "High", "Todo");
        m.AddTask(pid, "B", "", "Low", "Done");

        var tasks = m.GetTasks();
        Assert.Equal(2, tasks.Count);
        Assert.Equal(1, tasks[0].Id);
        Assert.Equal(2, tasks[1].Id);
        Assert.Equal(pid, tasks[0].ProjectId);
        Assert.Equal("High", tasks[0].Priority);
    }

    [Fact]
    public void AddTask_ToNonexistentProject_Throws()
    {
        var m = new TaskManager();
        Assert.Throws<TaskManagerException>(
            () => m.AddTask(999, "A", "", "Low", "Todo"));
    }

    [Fact]
    public void UpdateTaskById_ChangesFields_KeepsIds()
    {
        var m = new TaskManager();
        m.AddProject("P");
        int pid = m.GetProjects()[0].Id;
        m.AddTask(pid, "A", "d", "Low", "Todo");
        int id = m.GetTasks()[0].Id;

        m.UpdateTaskById(id, "B", "d2", "High", "Done");

        var t = m.GetTasks()[0];
        Assert.Equal(id, t.Id);
        Assert.Equal(pid, t.ProjectId);
        Assert.Equal("B", t.Title);
        Assert.Equal("d2", t.Description);
        Assert.Equal("High", t.Priority);
        Assert.Equal("Done", t.Status);
    }

    [Fact]
    public void UpdateTaskById_UnknownId_Throws()
    {
        var m = new TaskManager();
        Assert.Throws<TaskManagerException>(
            () => m.UpdateTaskById(999, "B", "", "Low", "Todo"));
    }

    [Fact]
    public void DeleteTaskById_RemovesOne()
    {
        var m = new TaskManager();
        m.AddTask(0, "A", "", "Low", "Todo");
        m.AddTask(0, "B", "", "Low", "Todo");
        int idA = m.GetTasks()[0].Id;

        m.DeleteTaskById(idA);

        Assert.Single(m.GetTasks());
        Assert.Equal("B", m.GetTasks()[0].Title);
    }

    [Fact]
    public void DeleteTaskById_UnknownId_Throws()
    {
        var m = new TaskManager();
        Assert.Throws<TaskManagerException>(() => m.DeleteTaskById(999));
    }

    [Fact]
    public void GetTasksByProject_Filters_And_ReturnsEmptyForUnknown()
    {
        var m = new TaskManager();
        m.AddProject("P1");
        m.AddProject("P2");
        int p1 = m.GetProjects()[0].Id;
        int p2 = m.GetProjects()[1].Id;

        m.AddTask(p1, "A", "", "Low", "Todo");
        m.AddTask(p1, "B", "", "Low", "Todo");
        m.AddTask(p2, "C", "", "Low", "Todo");
        m.AddTask(0,  "D", "", "Low", "Todo");

        Assert.Equal(2, m.GetTasksByProject(p1).Count);
        Assert.Equal(1, m.GetTasksByProject(p2).Count);
        Assert.Empty(m.GetTasksByProject(999));  
    }

    [Fact]
    public void CompletionPercent_Boundaries()
    {
        var m = new TaskManager();
        m.AddProject("P");
        int pid = m.GetProjects()[0].Id;

        Assert.Equal(0.0, m.CompletionPercent(pid));

        m.AddTask(pid, "A", "", "Low", "Done");
        m.AddTask(pid, "B", "", "Low", "Todo");
        Assert.Equal(50.0, m.CompletionPercent(pid));

        int idB = m.GetTasks()[1].Id;
        m.UpdateTaskById(idB, "B", "", "Low", "Done");
        Assert.Equal(100.0, m.CompletionPercent(pid));
    }

    [Fact]
    public void DeleteProject_CascadesToItsTasks_Only()
    {
        var m = new TaskManager();
        m.AddProject("P1");
        m.AddProject("P2");
        int p1 = m.GetProjects()[0].Id;
        int p2 = m.GetProjects()[1].Id;

        m.AddTask(p1, "A", "", "Low", "Todo");
        m.AddTask(p1, "B", "", "Low", "Todo");
        m.AddTask(p2, "C", "", "Low", "Todo");
        m.AddTask(0,  "D", "", "Low", "Todo");

        m.DeleteProject(p1);    

        var tasks = m.GetTasks();
        Assert.Equal(2, tasks.Count);
        Assert.Equal(p2, tasks[0].ProjectId);  
        Assert.Equal(0,  tasks[1].ProjectId);   
        Assert.Single(m.GetProjects());
        Assert.Equal("P2", m.GetProjects()[0].Name);
    }

    [Fact]
    public void DeleteProject_BadId_Throws()
    {
        var m = new TaskManager();
        Assert.Throws<TaskManagerException>(() => m.DeleteProject(999));
    }
}