package tasks;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Task {
    protected String name;
    protected String description;
    protected int id;
    protected TaskStatus status;
    protected long duration;
    protected ZonedDateTime startTime;

    public Task(String name, String desc, long duration) {
        this.name = name;
        this.description = desc;
        this.duration = duration;
        status = TaskStatus.NEW;

        Instant moment = Instant.now();
        ZoneId zone = ZoneId.of("Europe/Moscow");
        startTime = ZonedDateTime.ofInstant(moment, zone);
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long newDuration) {
        this.duration = newDuration;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime newStartTime) {
        this.startTime = newStartTime;
    }

    public ZonedDateTime getEndTime() {
        if (startTime == null)
            return null;
        else
            return startTime.plus(Duration.ofMinutes(duration));
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
        StringBuilder sb = new StringBuilder("Task{id=" + id + ", name='" + name + "', ");
        if (description != null)
            sb.append("description.length='" + description.length() + "', ");
        else
            sb.append("description=null, ");
        sb.append("status='" + status + "', duration=" + duration + ", startTime=" +
                startTime.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")) + "}");

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task otherTask = (Task) obj;
        return this.id == otherTask.getId() &&
                Duration.between(this.startTime, otherTask.getStartTime()).toMinutes() == 0;
    }
}

