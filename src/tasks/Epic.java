package tasks;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Epic extends Task {
    private final Map<Integer, Integer> subtasks;
    private ZonedDateTime epicEndTime;

    public Epic(String name, String desc) {
        super(name, desc, 0);
        subtasks = new HashMap<>();
        epicEndTime = this.getStartTime();
    }

    @Override
    public ZonedDateTime getEndTime() {
        return this.epicEndTime;
    }

    public void setEndTime(ZonedDateTime newTime) {
        this.epicEndTime = newTime;
    }

    @Override
    public String toString() {
        int counter = 0;

        StringBuilder sb = new StringBuilder("Epic{id=" + id + ", name='" + name + "', ");
        if (description != null)
            sb.append("description.length='" + description.length() + "', ");
        else
            sb.append("description=null, ");
        sb.append("status='" + status + "',");
        if (subtasks.size() > 0) {
            sb.append("subtasks=[id=");
            for (int subNo : subtasks.keySet()) {
                counter++;
                sb.append(subtasks.get(subNo));
                if (counter != subtasks.size())
                    sb.append(", id=");
            }
            sb.append("], ");
        } else
            sb.append("subtasks=null, ");
        String time = startTime.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"));
        sb.append("duration=" + duration + ", startTime=" +
                time + "}");

        return sb.toString();
    }

    public Map<Integer, Integer> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(Subtask subtask) {
        this.subtasks.put(subtask.getId(), subtask.getId());
    }
}
