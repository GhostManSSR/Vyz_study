namespace TaskManagerLab.Models
{
    public class Task
    {
        private int id;
        private string title;
        private string description;
        private string status;
        private string priority;
        private int projectId;   

        public Task(int id, string title)
        {
            this.id = id;
            this.title = title;
            this.description = string.Empty;
            this.status = "Todo";
            this.priority = "Medium";
            this.projectId = 0;
        }

        public int Id { get { return id; } }

        public int ProjectId
        {
            get { return projectId; }
            set { projectId = value; }
        }
     
        public string Title
        {
            get { return title; }
            set { title = value; }
        }

        public string Description
        {
            get { return description; }
            set { description = value; }
        }

        public string Status
        {
            get { return status; }
            set
            {
                if (value == "Todo" || value == "In Progress" || value == "Done")
                {
                    status = value;
                }
            }
        }

        public string Priority
        {
            get { return priority; }
            set
            {
                if (value == "Low" || value == "Medium" || value == "High")
                {
                    priority = value;
                }
            }
        }
    }
}