package tests;

import customexceptions.WrongIdException;
import manager.TaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    @Test
    void shouldCreateAndReturnTask() {
        Task task = manager.createTask(new Task("Test task1", "Test task1 description", 10));
        final int taskId = task.getId();

        final Task savedTask = manager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = manager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают");
    }

    @Test
    void shouldCreateAndReturnEpic() {
        Epic epic = manager.createEpic(new Epic("Test epic1", "Test epic1 description"));
        final int epicId = epic.getId();

        final Epic savedEpic = manager.getEpicById(epicId);

        assertNotNull(savedEpic, "Задача EPIC не найдена.");
        assertEquals(epic, savedEpic, "Задачи EPIC не совпадают.");

        final List<Epic> epics = manager.getEpics();

        assertNotNull(epics, "Задачи EPIC не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество задач EPIC.");
        assertEquals(epic, epics.get(0), "Задачи EPIC не совпадают");
    }

    @Test
    void shouldCreateAndReturnSubtask() {
        Epic epic = manager.createEpic(new Epic("Test epic1", "Test epic1 description"));
        final int epicId = epic.getId();
        Subtask subtask = manager.createSubtask(new Subtask("Test subtask1",
                "Subtask1 desc", epicId, 0));
        Subtask subtask2 = manager.createSubtask(new Subtask("Test subtask2",
                "Subtask2 desc", epicId, 0));
        final int subtaskId = subtask.getId();

        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);

        assertNotNull(savedSubtask, "Задача SUBTASK1 не найдена.");
        assertEquals(subtask, savedSubtask, "Задачи SUBTASK не совпадают.");

        final List<Subtask> subtasks = manager.getAllSubtasks();
        final List<Subtask> epicSubs = manager.getEpicSubtasks(epic);

        assertNotNull(subtasks, "Задачи SUBTASK не возвращаются.");
        assertNotNull(epicSubs, "Задачи epic's-SUBTASK не возвращаются.");
        assertEquals(2, subtasks.size(), "Неверное количество задач SUBTASK.");
        assertEquals(2, epicSubs.size(), "Неверное количество задач epic's-SUBTASK.");
        assertEquals(subtask, subtasks.get(1), "Задачи SUBTASK не совпадают");
        assertEquals(subtask2, subtasks.get(0), "Задачи SUBTASK не совпадают");
        assertEquals(subtask, epicSubs.get(1), "Задачи SUBTASK не совпадают");
        assertEquals(subtask2, epicSubs.get(0), "Задачи SUBTASK не совпадают");

        final int parentId = subtask.getParentId();

        assertEquals(epicId, parentId, "Некорректный номер parentId");
    }

    @Test
    void shouldRemoveTasks() {
        Task task = manager.createTask(new Task("Test task1", "Test task1 description", 10));
        Task task2 = manager.createTask(new Task("Test task2", "Test task2 description", 10));
        Task task3 = manager.createTask(new Task("Test task3", "Test task3 description", 10));
        final int task2Id = task2.getId();

        manager.removeTaskById(task2Id);
        List<Task> tasks = manager.getTasks();

        assertEquals(null, manager.getTaskById(task2Id), "Не удалилось по ID");
        assertEquals("false", String.valueOf(tasks.contains(task2)), "Не удалилось по ID");

        manager.removeAllTasks();
        tasks = manager.getTasks();

        assertEquals(0, tasks.size(), "Не все задачи удалены.");
    }

    @Test
    void shouldRemoveEpicsAndEpicsSubtasks() {
        Epic epic1 = manager.createEpic(new Epic("Test epic1", "Test epic1 description"));
        Epic epic2 = manager.createEpic(new Epic("Test epic2", "Test epic2 description"));
        Epic epic3 = manager.createEpic(new Epic("Test epic3", "Test epic3 description"));
        final int epic1Id = epic1.getId();
        final int epic2Id = epic2.getId();
        Task subtask1 = manager.createSubtask(new Subtask("Test subtask1",
                "Subtask1 desc", epic1Id, 0));
        Subtask subtask2 = manager.createSubtask(new Subtask("Test subtask2",
                "Subtask2 desc", epic1Id, 0));

        manager.removeEpicById(epic2Id);
        List<Epic> epics = manager.getEpics();

        assertEquals(null, manager.getEpicById(epic2Id), "Не удалилось по ID");
        assertEquals("false", String.valueOf(epics.contains(epic2)), "Не удалилось по ID");

        manager.removeEpicSubtasks(epic1);
        List<Subtask> subtasks = manager.getEpicSubtasks(epic1);
        assertEquals(0, subtasks.size(), "Не все задачи удалены.");

        manager.removeEpics();
        epics = manager.getEpics();

        assertEquals(0, epics.size(), "Не все задачи удалены.");
    }

    @Test
    void shouldRemoveSubtasks() {
        Epic epic = manager.createEpic(new Epic("Test epic1", "Test epic1 description"));
        final int epicId = epic.getId();
        Subtask subtask1 = manager.createSubtask(new Subtask("Test subtask1",
                "Subtask1 desc", epicId, 0));
        Subtask subtask2 = manager.createSubtask(new Subtask("Test subtask2",
                "Subtask2 desc", epicId, 0));
        Subtask subtask3 = manager.createSubtask(new Subtask("Test subtask3",
                "Subtask3 desc", epicId, 0));
        final int subtask2Id = subtask2.getId();

        manager.removeSubtaskById(subtask2Id);
        List<Subtask> subtasks = manager.getAllSubtasks();

        assertEquals(null, manager.getSubtaskById(subtask2Id), "Не удалилось по ID");
        assertEquals("false", String.valueOf(subtasks.contains(subtask2)), "Не удалилось по ID");

        manager.removeAllSubtasks();
        subtasks = manager.getAllSubtasks();

        assertEquals(0, subtasks.size(), "Не все задачи удалены.");
    }

    @Test
    void shouldUpdateTask() {
        Task task = manager.createTask(new Task("Test task1", "Test task1 description", 10));
        manager.removeTaskById(task.getId());
        task.setName("Test task2");
        task.setDescription("Test task2 description");
        manager.updateTask(task);

        final Task savedTask = manager.getTaskById(task.getId());

        assertEquals("Test task2", savedTask.getName(), "Название не обновилось.");
        assertEquals("Test task2 description", savedTask.getDescription(), "Описание не обновилось.");
    }

    @Test
    void shouldUpdateSubtask() {
        Epic epic = manager.createEpic(new Epic("Test epic1", "Test epic1 description"));
        final int epicId = epic.getId();
        Subtask subtask1 = manager.createSubtask(new Subtask("Test subtask1",
                "Subtask1 desc", epicId, 0));
        manager.removeSubtaskById(subtask1.getId());
        subtask1.setName("Test subtask2");
        subtask1.setDescription("Test subtask2 description");
        manager.updateSubtask(subtask1);

        final Subtask savedSubtask = manager.getSubtaskById(subtask1.getId());

        assertEquals("Test subtask2", savedSubtask.getName(), "Название не обновилось.");
        assertEquals("Test subtask2 description", savedSubtask.getDescription(), "Описание не обновилось.");
    }

    @Test
    void shouldUpdateEpic() {
        Epic epic = manager.createEpic(new Epic("Test epic1", "Test epic1 description"));

        manager.removeEpicById(epic.getId());
        epic.setName("Test epic2");
        epic.setDescription("Test epic2 description");
        manager.updateEpic(epic);

        final Epic savedEpic = manager.getEpicById(epic.getId());

        assertEquals("Test epic2", savedEpic.getName(), "Название не обновилось.");
        assertEquals("Test epic2 description", savedEpic.getDescription(), "Описание не обновилось.");
    }

    @Test
    void shouldRecalculateEpicStatus() {
        Epic epic = manager.createEpic(new Epic("Test epic1", "Test epic1 description"));
        final int epicId = epic.getId();
        Subtask subtask1 = manager.createSubtask(new Subtask("Test subtask1",
                "Subtask1 desc", epicId, 0));
        Subtask subtask2 = manager.createSubtask(new Subtask("Test subtask2",
                "Subtask2 desc", epicId, 0));
        Subtask subtask3 = manager.createSubtask(new Subtask("Test subtask3",
                "Subtask3 desc", epicId, 0));

        TaskStatus epicStatus = epic.getStatus();
        assertEquals(TaskStatus.NEW, epicStatus);

        subtask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.recalculateEpicStatus(epic);
        epicStatus = epic.getStatus();
        assertEquals(TaskStatus.IN_PROGRESS, epicStatus);

        subtask2.setStatus(TaskStatus.DONE);
        manager.recalculateEpicStatus(epic);
        epicStatus = epic.getStatus();
        assertEquals(TaskStatus.IN_PROGRESS, epicStatus);

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        subtask3.setStatus(TaskStatus.DONE);
        manager.recalculateEpicStatus(epic);
        epicStatus = epic.getStatus();
        assertEquals(TaskStatus.DONE, epicStatus);

        manager.removeEpicSubtasks(epic);
        manager.recalculateEpicStatus(epic);
        epicStatus = epic.getStatus();
        assertEquals(TaskStatus.NEW, epicStatus);
    }

    @Test
    void shouldWorkWithVoidAndAcceptAnyValues() {
        Task task = manager.getTaskById(999);
        Epic epic = manager.getEpicById(111);
        Subtask sub = manager.getSubtaskById(222);

        assertEquals(null, task);
        assertEquals(null, epic);
        assertEquals(null, sub);

        WrongIdException exception = Assertions.assertThrows(
                WrongIdException.class,
                () -> manager.removeTaskById(123)
        );
        assertEquals("Обращение к несуществующему ID", exception.getMessage());
        exception = Assertions.assertThrows(
                WrongIdException.class,
                () -> manager.removeEpicById(123)
        );
        assertEquals("Обращение к несуществующему ID", exception.getMessage());
        exception = Assertions.assertThrows(
                WrongIdException.class,
                () -> manager.removeSubtaskById(123)
        );
        assertEquals("Обращение к несуществующему ID", exception.getMessage());
    }

    @Test
    public void checkTimeAndDuration() {
        Epic epic = manager.createEpic(new Epic("Test epic1", "Test epic1 description"));
        final int epicId = epic.getId();
        Subtask subtask1 = manager.createSubtask(new Subtask("Test subtask1",
                "Subtask1 desc", epicId, 0));
        Subtask subtask2 = manager.createSubtask(new Subtask("Test subtask2",
                "Subtask2 desc", epicId, 0));

        ZonedDateTime nowTime = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

        assertEquals(Duration.between(nowTime, subtask1.getStartTime()).toMinutes(), 0);

        subtask1.setDuration(10);
        manager.setStartTime(subtask1, nowTime);
        subtask2.setDuration(10);
        manager.setStartTime(subtask2, nowTime);
        manager.recalculateEpicTime(epic);

        assertEquals(subtask1.getEndTime(), subtask2.getStartTime());
        assertEquals(10, subtask1.getDuration());
        assertEquals(subtask1.getDuration() + subtask2.getDuration(), epic.getDuration());
        assertEquals(subtask1.getStartTime(), epic.getStartTime());
        assertEquals(subtask2.getEndTime(), epic.getEndTime());
    }
}
