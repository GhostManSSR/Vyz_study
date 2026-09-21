using System.Collections.Generic;

namespace TaskManagerLab.Models
{
    public class TaskManager
    {
        private readonly Repository<Task> taskRepository = new Repository<Task>();
        private readonly Repository<Project> projectRepository = new Repository<Project>();

        private int nextTaskId;
        private int nextProjectId;

        public TaskManager()
        {
            nextTaskId = 1;
            nextProjectId = 1;

            // Демонстрационные данные: два проекта и по задаче в каждом
            AddProject("Учёба");   // Id = 1
            AddProject("Работа");  // Id = 2

            AddTask(1, "Лабораторная работа", "Реализовать на C#", "High", "In Progress");
            AddTask(2, "Купить продукты",     "Молоко, хлеб",      "Low",  "Todo");
        }

        // ---------- Задачи ----------
        public void AddTask(int projectId, string title, string description,
                            string priority, string status)
        {
            Task task = new Task(nextTaskId, title);
            nextTaskId++;

            task.ProjectId = projectId;
            task.Description = description;
            task.Priority = priority;
            task.Status = status;

            taskRepository.Add(task);
        }

        public void UpdateTaskById(int taskId, string title, string description,
                                   string priority, string status)
        {
            List<Task> tasks = taskRepository.GetAll();
            for (int i = 0; i < tasks.Count; i++)
            {
                if (tasks[i].Id == taskId)
                {
                    Task t = tasks[i];
                    t.Title = title;
                    t.Description = description;
                    t.Priority = priority;
                    t.Status = status;
                    taskRepository.Update(i, t);
                    return;
                }
            }
        }

        public void DeleteTaskById(int taskId)
        {
            List<Task> tasks = taskRepository.GetAll();
            for (int i = 0; i < tasks.Count; i++)
            {
                if (tasks[i].Id == taskId)
                {
                    taskRepository.Remove(i);
                    return;
                }
            }
        }

        public List<Task> GetTasks()
        {
            return taskRepository.GetAll();
        }

        public List<Task> GetTasksByProject(int projectId)
        {
            List<Task> result = new List<Task>();
            foreach (Task t in taskRepository.GetAll())
            {
                if (t.ProjectId == projectId)
                {
                    result.Add(t);
                }
            }
            return result;
        }

        public Task GetTaskById(int taskId)
        {
            foreach (Task t in taskRepository.GetAll())
            {
                if (t.Id == taskId)
                {
                    return t;
                }
            }
            return null;
        }

        // ---------- Проекты ----------
        public void AddProject(string name)
        {
            Project project = new Project(nextProjectId, name);
            nextProjectId++;
            projectRepository.Add(project);
        }

        public void DeleteProject(int index)
        {
            // Каскадное удаление: сначала убираем все задачи этого проекта
            List<Project> projects = projectRepository.GetAll();
            if (index >= 0 && index < projects.Count)
            {
                int projectId = projects[index].Id;

                List<Task> tasks = taskRepository.GetAll();
                for (int i = tasks.Count - 1; i >= 0; i--)
                {
                    if (tasks[i].ProjectId == projectId)
                    {
                        taskRepository.Remove(i);
                    }
                }

                projectRepository.Remove(index);
            }
        }

        public List<Project> GetProjects()
        {
            return projectRepository.GetAll();
        }
    }
}