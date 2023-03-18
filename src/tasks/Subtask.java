package tasks;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Subtask extends Task {
    private int parentId;

    public Subtask(String name, String desc, int epicId, long duration) {
        super(name, desc, duration);
        parentId = epicId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Subtask{id=" + id + ", name='" + name + "', ");
        if (description != null)
            sb.append("description.length='" + description.length() + "', ");
        else
            sb.append("description=null, ");
        sb.append("status='" + status + "', parentId=" + parentId + ", duration=" + duration +
                ", startTime=" + startTime.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")) + "}");

        return sb.toString();
    }

    public int getParentId() {
        return parentId;
    }
}
