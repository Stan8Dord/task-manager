package manager;

import history.HistoryManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private int taskNo = 0;
    private int subtaskNo = 0;
    private int epicNo = 0;
    private final HashMap<Integer,Task> tasks = new HashMap<>();
    private final HashMap<Integer,Subtask> subtasks = new HashMap<>();
    private final HashMap<Integer,Epic> epics = new HashMap<>();

    public HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public Task createTask(Task task) {
        task.setId(++taskNo);
        tasks.put(taskNo, task);

        return task;
    }

    @Override
    public Subtask createSubtask(Subtask sub) {
        int parentId = sub.getParentId();

        sub.setId(++subtaskNo);
        subtasks.put(subtaskNo, sub);
        epics.get(parentId).addSubtask(sub);
        recalculateEpicStatus(epics.get(parentId));

        return sub;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(++epicNo);
        epics.put(epicNo, epic);

        return epic;
    }

    @Override
    public void recalculateEpicStatus(Epic epic) {
        boolean hasNew = false;
        boolean hasDone = false;
        boolean hasInProgress = false;

        if (epic.getSubtasks().isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            for (int subNo : epic.getSubtasks().keySet()) {
                switch (subtasks.get(subNo).getStatus()) {
                    case NEW:
                        hasNew = true;
                        break;
                    case IN_PROGRESS:
                        hasInProgress = true;
                        break;
                    case DONE:
                        hasDone = true;
                        break;
                    default:
                        break;
                }
                if (hasInProgress) {
                    epic.setStatus(TaskStatus.IN_PROGRESS);
                    break;
                }
            }
            if (!hasNew && hasDone && !hasInProgress) {
                epic.setStatus(TaskStatus.DONE);
            } else if (hasNew && !hasDone && !hasInProgress) {
                epic.setStatus(TaskStatus.NEW);
            } else {
                epic.setStatus(TaskStatus.IN_PROGRESS);
            }
        }
    }

    @Override
    public ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();

        if (!this.tasks.isEmpty()) {
            for (int taskNo : this.tasks.keySet()) {
                tasks.add(this.tasks.get(taskNo));
            }
        }

        return tasks;
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        ArrayList<Subtask> subs = new ArrayList<>();

        if (!subtasks.isEmpty()) {
            for (int subNo : this.subtasks.keySet()) {
                subs.add(this.subtasks.get(subNo));
            }
        }

        return subs;
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        ArrayList<Subtask> subs = new ArrayList<>();

        if (!subtasks.isEmpty()) {
            for (int subNo : this.subtasks.keySet()) {
                Subtask sub = this.subtasks.get(subNo);
                if (sub.getParentId() == epic.getId())
                    subs.add(sub);
            }
        }

        return subs;
    }

    @Override
    public ArrayList<Epic> getEpics() {
        ArrayList<Epic> epics = new ArrayList<>();

        if (!this.epics.isEmpty()) {
            for (int epicNo : this.epics.keySet()) {
                epics.add(this.epics.get(epicNo));
            }
        }

        return epics;
    }

    @Override
    public void removeAllTasks() {
        this.tasks.clear();
    }

    @Override
    public void removeAllSubtasks() {
        this.subtasks.clear();
        for (int key : epics.keySet()) {
            epics.get(key).getSubtasks().clear();
            recalculateEpicStatus(epics.get(key));
        }
    }

    @Override
    public void removeEpicSubtasks(Epic epic) {
        if (!epic.getSubtasks().isEmpty()) {
            for (int subNo : epic.getSubtasks().keySet()) {
                subtasks.remove(subNo);
            }
            epic.getSubtasks().clear();
            recalculateEpicStatus(epic);
        }
    }

    @Override
    public void removeEpics() {
        this.epics.clear();
        this.subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Subtask getSubtaskById(int id) {
        historyManager.add(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask sub) {
        subtasks.put(sub.getId(), sub);
        recalculateEpicStatus(epics.get(sub.getParentId()));
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void removeTaskById(int id) {
        tasks.remove(id);
    }

    @Override
    public void removeSubtaskById(int id) {
        Epic epic = epics.get(subtasks.get(id).getParentId());

        subtasks.remove(id);
        epic.getSubtasks().remove(id);
        recalculateEpicStatus(epic);
    }

    @Override
    public void removeEpicById(int id) {
        for (int subNo : epics.get(id).getSubtasks().keySet()) {
            subtasks.remove(subNo);
        }
        epics.remove(id);
    }
}
