package tasks;

public class Task {
    protected String name;
    protected String description;
    protected int id;
    protected TaskStatus status;

    public Task(String name, String desc) {
        this.name = name;
        description = desc;
        this.status = status;
        status = TaskStatus.NEW;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        String result = "Task{id=" + id + ", ";
        result += "name='" + name + "', ";
        if (description != null)
            result = result + "description.length='" + description.length() + "', ";
        else
            result = result + "description=null, ";
        result += "status='" + status + "]}";

        return result;
    }
}
