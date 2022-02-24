package manager;

import java.util.HashMap;
import java.util.ArrayList;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

public class Manager {
    private int taskNo = 0;
    private int subtaskNo = 0;
    private int epicNo = 0;
    private HashMap<Integer,Task> tasks = new HashMap<>();
    private HashMap<Integer,Subtask> subtasks = new HashMap<>();
    private HashMap<Integer,Epic> epics = new HashMap<>();

    public Task createTask(Task task) {
        task.setId(++taskNo);
        tasks.put(taskNo, task);

        return task;
    }

    public Subtask createSubtask(Subtask sub) {
        int parentId = sub.getParentId();

        sub.setId(++subtaskNo);
        subtasks.put(subtaskNo, sub);
        epics.get(parentId).addSubtask(sub);
        recalculateEpicStatus(epics.get(parentId));

        return sub;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(++epicNo);
        epics.put(epicNo, epic);

        return epic;
    }

    public void recalculateEpicStatus(Epic epic) {
        boolean hasNew = false;
        boolean hasDone = false;
        boolean hasInProgress = false;

        if (epic.getSubtasks().isEmpty()) {
            epic.setStatus("NEW");
        } else {
            for (int subNo : epic.getSubtasks().keySet()) {
                switch (subtasks.get(subNo).getStatus()) {
                    case "NEW":
                        hasNew = true;
                        break;
                    case "IN_PROGRESS":
                        hasInProgress = true;
                        break;
                    case "DONE":
                        hasDone = true;
                        break;
                    default:
                        break;
                }
                if (hasInProgress) {
                    epic.setStatus("IN_PROGRESS");
                    break;
                }
            }
            if (!hasNew && hasDone && !hasInProgress) {
                epic.setStatus("DONE");
            } else if (hasNew && !hasDone && !hasInProgress) {
                epic.setStatus("NEW");
            } else {
                epic.setStatus("IN_PROGRESS");
            }
        }
    }

    public ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();

        if (!this.tasks.isEmpty()) {
            for (int taskNo : this.tasks.keySet()) {
                tasks.add(this.tasks.get(taskNo));
            }
        }

        return tasks;
    }

    public ArrayList<Subtask> getAllSubtasks() {
        ArrayList<Subtask> subs = new ArrayList<>();

        if (!subtasks.isEmpty()) {
            for (int subNo : this.subtasks.keySet()) {
                subs.add(this.subtasks.get(subNo));
            }
        }

        return subs;
    }

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

    public ArrayList<Epic> getEpics() {
        ArrayList<Epic> epics = new ArrayList<>();

        if (!this.epics.isEmpty()) {
            for (int epicNo : this.epics.keySet()) {
                epics.add(this.epics.get(epicNo));
            }
        }

        return epics;
    }

    public void removeAllTasks() {
        this.tasks.clear();
    }

    public void removeAllSubtasks() {
        this.subtasks.clear();
        for (int key : epics.keySet()) {
            epics.get(key).getSubtasks().clear();
            recalculateEpicStatus(epics.get(key));
        }
    }

    public void removeEpicSubtasks(Epic epic) {
        if (!epic.getSubtasks().isEmpty()) {
            for (int subNo : epic.getSubtasks().keySet()) {
                subtasks.remove(subNo);
            }
            epic.getSubtasks().clear();
            recalculateEpicStatus(epic);
        }
    }

    public void removeEpics() {
        this.epics.clear();
        this.subtasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateSubtask(Subtask sub) {
        subtasks.put(sub.getId(), sub);
        recalculateEpicStatus(epics.get(sub.getParentId()));
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void removeTaskById(int id) {
        tasks.remove(id);
    }

    public void removeSubtaskById(int id) {
        Epic epic = epics.get(subtasks.get(id).getParentId());

        subtasks.remove(id);
        epic.getSubtasks().remove(id);
        recalculateEpicStatus(epic);
    }

    public void removeEpicById(int id) {
        for (int subNo : epics.get(id).getSubtasks().keySet()) {
            subtasks.remove(subNo);
        }
        epics.remove(id);
    }
}
