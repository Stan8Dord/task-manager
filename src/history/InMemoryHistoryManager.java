package history;

import tasks.Task;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class InMemoryHistoryManager implements HistoryManager {
    private final BrandNewLinkedList<Task> tasksHistory = new BrandNewLinkedList<>();
    private final Map<Integer, Node<Task>> tasksHistoryMap = new HashMap<>();

    @Override
    public void add(Task task) {
        int taskId = task.getId();

        if (tasksHistoryMap.containsKey(taskId))
            tasksHistory.removeNode(tasksHistoryMap.get(taskId));
        if (tasksHistory.size >= 10) {
            remove(tasksHistory.head.getData().getId());
        }
        tasksHistory.linkLast(task);
        tasksHistoryMap.put(taskId, tasksHistory.tail);
    }

    @Override
    public List<Task> getHistory() {
        return tasksHistory.getTasks();
    }

    @Override
    public void remove(int id) {
        if (tasksHistoryMap.containsKey(id)) {
            tasksHistory.removeNode(tasksHistoryMap.get(id));
            tasksHistoryMap.remove(id);
        }
    }
}

