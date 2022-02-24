package tasks;

import java.util.HashMap;

public class Epic extends Task {
    private HashMap<Integer, Integer> subtasks;

    public Epic(String name, String desc, String status) {
        super(name, desc, status);
        subtasks = new HashMap<>();
    }

    @Override
    public String toString() {
        int counter = 0;

        String result = "Epic{id=" + id + ", ";
        result += "name='" + name + "', ";
        if (description != null)
            result = result + "description.length='" + description.length() + "', ";
        else
            result = result + "description=null, ";
        result += "status='" + status + "',";
        if (subtasks.size() > 0) {
            result += "subtasks=[id=";
            for (int subNo : subtasks.keySet()) {
                counter++;
                result += subtasks.get(subNo);
                if (counter != subtasks.size())
                    result += ", id=";
            }
            result += "]}";
        } else
            result += "subtasks=null}";

        return result;
    }

    public HashMap<Integer, Integer> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(Subtask subtask) {
        this.subtasks.put(subtask.getId(), subtask.getId());
    }
}
