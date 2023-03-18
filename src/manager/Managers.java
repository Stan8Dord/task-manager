package manager;

import history.HistoryManager;
import history.InMemoryHistoryManager;

import java.io.File;
import java.net.URI;

public abstract class Managers {

    public static TaskManager getDefault(int mode, File theFile) {
        switch (mode) {
            case 1:
                return new InMemoryTaskManager();
            case 2:
                return new FileBackedTasksManager(theFile);
            default:
                return new HTTPTaskManager(URI.create("http://localhost:8078/"));
        }
    }

    public static HistoryManager getDefaultHistory() {
            return new InMemoryHistoryManager();
    }
}
