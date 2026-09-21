namespace TaskManagerLab.Models
{
    public class Project
    {
        private int id;
        private string name;

        public Project(int id, string name)
        {
            this.id = id;
            this.name = name;
        }

        public int Id
        {
            get { return id; }
        }

        public string Name
        {
            get { return name; }
            set { name = value; }
        }
    }
}