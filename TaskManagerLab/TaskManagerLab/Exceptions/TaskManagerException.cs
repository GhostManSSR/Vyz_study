using System;

namespace TaskManagerLab;

public class TaskManagerException : Exception
{
    public TaskManagerException(string message) : base(message) { }
}