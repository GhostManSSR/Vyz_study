using TaskManagerLab;
using TaskManagerLab.Models;
using Xunit;
using TaskModel = TaskManagerLab.Models.Task;

namespace TaskManagerLab.Tests;

public class TaskTests
{
    [Fact]
    public void Builder_SetsDefaults_And_ExplicitProjectId()
    {
        var t = new TaskModel.Builder(1, "A").Build();
        Assert.Equal(1, t.Id);
        Assert.Equal(0, t.ProjectId);
        Assert.Equal("A", t.Title);
        Assert.Equal("", t.Description);
        Assert.Equal("Todo", t.Status);
        Assert.Equal("Medium", t.Priority);

        var t2 = new TaskModel.Builder(2, "B")
            .WithProjectId(42)
            .Build();
        Assert.Equal(42, t2.ProjectId);
    }

    [Fact]
    public void Builder_WithStatus_Valid_Changes()
    {
        var t = new TaskModel.Builder(1, "A")
            .WithStatus("Done")
            .Build();
        Assert.Equal("Done", t.Status);
    }

    [Fact]
    public void Builder_WithStatus_Invalid_Ignored()
    {
        var t = new TaskModel.Builder(1, "A")
            .WithStatus("Done")
            .WithStatus("ЗАВТРА")           
            .Build();
        Assert.Equal("Done", t.Status);
    }

    [Fact]
    public void Builder_WithPriority_Valid_And_Invalid()
    {
        var t = new TaskModel.Builder(1, "A")
            .WithPriority("High")
            .Build();
        Assert.Equal("High", t.Priority);

        var t2 = new TaskModel.Builder(2, "B")
            .WithPriority("High")
            .WithPriority("Очень высокий")  
            .Build();
        Assert.Equal("High", t2.Priority);
    }

    [Fact]
    public void SetStatus_Valid_Changes()
    {
        var t = new TaskModel.Builder(1, "A").Build();
        t.SetStatus("Done");
        Assert.Equal("Done", t.Status);
    }

    [Fact]
    public void SetStatus_Invalid_Ignored()
    {
        var t = new TaskModel.Builder(1, "A").Build();
        t.SetStatus("Done");
        t.SetStatus("ЗАВТРА");           
        Assert.Equal("Done", t.Status);
    }

    [Fact]
    public void SetPriority_Valid_And_Invalid()
    {
        var t = new TaskModel.Builder(1, "A").Build();

        t.SetPriority("High");
        Assert.Equal("High", t.Priority);

        t.SetPriority("Очень высокий");  
        Assert.Equal("High", t.Priority);
    }
}