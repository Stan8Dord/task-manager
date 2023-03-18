package tests;

import history.HistoryManager;
import manager.InMemoryTaskManager;
import manager.Managers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void shouldWorkWithVoid() {
        String text = "";
        try {
            historyManager.remove(23);
            historyManager.getHistory();
            historyManager.add(new Task("Test task1", "Test task1 description", 10));
        } catch (Exception e) {
            text = e.getMessage();
        }
        assertEquals("", text);
    }

    @Test
    public void shouldHandleDoubles() {
        InMemoryTaskManager manager = (InMemoryTaskManager) Managers.getDefault(1, null);
        Task task1 = manager.createTask(new Task("Задача1", "Сделать ДЗ", 10));
        Task task2 = manager.createTask(new Task("Задача2", "Погулять", 60L));
        Epic epic1 = manager.createEpic(new Epic("Большая задача без подзадач", "Закончить курс"));
        Epic epic2 = manager.createEpic(new Epic("Большая задача с 3мя подзадачами", "Создать приложение"));

        Subtask subtask1 = manager.createSubtask(new Subtask("Тема",
                "Определиться с темой", epic2.getId(), 10));
        Subtask subtask2 = manager.createSubtask(new Subtask("Технологии",
                "Продумать архитектуру", epic2.getId(), 10));
        Subtask subtask3 = manager.createSubtask(new Subtask("Код",
                "Реализовать задуманное", epic2.getId(), 10));

        manager.getSubtaskById(22);
        manager.getSubtaskById(22);
        manager.getSubtaskById(22);
        manager.getSubtaskById(12);
        manager.getSubtaskById(12);
        manager.getSubtaskById(12);
        manager.getSubtaskById(12);
        manager.getSubtaskById(12);
        manager.getTaskById(10);
        manager.getTaskById(10);

        assertEquals(3, manager.getHistoryManager().getHistory().size());
    }

    @Test
    public void shouldRemoveAny() {
        InMemoryTaskManager manager = (InMemoryTaskManager) Managers.getDefault(1, null);
        Task task1 = manager.createTask(new Task("Задача1", "Сделать ДЗ", 10));
        Epic epic2 = manager.createEpic(new Epic("Большая задача с 3мя подзадачами", "Создать приложение"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Тема",
                "Определиться с темой", epic2.getId(), 10));
        Subtask subtask2 = manager.createSubtask(new Subtask("Технологии",
                "Продумать архитектуру", epic2.getId(), 10));

        manager.getSubtaskById(22);
        manager.getSubtaskById(12);
        manager.getTaskById(10);

        manager.getHistoryManager().remove(12);

        StringBuilder sb = new StringBuilder();
        for (Task task : manager.getHistoryManager().getHistory()) {
            sb.append("_" + task.getId());
        }
        assertEquals("_22_10", sb.toString());

        manager.getHistoryManager().remove(22);
        sb = new StringBuilder();
        for (Task task : manager.getHistoryManager().getHistory()) {
            sb.append("_" + task.getId());
        }
        assertEquals("_10", sb.toString());

        manager.getHistoryManager().remove(10);
        sb = new StringBuilder("");
        for (Task task : manager.getHistoryManager().getHistory()) {
            sb.append("_" + task.getId());
        }
        assertEquals("", sb.toString());
        assertEquals(0, manager.getHistoryManager().getHistory().size());
    }
}
