package tasks;

public class Task {
    String name;
    String description;
    int id;
    String status;

    public Task(String name, String desc, String status) {
        this.name = name;
        description = desc;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
