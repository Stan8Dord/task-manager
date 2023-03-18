package tests;

import customexceptions.ManagerSaveException;
import manager.FileBackedTasksManager;
import manager.Managers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Task;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTasksManager> {
    private static File theFile;

    @BeforeAll
    public static void beforeAll() {
        theFile = new File("src\\data", "testdatafile.csv");
        if (!theFile.exists()) {
            try {
                theFile.createNewFile();
            } catch (IOException e) {
                throw new ManagerSaveException("Проблема с созданием файла.");
            }
        }
    }

    @BeforeEach
    public void beforeEach() {
        manager = (FileBackedTasksManager) Managers.getDefault(2, theFile);
    }

    @Test
    public void shouldSaveToAndLoadFromFileVoid() {
        String text = "";

        try {
            manager.save();
            FileBackedTasksManager managerFromFile = FileBackedTasksManager.loadFromFile(theFile);
            managerFromFile.getHistoryManager();
            managerFromFile.getPrioritizedTasks();
            managerFromFile.save();
        } catch (Exception e) {
            text = e.getMessage();
        }
        assertEquals("", text);
    }

    @Test
    public void shouldSaveToAndLoadFromFile() {
        Task task = manager.createTask(new Task("Test task1", "Test task1 description", 10));
        Epic epic1 = manager.createEpic(new Epic("Test epic1", "Test epic1 description"));
        final int taskId = task.getId();
        final int epicId = epic1.getId();

        manager.getTaskById(taskId);
        manager.getEpicById(epicId);
        manager.save();
        FileBackedTasksManager managerFromFile = FileBackedTasksManager.loadFromFile(theFile);


        assertEquals(manager.getTasks().size(), managerFromFile.getTasks().size());
        assertEquals(manager.getEpics().size(), managerFromFile.getEpics().size());
        assertEquals(manager.getTaskById(taskId), managerFromFile.getTaskById(taskId));
        assertEquals(manager.getEpicById(epicId), managerFromFile.getEpicById(epicId));
            assertEquals(true, manager.getHistoryManager().getHistory().equals(managerFromFile.getHistoryManager().getHistory()));
    }
}
