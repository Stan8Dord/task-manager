package history;

import java.util.ArrayList;
import tasks.Task;

public interface HistoryManager {
    void add(Task task);

    ArrayList<Task> getHistory();
}
