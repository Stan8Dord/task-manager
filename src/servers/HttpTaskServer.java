package servers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import manager.HTTPTaskManager;
import manager.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

public class HttpTaskServer {
    private final HttpServer httpServer;
    private final TaskManager manager;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new HTTPTaskManager.ZonedDateTimeAdapter())
            .create();

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.httpServer = HttpServer.create();
        this.httpServer.bind(new InetSocketAddress(8080), 0);
        this.httpServer.createContext("/tasks", new TasksHandler());
        this.manager = manager;
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

    public TaskManager getManager() {
        return manager;
    }

    class TasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response;
            int code = 200;
            String method = httpExchange.getRequestMethod();
            String path = httpExchange.getRequestURI().getPath();
            String[] pathArray = path.split("/");

            if (pathArray.length < 3) {
                if (pathArray[1].equals("tasks")) {
                    System.out.println("Запрошен список всех задач");
                    response = gson.toJson(manager.getPrioritizedTasks());
                } else {
                    code = 404;
                    response = "Некорректный запрос!";
                }
            } else {
                switch (pathArray[2]) {
                    case "history":
                        System.out.println("Запрошена история задач");
                        response = gson.toJson(manager.getHistoryManager().getHistory());
                        break;
                    case "task":
                        System.out.println("Запрос про задачи");
                        response = taskHandler(httpExchange, method);
                        break;
                    case "epic":
                        System.out.println("Запрос про эпики");
                        response = epicHandler(httpExchange, method);
                        break;
                    case "subtask":
                        System.out.println("Запрос про подзадачи");
                        response = subtaskHandler(httpExchange, method);
                        break;
                    default:
                        response = "Некорректный запрос!";
                        code = 404;
                }
            }
            httpExchange.sendResponseHeaders(code, 0);
            send(httpExchange, response);
        }

        public String taskHandler(HttpExchange httpE, String method) throws IOException {
            String response = "";
            int id = 0;
            String query = httpE.getRequestURI().getQuery();

           if (httpE.getRequestURI().toString().contains("?")) {
                try {
                    id = Integer.parseInt(query.substring(query.lastIndexOf("=") + 1));
                } catch (Exception e) {
                    response = "Проблема в: " + query;
                }
                switch (method) {
                    case GET:
                        response = gson.toJson(manager.getTaskById(id));
                        break;
                    case DELETE:
                        manager.removeTaskById(id);
                        httpE.sendResponseHeaders(201, 0);
                        send(httpE, response);
                        break;
                    default:
                }
            } else {
               switch (method) {
                   case GET:
                       response = gson.toJson(manager.getTasks());
                       break;
                   case POST:
                       InputStream inputStream = httpE.getRequestBody();
                       String body = new String(inputStream.readAllBytes(),DEFAULT_CHARSET);
                       Task task = gson.fromJson(body, Task.class);
                       if (manager.getTaskById(task.getId()) == null)
                           manager.createTask(task);
                       else
                           manager.updateTask(task);
                       httpE.sendResponseHeaders(201, 0);
                       send(httpE, response);
                       break;
                   case DELETE:
                       manager.removeAllTasks();
                       httpE.sendResponseHeaders(201, 0);
                       send(httpE, response);
                       break;
                   default:
               }
            }

            return response;
        }

        public String epicHandler(HttpExchange httpE, String method) throws IOException {
            String response = "";
            int id = 0;
            String query = httpE.getRequestURI().getQuery();

            if (httpE.getRequestURI().toString().contains("?")) {
                try {
                    id = Integer.parseInt(query.substring(query.lastIndexOf("=") + 1));
                } catch (Exception e) {
                    response = "Проблема в: " + query;
                }
                switch (method) {
                    case GET:
                        response = gson.toJson(manager.getEpicById(id));
                        break;
                    case DELETE:
                        manager.removeEpicById(id);
                        httpE.sendResponseHeaders(201, 0);
                        send(httpE, response);
                        break;
                    default:
                }
            } else {
                switch (method) {
                    case GET:
                        response = gson.toJson(manager.getEpics());
                        break;
                    case POST:
                        InputStream inputStream = httpE.getRequestBody();
                        String body = new String(inputStream.readAllBytes(),DEFAULT_CHARSET);
                        Epic epic = gson.fromJson(body, Epic.class);
                        if (manager.getEpicById(epic.getId()) == null)
                            manager.createEpic(epic);
                        else
                            manager.updateEpic(epic);
                        httpE.sendResponseHeaders(201, 0);
                        send(httpE, response);
                        break;
                    case DELETE:
                        manager.removeEpics();
                        httpE.sendResponseHeaders(201, 0);
                        send(httpE, response);
                        break;
                    default:
                }
            }

            return response;
        }

        public String subtaskHandler(HttpExchange httpE, String method) throws IOException {
            String response = "";
            int id = 0;
            String query = httpE.getRequestURI().getQuery();

            if (httpE.getRequestURI().toString().contains("?")) {
                try {
                    id = Integer.parseInt(query.substring(query.lastIndexOf("=") + 1));
                } catch (Exception e) {
                    response = "Проблема в: " + query;
                }
                switch (method) {
                    case GET:
                        if (httpE.getRequestURI().toString().contains("epic")) {
                            response = gson.toJson(manager.getEpicSubtasks(id));
                        } else {
                            response = gson.toJson(manager.getSubtaskById(id));
                        }
                        break;
                    case DELETE:
                        manager.removeSubtaskById(id);
                        httpE.sendResponseHeaders(201, 0);
                        send(httpE, response);
                        break;
                    default:
                }
            } else {
                switch (method) {
                    case GET:
                        response = gson.toJson(manager.getAllSubtasks());
                        break;
                    case POST:
                        InputStream inputStream = httpE.getRequestBody();
                        String body = new String(inputStream.readAllBytes(),DEFAULT_CHARSET);
                        Subtask subtask = gson.fromJson(body, Subtask.class);
                        if (manager.getSubtaskById(subtask.getId()) == null)
                            manager.createSubtask(subtask);
                        else
                            manager.updateSubtask(subtask);
                        httpE.sendResponseHeaders(201, 0);
                        send(httpE, response);
                        break;
                    case DELETE:
                        manager.removeAllSubtasks();
                        httpE.sendResponseHeaders(201, 0);
                        send(httpE, response);
                        break;
                    default:
                }
            }

            return response;
        }
    }

    private void send(HttpExchange httpE, String response) throws IOException {
        try (OutputStream os = httpE.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
