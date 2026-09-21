using System;
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using TaskManagerLab.Models;
using TaskModel = TaskManagerLab.Models.Task;

namespace TaskManagerLab.Forms
{
    public class MainForm : Form
    {
        private readonly TaskManager manager = new TaskManager();

        private ListBox projectList;
        private ListBox taskList;

        private TextBox titleInput;
        private TextBox descriptionInput;
        private TextBox searchInput;

        private ComboBox statusChoice;
        private ComboBox priorityChoice;
        private ComboBox statusFilter;
        private ComboBox priorityFilter;

        private Button addTaskButton;
        private Button updateTaskButton;
        private Button deleteTaskButton;
        private Button clearButton;
        private Button addProjectButton;
        private Button deleteProjectButton;

        private Label statistics;

        public MainForm()
        {
            Text = "Task Manager - C# Templates Lab";
            ClientSize = new Size(1050, 700);
            StartPosition = FormStartPosition.CenterScreen;
            FormBorderStyle = FormBorderStyle.FixedSingle;
            MaximizeBox = false;

            BuildUI();
            WireEvents();

            RefreshProjects();
            RefreshTasks();
        }

        // ---------- Построение интерфейса ----------
        private void BuildUI()
        {
            AddLabel("Projects", 20, 30, 210, 25);
            AddLabel("Tasks", 250, 30, 780, 25);
            AddLabel("Title", 250, 375, 280, 25);
            AddLabel("Description", 545, 375, 485, 25);
            AddLabel("Search", 250, 445, 250, 25);
            AddLabel("Status", 515, 445, 145, 25);
            AddLabel("Priority", 675, 445, 145, 25);
            AddLabel("Status filter", 250, 515, 180, 25);
            AddLabel("Priority filter", 445, 515, 180, 25);

            projectList = new ListBox { Left = 20, Top = 60, Width = 210, Height = 390 };
            taskList    = new ListBox { Left = 250, Top = 60, Width = 780, Height = 300 };
            Controls.Add(projectList);
            Controls.Add(taskList);

            titleInput       = new TextBox { Left = 250, Top = 400, Width = 280 };
            descriptionInput = new TextBox { Left = 545, Top = 400, Width = 485 };
            searchInput      = new TextBox { Left = 250, Top = 470, Width = 250 };
            Controls.Add(titleInput);
            Controls.Add(descriptionInput);
            Controls.Add(searchInput);

            statusChoice = new ComboBox
            {
                Left = 515, Top = 470, Width = 145,
                DropDownStyle = ComboBoxStyle.DropDownList
            };
            statusChoice.Items.AddRange(new object[] { "Todo", "In Progress", "Done" });
            statusChoice.SelectedIndex = 0;
            Controls.Add(statusChoice);

            priorityChoice = new ComboBox
            {
                Left = 675, Top = 470, Width = 145,
                DropDownStyle = ComboBoxStyle.DropDownList
            };
            priorityChoice.Items.AddRange(new object[] { "Low", "Medium", "High" });
            priorityChoice.SelectedIndex = 1;
            Controls.Add(priorityChoice);

            statusFilter = new ComboBox
            {
                Left = 250, Top = 540, Width = 180,
                DropDownStyle = ComboBoxStyle.DropDownList
            };
            statusFilter.Items.AddRange(new object[] { "All statuses", "Todo", "In Progress", "Done" });
            statusFilter.SelectedIndex = 0;
            Controls.Add(statusFilter);

            priorityFilter = new ComboBox
            {
                Left = 445, Top = 540, Width = 180,
                DropDownStyle = ComboBoxStyle.DropDownList
            };
            priorityFilter.Items.AddRange(new object[] { "All priorities", "Low", "Medium", "High" });
            priorityFilter.SelectedIndex = 0;
            Controls.Add(priorityFilter);

            addTaskButton        = MakeButton("Add",             840, 465, 140, 32);
            updateTaskButton     = MakeButton("Update",          840, 505, 140, 32);
            deleteTaskButton     = MakeButton("Delete",          840, 545, 140, 32);
            clearButton          = MakeButton("Clear",           650, 540, 160, 32);
            addProjectButton     = MakeButton("Add project",      20, 470, 210, 32);
            deleteProjectButton  = MakeButton("Delete project",   20, 510, 210, 32);

            statistics = new Label
            {
                Left = 20, Top = 600, Width = 1010, Height = 40,
                TextAlign = ContentAlignment.MiddleLeft,
                BorderStyle = BorderStyle.FixedSingle
            };
            Controls.Add(statistics);
        }

        private Label AddLabel(string text, int x, int y, int w, int h)
        {
            Label lbl = new Label
            {
                Text = text, Left = x, Top = y, Width = w, Height = h,
                TextAlign = ContentAlignment.MiddleLeft
            };
            Controls.Add(lbl);
            return lbl;
        }

        private Button MakeButton(string text, int x, int y, int w, int h)
        {
            Button btn = new Button { Text = text, Left = x, Top = y, Width = w, Height = h };
            Controls.Add(btn);
            return btn;
        }

        private void WireEvents()
        {
            projectList.SelectedIndexChanged += (s, e) => OnProjectSelected();
            taskList.SelectedIndexChanged    += (s, e) => LoadSelectedTask();

            addTaskButton.Click        += (s, e) => AddTaskFromForm();
            updateTaskButton.Click     += (s, e) => UpdateTaskFromForm();
            deleteTaskButton.Click     += (s, e) => DeleteSelectedTask();
            clearButton.Click          += (s, e) => ClearTaskFields();
            addProjectButton.Click     += (s, e) => AddProjectFromDialog();
            deleteProjectButton.Click  += (s, e) => DeleteSelectedProject();

            searchInput.TextChanged               += (s, e) => RefreshTasks();
            statusFilter.SelectedIndexChanged     += (s, e) => RefreshTasks();
            priorityFilter.SelectedIndexChanged   += (s, e) => RefreshTasks();
        }

        // ---------- Обновление списков ----------
        private void RefreshProjects()
        {
            int keepId = CurrentProjectId();

            projectList.Items.Clear();
            List<Project> projects = manager.GetProjects();
            foreach (Project p in projects)
            {
                projectList.Items.Add(p.Name);
            }

            // Восстановить выбор: сначала по сохранённому id, иначе — первый
            int restore = -1;
            for (int i = 0; i < projects.Count; i++)
            {
                if (projects[i].Id == keepId)
                {
                    restore = i;
                    break;
                }
            }
            if (restore < 0 && projectList.Items.Count > 0)
            {
                restore = 0;
            }
            if (restore >= 0)
            {
                projectList.SelectedIndex = restore;
            }
        }

        private void RefreshTasks()
        {
            taskList.Items.Clear();

            int projectId = CurrentProjectId();
            List<TaskModel> tasks = projectId < 0
                ? manager.GetTasks()                       // нет выбранного проекта — все задачи
                : manager.GetTasksByProject(projectId);    // только задачи выбранного проекта

            string search = searchInput.Text ?? "";
            string selStatus = statusFilter.SelectedItem != null
                               ? statusFilter.SelectedItem.ToString()
                               : "All statuses";
            string selPriority = priorityFilter.SelectedItem != null
                                 ? priorityFilter.SelectedItem.ToString()
                                 : "All priorities";

            int shown = 0;
            foreach (TaskModel t in tasks)
            {
                bool matchSearch = string.IsNullOrEmpty(search) ||
                    t.Title.IndexOf(search, StringComparison.OrdinalIgnoreCase) >= 0;
                bool matchStatus = selStatus == "All statuses" || t.Status == selStatus;
                bool matchPriority = selPriority == "All priorities" || t.Priority == selPriority;

                if (matchSearch && matchStatus && matchPriority)
                {
                    taskList.Items.Add(t.Id + " | " + t.Title + " | " + t.Priority + " | " + t.Status);
                    shown++;
                }
            }

            statistics.Text = "Tasks: " + tasks.Count +
                              "    Shown: " + shown +
                              "    Projects: " + manager.GetProjects().Count;
        }

        // Id проекта, соответствующего выделенной строке ListBox
        private int CurrentProjectId()
        {
            if (projectList.SelectedIndex < 0) return -1;
            List<Project> projects = manager.GetProjects();
            if (projectList.SelectedIndex >= projects.Count) return -1;
            return projects[projectList.SelectedIndex].Id;
        }

        // Id задачи, извлечённый из строки "id | title | priority | status"
        private int SelectedTaskId()
        {
            if (taskList.SelectedIndex < 0) return -1;

            string selected = taskList.SelectedItem != null
                              ? taskList.SelectedItem.ToString()
                              : "";

            int sep = selected.IndexOf('|');
            if (sep <= 0) return -1;

            int id;
            if (int.TryParse(selected.Substring(0, sep).Trim(), out id))
            {
                return id;
            }
            return -1;
        }

        // ---------- Работа с задачами ----------
        private void LoadSelectedTask()
        {
            int taskId = SelectedTaskId();
            if (taskId < 0) return;

            TaskModel t = manager.GetTaskById(taskId);
            if (t == null) return;

            titleInput.Text = t.Title;
            descriptionInput.Text = t.Description;

            switch (t.Status)
            {
                case "Todo":        statusChoice.SelectedIndex = 0; break;
                case "In Progress": statusChoice.SelectedIndex = 1; break;
                case "Done":        statusChoice.SelectedIndex = 2; break;
            }

            switch (t.Priority)
            {
                case "Low":    priorityChoice.SelectedIndex = 0; break;
                case "Medium": priorityChoice.SelectedIndex = 1; break;
                case "High":   priorityChoice.SelectedIndex = 2; break;
            }
        }

        private void ClearTaskFields()
        {
            titleInput.Text = "";
            descriptionInput.Text = "";
            statusChoice.SelectedIndex = 0;
            priorityChoice.SelectedIndex = 1;
            taskList.ClearSelected();
        }

        private void AddTaskFromForm()
        {
            int projectId = CurrentProjectId();
            if (projectId < 0)
            {
                MessageBox.Show("Сначала выберите проект.", "Warning",
                                MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }
            if (string.IsNullOrWhiteSpace(titleInput.Text))
            {
                MessageBox.Show("Enter a task title.", "Warning",
                                MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            manager.AddTask(projectId, titleInput.Text, descriptionInput.Text,
                            priorityChoice.SelectedItem.ToString(),
                            statusChoice.SelectedItem.ToString());

            RefreshTasks();
            ClearTaskFields();
        }

        private void UpdateTaskFromForm()
        {
            int taskId = SelectedTaskId();
            if (taskId < 0)
            {
                MessageBox.Show("Select a task first.", "Warning",
                                MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            manager.UpdateTaskById(taskId,
                titleInput.Text,
                descriptionInput.Text,
                priorityChoice.SelectedItem.ToString(),
                statusChoice.SelectedItem.ToString());

            RefreshTasks();
        }

        private void DeleteSelectedTask()
        {
            int taskId = SelectedTaskId();
            if (taskId < 0)
            {
                MessageBox.Show("Select a task first.", "Warning",
                                MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            manager.DeleteTaskById(taskId);
            RefreshTasks();
            ClearTaskFields();
        }

        // ---------- Работа с проектами ----------
        private void AddProjectFromDialog()
        {
            string name = Prompt("Project name:", "New project", "New project");
            if (!string.IsNullOrEmpty(name))
            {
                manager.AddProject(name);
                RefreshProjects();
                RefreshTasks();
            }
        }

        private void DeleteSelectedProject()
        {
            if (projectList.SelectedIndex < 0)
            {
                MessageBox.Show("Select a project first.", "Warning",
                                MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            DialogResult res = MessageBox.Show(
                "Удалить проект и все его задачи?", "Confirm",
                MessageBoxButtons.YesNo, MessageBoxIcon.Question);
            if (res != DialogResult.Yes) return;

            manager.DeleteProject(projectList.SelectedIndex);
            ClearTaskFields();
            RefreshProjects();
            RefreshTasks();
        }

        private void OnProjectSelected()
        {
            if (projectList.SelectedIndex < 0) return;

            string name = projectList.SelectedItem != null
                          ? projectList.SelectedItem.ToString()
                          : "";

            int projectId = CurrentProjectId();
            int taskCount = projectId < 0 ? 0 : manager.GetTasksByProject(projectId).Count;

            statistics.Text = "Selected project: " + name +
                              "    Tasks: " + taskCount +
                              "    Projects: " + manager.GetProjects().Count;

            ClearTaskFields();
            RefreshTasks();
        }

        // ---------- Простой диалог ввода ----------
        private static string Prompt(string text, string caption, string defaultValue)
        {
            using (Form dlg = new Form())
            {
                dlg.Text = caption;
                dlg.ClientSize = new Size(380, 110);
                dlg.FormBorderStyle = FormBorderStyle.FixedDialog;
                dlg.StartPosition = FormStartPosition.CenterParent;
                dlg.MinimizeBox = false;
                dlg.MaximizeBox = false;

                Label lbl = new Label { Left = 10, Top = 10, Width = 360, Text = text };
                TextBox tb = new TextBox { Left = 10, Top = 35, Width = 360, Text = defaultValue };
                Button ok = new Button
                {
                    Text = "OK", Left = 210, Top = 70, Width = 75,
                    DialogResult = DialogResult.OK
                };
                Button cancel = new Button
                {
                    Text = "Cancel", Left = 295, Top = 70, Width = 75,
                    DialogResult = DialogResult.Cancel
                };

                dlg.Controls.Add(lbl);
                dlg.Controls.Add(tb);
                dlg.Controls.Add(ok);
                dlg.Controls.Add(cancel);
                dlg.AcceptButton = ok;
                dlg.CancelButton = cancel;

                return dlg.ShowDialog() == DialogResult.OK ? tb.Text : null;
            }
        }
    }
}