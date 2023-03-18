package manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import servers.KVTaskClient;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

public class HTTPTaskManager extends FileBackedTasksManager {
    private final KVTaskClient kvClient;
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
            .create();

    HTTPTaskManager(URI serverUrl) {
        super(null);
        this.kvClient = new KVTaskClient(serverUrl);
    }

    @Override
    public void save() {
        try {
            kvClient.put("tasks", gson.toJson(getTasks()));
            kvClient.put("epics", gson.toJson(getEpics()));
            kvClient.put("subtasks", gson.toJson(getAllSubtasks()));
            kvClient.put("history", gson.toJson(getHistoryManager().getHistory()));
            kvClient.put("sortedTasks", gson.toJson(getPrioritizedTasks()));
        } catch (IOException | InterruptedException e) {
            System.out.println("Возникла проблема при сериализации: " + e.getMessage());
        }
    }

    public static HTTPTaskManager load(URI serverUrl) {
        HTTPTaskManager manager = new HTTPTaskManager(serverUrl);

        try {
            List<Task> tasksList = gson.fromJson(manager.kvClient.load("tasks"), new TypeToken<List<Task>>() {}.getType());
            for (int i = tasksList.size() - 1; i >= 0; i--) {
                Task task = tasksList.get(i);
                manager.tasks.put(task.getId(), task);
            }

            List<Epic> epicsList = gson.fromJson(manager.kvClient.load("epics"), new TypeToken<List<Epic>>() {}.getType());
            for (int i = epicsList.size() - 1; i >= 0; i--) {
                Epic epic = epicsList.get(i);
                manager.epics.put(epic.getId(), epic);
            }

            List<Subtask> subsList = gson.fromJson(manager.kvClient.load("subtasks"),
                    new TypeToken<List<Subtask>>() {}.getType());
            for (int i = subsList.size() - 1; i >= 0; i--) {
                Subtask subtask = subsList.get(i);
                manager.subtasks.put(subtask.getId(), subtask);
            }

            List<Task> historyList = gson.fromJson(manager.kvClient.load("history"), new TypeToken<List<Task>>() {}.getType());
            for (int i = 0; i < historyList.size(); i++) {
                int id = historyList.get(i).getId();
                switch (id % 10) {
                    case 1:
                        manager.getHistoryManager().add(manager.epics.get(id));
                        break;
                    case 2:
                        manager.getHistoryManager().add(manager.subtasks.get(id));
                        break;
                    default:
                        manager.getHistoryManager().add(historyList.get(i));
                }
            }

            List<Task> sortedTasksList = gson.fromJson(manager.kvClient.load("sortedTasks"),
                    new TypeToken<List<Task>>() {}.getType());
            for (int i = sortedTasksList.size() - 1; i >= 0; i--) {
                int id = sortedTasksList.get(i).getId();
                switch (id % 10) {
                    case 1:
                        manager.sortedTasks.add(manager.epics.get(id));
                        break;
                    case 2:
                        manager.sortedTasks.add(manager.subtasks.get(id));
                        break;
                    default:
                        manager.sortedTasks.add(sortedTasksList.get(i));
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Возникла проблема при десериализации: " + e.getMessage());
        }

        return manager;
    }

    public static class ZonedDateTimeAdapter extends TypeAdapter<ZonedDateTime> {
        @Override
        public void write(final JsonWriter jsonWriter, final ZonedDateTime znd) throws IOException {
            jsonWriter.value(znd.format(SAVE_FORMAT));
        }

        @Override
        public ZonedDateTime read(final JsonReader jsonReader) throws IOException {
            return ZonedDateTime.parse(jsonReader.nextString(), SAVE_FORMAT);
        }
    }

}
