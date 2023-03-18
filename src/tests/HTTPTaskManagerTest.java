package tests;

import manager.HTTPTaskManager;
import manager.Managers;
import org.junit.jupiter.api.*;
import servers.KVServer;
import tasks.Epic;
import tasks.Task;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HTTPTaskManagerTest extends TaskManagerTest<HTTPTaskManager> {
    private static KVServer kvServer;

    @BeforeAll
    public static void beforeAll() {
        try {
            kvServer = new KVServer();
            kvServer.start();
        } catch (IOException e) {
            System.out.println("Проблема с запуском сервера.");
        }
    }

    @BeforeEach
    public void beforeEach() {
        manager = (HTTPTaskManager) Managers.getDefault(3,null);
    }

    @AfterAll
    public static void afterEach() {
        kvServer.stop();
    }

    @Test
    public void shouldSaveToAndLoadFromVoid() {
        String text = "";

        try {
            manager.save();
            HTTPTaskManager managerFromKVServer = HTTPTaskManager.load(URI.create("http://localhost:8078/"));
            assertTrue(managerFromKVServer.getHistoryManager() != null);
            assertEquals(managerFromKVServer.getPrioritizedTasks().size(), 0);
            managerFromKVServer.save();
        } catch (Exception e) {
            text = e.getMessage();
        }
        assertEquals("", text);
    }

    @Test
    public void shouldSaveToAndLoadFromKVServer() {
        Task task = manager.createTask(new Task("Test task1", "Test task1 description", 10));
        Epic epic1 = manager.createEpic(new Epic("Test epic1", "Test epic1 description"));
        final int taskId = task.getId();
        final int epicId = epic1.getId();

        manager.getTaskById(taskId);
        manager.getEpicById(epicId);
        manager.save();
        HTTPTaskManager managerFromKVServer = HTTPTaskManager.load(URI.create("http://localhost:8078/"));

        assertEquals(manager.getTasks().size(), managerFromKVServer.getTasks().size());
        assertEquals(manager.getEpics().size(), managerFromKVServer.getEpics().size());
        assertEquals(manager.getTaskById(taskId), managerFromKVServer.getTaskById(taskId));
        assertEquals(manager.getEpicById(epicId), managerFromKVServer.getEpicById(epicId));
        assertEquals(manager.getHistoryManager().getHistory(), managerFromKVServer.getHistoryManager().getHistory());
    }
}
