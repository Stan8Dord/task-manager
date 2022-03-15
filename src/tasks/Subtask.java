package tasks;

public class Subtask extends Task {
    private int parentId;

    public Subtask(String name, String desc, int epicId) {
        super(name, desc);
        parentId = epicId;
    }

    @Override
    public String toString() {
        String result = "Subtask{id=" + id + ", ";
        result += "name='" + name + "', ";
        if (description != null)
            result = result + "description.length='" + description.length() + "', ";
        else
            result = result + "description=null, ";
        result += "status='" + status + "', ";
        result += "parentId=" + parentId + "]}";

        return result;
    }

    public int getParentId() {
        return parentId;
    }
}
