package manager;

import customexceptions.ManagerSaveException;
import history.HistoryManager;
import tasks.*;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.*;

public class FileBackedTasksManager extends InMemoryTaskManager implements TaskManager {
    private File theFile = null;


    FileBackedTasksManager(File file) {
        this.theFile = file;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(theFile)) {
            writer.write("id,type,name,status,description,epic,duration,startTime\n");
            for (int i = getTasks().size() - 1; i >= 0; i--) {
                writer.write(toString(getTasks().get(i)) + "\n");
            }
            for (int i = getEpics().size() - 1; i >= 0; i--) {
                writer.write(toString(getEpics().get(i)) + "\n");
            }
            for (int i = getAllSubtasks().size() - 1; i >= 0; i--) {
                writer.write(toString(getAllSubtasks().get(i)) + "\n");
            }
            writer.write("\n");
            writer.write(toString(getHistoryManager()));
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager manager = new FileBackedTasksManager(file);

        try (BufferedReader reader = new BufferedReader(new FileReader(file.toString()))) {
            String line = reader.readLine();
            boolean isHistoryRowNext = false;

            while (reader.ready()) {
                line = reader.readLine();
                if (line.length() == 0 && !isHistoryRowNext)
                    isHistoryRowNext = true;
                if (line.length() > 0 && !isHistoryRowNext) {
                    Task task = fromString(line);
                    if (task instanceof Epic) {
                        manager.reCreateEpic((Epic) task, task.getId());
                    } else if (task instanceof Subtask) {
                        Subtask sub = manager.reCreateSubtask((Subtask) task, task.getId());
                        manager.sortedTasks.add(sub);
                    } else {
                        Task tsk = manager.reCreateTask(task, task.getId());
                        manager.sortedTasks.add(tsk);
                    }
                } else if (line.length() > 0 && isHistoryRowNext) {
                    List<Integer> list = historyFromString(line);
                    for (int item : list) {
                        if (manager.tasks.containsKey(item)) {
                            Task task = manager.tasks.get(item);
                            manager.getHistoryManager().add(task);
                        } else if (manager.epics.containsKey(item)) {
                            Epic epic = manager.epics.get(item);
                            manager.getHistoryManager().add(epic);
                        } else if (manager.subtasks.containsKey(item)) {
                            Subtask sub = manager.subtasks.get(item);
                            manager.getHistoryManager().add(sub);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }

        return manager;
    }

    private String toString(Task task) {
        String[] fields = new String[8];

        fields[0] = Integer.toString(task.getId());
        fields[1] = TaskType.TASK.toString();
        fields[2] = task.getName();
        fields[3] = task.getStatus().toString();
        fields[4] = task.getDescription();
        fields[6] = String.valueOf(task.getDuration());
        fields[7] = task.getStartTime().format(SAVE_FORMAT);

        if (task instanceof Epic) {
            fields[1] = TaskType.EPIC.toString();
        } else if (task instanceof Subtask) {
            Subtask sub = (Subtask) task;
            fields[1] = TaskType.SUBTASK.toString();
            fields[5] = Integer.toString(sub.getParentId());
        }
        String str = String.join(",", fields).replace("null","");

        return str;
    }

    private static String toString(HistoryManager manager) {
        if (manager.getHistory().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Task task : manager.getHistory()) {
                sb.append(task.getId() + ",");
            }
            sb.setLength(sb.length() - 1);

            return sb.toString();
        } else
            return "";
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> list = new ArrayList<>();
        String[] values = value.split(",");

         for (String element : values) {
             list.add(Integer.parseInt(element));
         }

         return list;
    }

    private static Task fromString(String value) {
        String[] columns = value.split(",");
        if (columns[1].equals(TaskType.TASK.name())) {
            Task task = new Task(columns[2], columns[4], Long.parseLong(columns[6]));
            task.setId(Integer.parseInt(columns[0]));
            task.setStatus(TaskStatus.valueOf(columns[3]));
            task.setStartTime(ZonedDateTime.parse(columns[7], SAVE_FORMAT));
            return task;
        } else if (columns[1].equals(TaskType.EPIC.name())) {
            Task epic = new Epic(columns[2], columns[4]);
            epic.setId(Integer.parseInt(columns[0]));
            epic.setStatus(TaskStatus.valueOf(columns[3]));
            epic.setDuration(Long.parseLong(columns[6]));
            epic.setStartTime(ZonedDateTime.parse(columns[7], SAVE_FORMAT));
            return epic;
        } else if (columns[1].equals(TaskType.SUBTASK.name())) {
            Task sub = new Subtask(columns[2], columns[4], Integer.parseInt(columns[5]), Long.parseLong(columns[6]));
            sub.setId(Integer.parseInt(columns[0]));
            sub.setStatus(TaskStatus.valueOf(columns[3]));
            sub.setStartTime(ZonedDateTime.parse(columns[7], SAVE_FORMAT));
            return sub;
        }

        return null;
    }

    @Override
    public Task createTask(Task task) {
        Task tsk = super.createTask(task);

        save();
        return tsk;
    }

    public Task reCreateTask(Task task, int id) {
        task.setId(id);
        taskNo = id;
        tasks.put(id, task);

        return task;
    }

    @Override
    public Subtask createSubtask(Subtask sub) {
        Subtask subtask = super.createSubtask(sub);
        save();

        return subtask;
    }

    public Subtask reCreateSubtask(Subtask sub, int id) {
        Epic epic = epics.get(sub.getParentId());

        sub.setId(id);
        subtaskNo = id;
        subtasks.put(id, sub);
        epic.addSubtask(sub);

        return sub;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic epc = super.createEpic(epic);
        save();

        return epc;
    }

    public Epic reCreateEpic(Epic epic, int id) {
        epic.setId(id);
        epics.put(id, epic);
        epicNo = id;

        return epic;
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public void removeEpicSubtasks(Epic epic) {
        super.removeEpicSubtasks(epic);
        save();
    }

    @Override
    public void removeEpics() {
        super.removeEpics();
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask sub) {
        super.updateSubtask(sub);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeSubtaskById(int id) {
        super.removeSubtaskById(id);
        save();
    }

    @Override
    public void removeEpicById(int id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask sub = super.getSubtaskById(id);
        save();
        return sub;
    }
}
