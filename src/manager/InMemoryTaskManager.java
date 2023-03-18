package manager;

import customexceptions.WrongIdException;
import history.HistoryManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected int taskNo = 0;
    protected int subtaskNo = 0;
    protected int epicNo = 0;
    protected final Map<Integer,Task> tasks = new HashMap<>();
    protected final Map<Integer,Subtask> subtasks = new HashMap<>();
    protected final Map<Integer,Epic> epics = new HashMap<>();
    private final HistoryManager historyManager;
    protected final TreeSet<Task> sortedTasks;

    public static final DateTimeFormatter SAVE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm VVZZZZZ");

    InMemoryTaskManager() {
        Comparator<Task> comparator = (t1, t2) -> {
            if (t1.getStartTime().toEpochSecond() - t2.getStartTime().toEpochSecond() < 0)
                    return -1;
            else if (t1.getStartTime().toEpochSecond() - t2.getStartTime().toEpochSecond() > 0)
                    return 1;
            else
                return 0;
        };
        sortedTasks = new TreeSet<>(comparator);
        historyManager = Managers.getDefaultHistory();
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public Task createTask(Task task) {
        boolean done = false;
        int i = 1;

        while (!done) {
            taskNo = (taskNo / 10 + i) * 10;
            if (tasks.keySet().contains(taskNo))
                i++;
            else {
                task.setId(taskNo);
                validateNewTaskTime(task);
                tasks.put(taskNo, task);
                done = true;
            }
        }

        return task;
    }

    @Override
    public Subtask createSubtask(Subtask sub) {
        int parentId = sub.getParentId();
        boolean done = false;
        int i = 1;

        while (!done) {
            subtaskNo = (subtaskNo / 10 + i) * 10 + 2;
            if (subtasks.keySet().contains(subtaskNo))
                i++;
            else {
                sub.setId(subtaskNo);
                validateNewTaskTime(sub);
                subtasks.put(subtaskNo, sub);
                epics.get(parentId).addSubtask(sub);
                recalculateEpicStatus(epics.get(parentId));
                recalculateEpicTime(epics.get(sub.getParentId()));
                done = true;
            }
        }

        return sub;
    }

    @Override
    public Epic createEpic(Epic epic) {
        boolean done = false;
        int i = 1;

        while (!done) {
            epicNo = (epicNo / 10 + i) * 10 + 1;
            if (epics.keySet().contains(epicNo))
                i++;
            else {
                epic.setId(epicNo);
                epics.put(epicNo, epic);
                done = true;
            }
        }

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
    public List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();

        if (!this.tasks.isEmpty()) {
            for (int taskNo : this.tasks.keySet()) {
                tasks.add(this.tasks.get(taskNo));
            }
        }

        return tasks;
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        List<Subtask> subs = new ArrayList<>();

        if (!subtasks.isEmpty()) {
            for (int subNo : this.subtasks.keySet()) {
                subs.add(this.subtasks.get(subNo));
            }
        }

        return subs;
    }

    @Override
    public List<Subtask> getEpicSubtasks(Epic epic) {
        return getEpicSubtasks(epic.getId());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int id) {
        List<Subtask> subs = new ArrayList<>();

        if (!subtasks.isEmpty()) {
            for (int subNo : this.subtasks.keySet()) {
                Subtask sub = this.subtasks.get(subNo);
                if (sub.getParentId() == id)
                    subs.add(sub);
            }
        }

        return subs;
    }

    @Override
    public List<Epic> getEpics() {
        List<Epic> epics = new ArrayList<>();

        if (!this.epics.isEmpty()) {
            for (int epicNo : this.epics.keySet()) {
                epics.add(this.epics.get(epicNo));
            }
        }

        return epics;
    }

    @Override
    public void removeAllTasks() {
        for (int id : tasks.keySet()) {
            historyManager.remove(id);
        }
        this.tasks.clear();
        sortedTasks.clear();
    }

    @Override
    public void removeAllSubtasks() {
        for (int id : subtasks.keySet()) {
            historyManager.remove(id);
            sortedTasks.remove(subtasks.get(id));
        }
        this.subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            recalculateEpicStatus(epic);
            recalculateEpicTime(epic);
        }
    }

    @Override
    public void removeEpicSubtasks(Epic epic) {
        if (!epic.getSubtasks().isEmpty()) {
            for (int subNo : epic.getSubtasks().keySet()) {
                sortedTasks.remove(subtasks.get(subNo));
                subtasks.remove(subNo);
                historyManager.remove(subNo);
            }
            epic.getSubtasks().clear();
            recalculateEpicStatus(epic);
            recalculateEpicTime(epic);
        }
    }

    @Override
    public void removeEpics() {
        for (Epic epic : epics.values()) {
            for (int subNo : epic.getSubtasks().keySet()) {
                sortedTasks.remove(subtasks.get(subNo));
                historyManager.remove(subNo);
            }
            sortedTasks.remove(epic);
            historyManager.remove(epic.getId());
        }
        this.epics.clear();
        this.subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        if (tasks.get(id) != null) {
            historyManager.add(tasks.get(id));
        }
        return tasks.get(id);
    }

    @Override
    public Subtask getSubtaskById(int id) {
        if (subtasks.get(id) != null) {
            historyManager.add(subtasks.get(id));
        }
        return subtasks.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        if (epics.get(id) != null) {
            historyManager.add(epics.get(id));
        }
        return epics.get(id);
    }

    @Override
    public void updateTask(Task task) {
        validateNewTaskTime(task);
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask sub) {
        int parentId = sub.getParentId();
        subtasks.put(sub.getId(), sub);
        recalculateEpicStatus(epics.get(parentId));
        validateNewTaskTime(sub);
        recalculateEpicTime(epics.get(parentId));
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void removeTaskById(int id) {
        if (tasks.containsKey(id)) {
            if (tasks.get(id) != null) {
                sortedTasks.remove(tasks.get(id));
                tasks.remove(id);
                historyManager.remove(id);
            }
        } else {
            throw new WrongIdException("Обращение к несуществующему ID");
        }
    }

    @Override
    public void removeSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            if (subtasks.get(id) != null) {
                Epic epic = epics.get(subtasks.get(id).getParentId());

                sortedTasks.remove(subtasks.get(id));
                subtasks.remove(id);
                epic.getSubtasks().remove(id);
                recalculateEpicStatus(epic);
                historyManager.remove(id);
                recalculateEpicTime(epic);
            }
        } else {
            throw new WrongIdException("Обращение к несуществующему ID");
        }
    }

    @Override
    public void removeEpicById(int id) {
        if (epics.containsKey(id)) {
            if (epics.get(id) != null) {
                for (int subNo : epics.get(id).getSubtasks().keySet()) {
                    sortedTasks.remove(subtasks.get(subNo));
                    subtasks.remove(subNo);
                    historyManager.remove(subNo);
                }
                sortedTasks.remove(epics.get(id));
                epics.remove(id);
                historyManager.remove(id);
            }
        } else {
            throw new WrongIdException("Обращение к несуществующему ID");
        }
    }

    @Override
    public void recalculateEpicTime(Epic epic) {
        long sTime = 0;
        long eTime = 0;
        long duration = 0;
        ZonedDateTime epicStart = null;
        ZonedDateTime epicEnd = null;

        if (epic.getSubtasks().isEmpty()) {
            epic.setEndTime(epic.getStartTime());
        } else {
            for (int subNo : epic.getSubtasks().keySet()) {
                long subTime = subtasks.get(subNo).getStartTime().toEpochSecond();
                if (sTime > subTime || sTime == 0) {
                    sTime = subTime;
                    epicStart = subtasks.get(subNo).getStartTime();
                }
                subTime = subtasks.get(subNo).getEndTime().toEpochSecond();
                if (eTime < subTime) {
                    eTime = subTime;
                    epicEnd = subtasks.get(subNo).getEndTime();
                }
                duration += subtasks.get(subNo).getDuration();
            }
            epic.setStartTime(epicStart);
            epic.setDuration(duration);
            epic.setEndTime(epicEnd);
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        List<Task> tasks = new ArrayList<>();
        Map<Integer, Byte> epicMap = new HashMap<>();

        for (Task task : sortedTasks) {
            if (task instanceof Subtask) {
                Epic epic = epics.get(((Subtask) task).getParentId());
                if (!epicMap.containsKey(epic.getId())) {
                    tasks.add(epic);
                    epicMap.put(epic.getId(), (byte) 0);
                }
            }
            tasks.add(task);
        }
        return tasks;
    }

    @Override
    public void validateNewTaskTime(Task newTask) {
        if (!sortedTasks.contains(newTask) || !sortedTasks.ceiling(newTask).equals(newTask)) {
            long newTaskStartTime = newTask.getStartTime().toEpochSecond();
            if (!sortedTasks.isEmpty()) {
                Task maxTask = sortedTasks.last();

                if (maxTask.getEndTime().toEpochSecond() >= newTaskStartTime) {
                    ZonedDateTime newTime = maxTask.getEndTime().plusMinutes(maxTask.getDuration() > 0 ? 0 : 1);
                    System.out.println("Время задачи пересекается с существующими, для задачи '" +
                            newTask.getName() + "' установлено " +
                            "ближайшее доступное время: " + newTime.format(SAVE_FORMAT));
                    newTask.setStartTime(newTime);
                    if (newTask instanceof Subtask) recalculateEpicTime(epics.get(((Subtask) newTask).getParentId()));
                }
            }
            sortedTasks.add(newTask);
        }
    }

    @Override
    public void setStartTime(Task task, ZonedDateTime time) {
        if (!(task instanceof Epic)) {
            if (!task.getStartTime().isEqual(time)) {
                if (sortedTasks.contains(task)) sortedTasks.remove(task);
                task.setStartTime(time);
                if (task instanceof Subtask) recalculateEpicTime(epics.get(((Subtask) task).getParentId()));
                validateNewTaskTime(task);
            }
        }
    }
}
