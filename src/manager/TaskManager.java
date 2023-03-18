package manager;

import history.HistoryManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.time.ZonedDateTime;
import java.util.List;

public interface TaskManager {
    Task createTask(Task task);

    Subtask createSubtask(Subtask sub);

    Epic createEpic(Epic epic);

    void recalculateEpicStatus(Epic epic);

    List<Task> getTasks();

    List<Subtask> getAllSubtasks();

    List<Subtask> getEpicSubtasks(Epic epic);

    List<Subtask> getEpicSubtasks(int id);

    List<Epic> getEpics();

    void removeAllTasks();

    void removeAllSubtasks();

    void removeEpicSubtasks(Epic epic);

    void removeEpics();

    Task getTaskById(int id);

    Subtask getSubtaskById(int id);

    Epic getEpicById(int id);

    void updateTask(Task task);

    void updateSubtask(Subtask sub);

    void updateEpic(Epic epic);

    void removeTaskById(int id);

    void removeSubtaskById(int id);

    void removeEpicById(int id);

    void recalculateEpicTime(Epic epic);

    List<Task> getPrioritizedTasks();

    void validateNewTaskTime(Task newTask);

    void setStartTime(Task task, ZonedDateTime time);

    HistoryManager getHistoryManager();
}
