package tests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import manager.HTTPTaskManager;
import manager.Managers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import servers.HttpTaskServer;
import servers.KVServer;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerTest {
    private KVServer kvServer;
    private HttpTaskServer taskServer;
    private static HttpClient testClient;
    private static Gson gson;
    private HTTPTaskManager newManager;

    @BeforeAll
    public static void beforeAll() {
        testClient = HttpClient.newHttpClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class, new HTTPTaskManager.ZonedDateTimeAdapter())
                .create();
    }

    @BeforeEach
    public void beforeEach() {
        try {
            kvServer = new KVServer();
            kvServer.start();
            newManager = (HTTPTaskManager) Managers.getDefault(3,null);

            newManager.createTask(new Task("Задача1", "Сделать ДЗ", 10));
            newManager.createTask(new Task("Задача2", "Вторая", 10));
            Epic epic1 = newManager.createEpic(new Epic("Большая задача 1", "И две подзадачи"));
            newManager.createEpic(new Epic("Эпик 2", "Эпичный эпик"));
            newManager.createSubtask(new Subtask("Тема",
                    "подзадача 1", epic1.getId(), 10));
            newManager.createSubtask(new Subtask("Сабтаск 2",
                    "Подзадача 2", epic1.getId(), 10));
            newManager.getSubtaskById(12);
            newManager.getTaskById(10);
            newManager.getEpicById(11);
            newManager.getTaskById(10);
            newManager.getSubtaskById(12);

            taskServer = new HttpTaskServer(newManager);
            taskServer.start();
        } catch (IOException e) {
            System.out.println("Проблема с запуском сервера.");
        }
    }

    @AfterEach
    public void afterEach() {
        taskServer.stop();
        kvServer.stop();
    }

    @Test
    public void shouldLoadAllTasks() {
        HttpRequest request;
        URI url;
        HttpResponse<String> response;

        url = URI.create("http://localhost:8080/tasks/");
        request = HttpRequest.newBuilder().uri(url).GET().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 200);
            assertTrue(response.body().contains("Задача1"));
            assertTrue(response.body().contains("Большая задача 1"));
            assertTrue(response.body().contains("Подзадача 2"));
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест GET All Tasks не пройден!");
        }
    }

    @Test
    public void shouldLoadHistory() {
        HttpRequest request;
        URI url;
        HttpResponse<String> response;

        url = URI.create("http://localhost:8080/tasks/history");
        request = HttpRequest.newBuilder().uri(url).GET().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 200);
            assertTrue(response.body().contains("\"id\":11"));
            assertTrue(!response.body().contains("Подзадача 2"));
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест History не пройден!");
        }
    }

    @Test
    public void shouldWorkWithTaskRequestsGet() {
        HttpRequest request;
        URI url;
        HttpResponse<String> response;

        url = URI.create("http://localhost:8080/tasks/task/");
        request = HttpRequest.newBuilder().uri(url).GET().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 200);
            assertTrue(response.body().contains("Задача2"));
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест GET не пройден!");
        }

        url = URI.create("http://localhost:8080/tasks/task/?id=10");
        request = HttpRequest.newBuilder().uri(url).GET().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 200);
            assertTrue(response.body().contains("\"id\":10"));
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест GET ID не пройден!");
        }
    }

    @Test
    public void shouldWorkWithTaskRequestsPost() {
        HttpRequest request;
        URI url;
        HttpResponse<String> response;

        Task task3 = new Task("Задача3", "Третья", 10);
        url = URI.create("http://localhost:8080/tasks/task/");
        String json = gson.toJson(task3);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(body).build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 201);
            assertTrue(newManager.getTasks().size() == 3);
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест POST не пройден! Ошибка = " + e.getMessage());
        }
    }

    @Test
    public void shouldWorkWithTaskRequestsDelete() {
        HttpRequest request;
        URI url;
        HttpResponse<String> response;

        url = URI.create("http://localhost:8080/tasks/task/?id=10");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 201);
            assertTrue(newManager.getTasks().size() == 1);
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест DELETE ID не пройден!");
        }

        url = URI.create("http://localhost:8080/tasks/task/");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 201);
            assertTrue(newManager.getTasks().size() == 0);
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест DELETE ID не пройден!");
        }
    }

    @Test
    public void shouldWorkWithEpicRequestsGet() {
        HttpRequest request;
        URI url;
        HttpResponse<String> response;

        url = URI.create("http://localhost:8080/tasks/epic/");
        request = HttpRequest.newBuilder().uri(url).GET().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 200);
            assertTrue(response.body().contains("Эпичный эпик"));
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест GET не пройден!");
        }

        url = URI.create("http://localhost:8080/tasks/epic/?id=11");
        request = HttpRequest.newBuilder().uri(url).GET().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 200);
            assertTrue(response.body().contains("\"id\":11"));
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест GET ID не пройден!");
        }
    }

    @Test
    public void shouldWorkWithEpicRequestsPost() {
        HttpRequest request;
        URI url;
        HttpResponse<String> response;

        Epic epic3 = new Epic("Эпик 3", "Эпопея");
        url = URI.create("http://localhost:8080/tasks/epic/");
        String json = gson.toJson(epic3);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(body).build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 201);
            assertTrue(newManager.getEpics().size() == 3);
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест POST не пройден! Ошибка = " + e.getMessage());
        }
    }

    @Test
    public void shouldWorkWithEpicRequestsDelete() {
        HttpRequest request;
        URI url;
        HttpResponse<String> response;

        url = URI.create("http://localhost:8080/tasks/epic/?id=21");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 201);
            assertTrue(newManager.getEpics().size() == 1);
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест DELETE ID не пройден!");
        }

        url = URI.create("http://localhost:8080/tasks/epic/");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 201);
            assertTrue(newManager.getEpics().size() == 0);
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест DELETE ID не пройден!");
        }
    }

    @Test
    public void shouldWorkWithSubtaskRequestsGet() {
        HttpRequest request;
        URI url;
        HttpResponse<String> response;

        url = URI.create("http://localhost:8080/tasks/subtask/");
        request = HttpRequest.newBuilder().uri(url).GET().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 200);
            assertTrue(response.body().contains("Сабтаск 2"));
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест GET не пройден! Ошибка = " + e.getMessage());
        }

        url = URI.create("http://localhost:8080/tasks/subtask/?id=12");
        request = HttpRequest.newBuilder().uri(url).GET().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 200);
            assertTrue(response.body().contains("\"id\":12"));
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест GET ID не пройден! Ошибка = " + e.getMessage());
        }
    }

    @Test
    public void shouldWorkWithSubtaskRequestsPost() {
        HttpRequest request;
        URI url;
        HttpResponse<String> response;

        Subtask subtask3 = new Subtask("Подзадача три", "подзадача 3", 11, 10);
        url = URI.create("http://localhost:8080/tasks/subtask/");
        String json = gson.toJson(subtask3);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(body).build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 201);
            assertTrue(newManager.getAllSubtasks().size() == 3);
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест POST не пройден! Ошибка = " + e.getMessage());
        }
    }

    @Test
    public void shouldWorkWithSubtaskRequestsDelete() {
        HttpRequest request;
        URI url;
        HttpResponse<String> response;

        url = URI.create("http://localhost:8080/tasks/subtask/?id=12");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 201);
            assertTrue(newManager.getAllSubtasks().size() == 1);
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест DELETE ID не пройден! Ошибка = " + e.getMessage());
        }

        url = URI.create("http://localhost:8080/tasks/subtask/");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 201);
            assertTrue(newManager.getAllSubtasks().size() == 0);
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест DELETE ID не пройден! Ошибка = " + e.getMessage());
        }
    }

    @Test
    public void shouldWorkWithSubtaskRequestsByEpic() {
        HttpRequest request;
        URI url;
        HttpResponse<String> response;

        url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=11");
        request = HttpRequest.newBuilder().uri(url).GET().build();

        try {
            response = testClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.statusCode(), 200);
            assertTrue(response.body().contains("Сабтаск 2"));
            assertTrue(response.body().contains("подзадача 1"));
        } catch (IOException | InterruptedException e) {
            System.out.println("Тест GET epicSubtasks не пройден! Ошибка = " + e.getMessage());
        }
    }
}
