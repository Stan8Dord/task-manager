package main;

import manager.*;
import servers.KVServer;
import customexceptions.ManagerSaveException;
import servers.HttpTaskServer;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        final Scanner scanner = new Scanner(System.in);
        int mode = 3;

        System.out.println("Выберите режим работы менеджера задач:");
        System.out.println("1 - в памяти");
        System.out.println("2 - из файла");
        System.out.println("3 - на сервере");
        System.out.println("По умолчанию - на сервере. Введите число:");
        try {
            //mode = scanner.nextInt();
            if (mode != 1 || mode != 2)
                mode = 3;
        } catch (InputMismatchException exp) {
            System.out.println("Значит на сервере.");
            mode = 3;
        }

        if (mode == 1) {
            InMemoryTaskManager manager = (InMemoryTaskManager) Managers.getDefault(mode, null);
            Task task1 = manager.createTask(new Task("Задача1", "Сделать ДЗ", 60 * 24));
            Task task2 = manager.createTask(new Task("Задача2", "Погулять", 60L));
            Epic epic1 = manager.createEpic(new Epic("Большая задача без подзадач", "Построить дом"));
            Epic epic2 = manager.createEpic(new Epic("Большая задача с 3мя подзадачами", "Похудеть"));

            Subtask subtask1 = manager.createSubtask(new Subtask("Меньше есть",
                    "Меньше есть сладкого и мучного!", epic2.getId(), 0));
            Subtask subtask2 = manager.createSubtask(new Subtask("Больше двигаться",
                    "Бегать, прыгать, плавать!", epic2.getId(), 0));
            Subtask subtask3 = manager.createSubtask(new Subtask("Ходить на фитнес",
                    "Надо подкачаться!", epic2.getId(), 0));

            printAll(manager);

            manager.getSubtaskById(22);
            manager.getSubtaskById(12);
            manager.getSubtaskById(32);
            manager.getTaskById(10);
            manager.getSubtaskById(12);
            manager.getSubtaskById(32);
            manager.getSubtaskById(22);

            System.out.println();
            System.out.println("1. Количество задач в истории просмотров "
                    + manager.getHistoryManager().getHistory().size() + ":");
            for (Task task : manager.getHistoryManager().getHistory()) {
                System.out.println(task);
            }
            System.out.println();

            task1.setStatus(TaskStatus.IN_PROGRESS);
            manager.updateTask(task1);
            task2.setStatus(TaskStatus.DONE);
            manager.updateTask(task2);
            subtask1.setStatus(TaskStatus.DONE);
            manager.updateSubtask(subtask1);
            subtask3.setStatus(TaskStatus.IN_PROGRESS);
            manager.updateSubtask(subtask3);

            manager.getTaskById(10);
            manager.getEpicById(21);
            manager.removeTaskById(10);
            manager.getTaskById(20);
            manager.getSubtaskById(22);

            System.out.println("2. (удалена задача 10) Количество задач в истории просмотров "
                    + manager.getHistoryManager().getHistory().size() + ":");
            for (Task task : manager.getHistoryManager().getHistory()) {
                System.out.println(task);
            }
            System.out.println();

            manager.getTaskById(20);
            manager.getSubtaskById(22);
            manager.getTaskById(20);
            manager.getSubtaskById(22);
            manager.getTaskById(20);
            manager.getEpicById(11);
            manager.getSubtaskById(22);
            manager.getTaskById(20);

            manager.removeEpicById(21);

            System.out.println("Удалена часть задач.");
            printAll(manager);

            System.out.println();
            System.out.println("3. (удален эпик с 3мя подзадачами) Количество задач в истории просмотров "
                    + manager.getHistoryManager().getHistory().size() + ":");
            for (Task task : manager.getHistoryManager().getHistory()) {
                System.out.println(task);
            }
            System.out.println();
        } else if (mode == 2) {
            File theFile = new File("src\\data", "datafile.csv");
            if (!theFile.exists()) {
                try {
                    if(theFile.createNewFile())
                        System.out.println("Файл создан успешно.");
                } catch (IOException e) {
                    throw new ManagerSaveException("Проблема с созданием файла.");
                }
            }

            FileBackedTasksManager manager = (FileBackedTasksManager) Managers.getDefault(2, theFile);
            Task task1 = manager.createTask(new Task("Задача1", "Сделать ДЗ", 10));
            Task task2 = manager.createTask(new Task("Задача2", "Погулять", 60L));
            Epic epic1 = manager.createEpic(new Epic("Большая задача без подзадач", "Закончить курс"));
            Epic epic2 = manager.createEpic(new Epic("Большая задача с 3мя подзадачами", "Создать приложение"));

            Subtask subtask1 = manager.createSubtask(new Subtask("Тема",
                    "Определиться с темой", epic2.getId(), 10));
            Subtask subtask2 = manager.createSubtask(new Subtask("Технологии",
                    "Продумать архитектуру", epic2.getId(), 10));
            Subtask subtask3 = manager.createSubtask(new Subtask("Код",
                    "Реализовать задуманное", epic2.getId(), 10));

            Subtask subtask5 = manager.createSubtask(new Subtask("Тема1",
                    "Определиться1", epic1.getId(), 10));
            Subtask subtask4 = manager.createSubtask(new Subtask("Технологии2",
                    "Продумать3", epic1.getId(), 10));

            System.out.println("\n1. Отсортированные задачи = " + manager.getPrioritizedTasks().size() + "шт.:");
            for (Task task : manager.getPrioritizedTasks()) {
                System.out.println(task);
            }
            System.out.println("");

            task1.setStatus(TaskStatus.IN_PROGRESS);
            manager.updateTask(task1);
            task2.setStatus(TaskStatus.DONE);
            manager.updateTask(task2);
            subtask1.setStatus(TaskStatus.DONE);
            manager.updateSubtask(subtask1);
            subtask3.setStatus(TaskStatus.IN_PROGRESS);
            manager.updateSubtask(subtask3);

            manager.getSubtaskById(22);
            manager.getSubtaskById(12);
            manager.getSubtaskById(32);
            manager.getTaskById(10);
            manager.getSubtaskById(12);
            manager.getTaskById(10);
            manager.getEpicById(21);
            manager.removeTaskById(10);
            manager.getTaskById(20);

            FileBackedTasksManager managerFromFile = FileBackedTasksManager.loadFromFile(theFile);

            System.out.println("Изначальный менеджер:");
            printAll(manager);
            System.out.println("======================");
            System.out.println("Восстановленный менеджер:");
            printAll(managerFromFile);

            System.out.println();
            System.out.println("История");
            System.out.println("Было в первом менеджере. Количество задач в истории просмотров "
                    + manager.getHistoryManager().getHistory().size() + ":");
            for (Task task : manager.getHistoryManager().getHistory()) {
                System.out.println(task);
            }
            System.out.println("======================");
            System.out.println("В восстановленном менеджере. Количество задач в истории просмотров "
                    + managerFromFile.getHistoryManager().getHistory().size() + ":");
            for (Task task : managerFromFile.getHistoryManager().getHistory()) {
                System.out.println(task);
            }

            System.out.println("\nОтсортированные задачи:");
            for (Task task : manager.getPrioritizedTasks()) {
                System.out.println(task);
            }
            System.out.println("");
            System.out.println("\nОтсортированные задачи менджера из файла:");
            for (Task task : managerFromFile.getPrioritizedTasks()) {
                System.out.println(task);
            }
        } else {
            KVServer kvServer = new KVServer();
            kvServer.start();
            HTTPTaskManager newManager = (HTTPTaskManager) Managers.getDefault(3,null);
            HttpTaskServer taskServer = new HttpTaskServer(newManager);
            taskServer.start();

            Task task1 = taskServer.getManager().createTask(new Task("Задача1", "Сделать ДЗ", 10));
            Task task2 = taskServer.getManager().createTask(new Task("Задача2", "Вторая", 10));

            Epic epic1 = taskServer.getManager().createEpic(new Epic("Большая задача 1", "Закончить курс"));
            Subtask subtask1 = taskServer.getManager().createSubtask(new Subtask("Тема",
                    "Определиться с темой", epic1.getId(), 10));
            Subtask subtask2 = taskServer.getManager().createSubtask(new Subtask("SDfsd22222",
                    "Оsssss 112222 с темой", epic1.getId(), 10));

            taskServer.getManager().getSubtaskById(12);
            taskServer.getManager().getTaskById(10);
            taskServer.getManager().getEpicById(11);
            taskServer.getManager().getTaskById(10);
            taskServer.getManager().getSubtaskById(12);

            HTTPTaskManager managerFromKVServer = HTTPTaskManager.load(URI.create("http://localhost:8078/"));

            System.out.println("Изначальный менеджер:");
            printAll(taskServer.getManager());
            System.out.println("======================");
            System.out.println("Восстановленный менеджер:");
            printAll(managerFromKVServer);

            System.out.println();
            System.out.println("История");
            System.out.println("Было в первом менеджере. Количество задач в истории просмотров "
                    + taskServer.getManager().getHistoryManager().getHistory().size() + ":");
            for (Task task : taskServer.getManager().getHistoryManager().getHistory()) {
                System.out.println(task);
            }
            System.out.println("======================");
            System.out.println("В восстановленном менеджере. Количество задач в истории просмотров "
                    + managerFromKVServer.getHistoryManager().getHistory().size() + ":");
            for (Task task : managerFromKVServer.getHistoryManager().getHistory()) {
                System.out.println(task);
            }

            System.out.println("\nОтсортированные задачи:");
            for (Task task : taskServer.getManager().getPrioritizedTasks()) {
                System.out.println(task);
            }
            System.out.println("\nОтсортированные задачи менджера из файла:");
            for (Task task : managerFromKVServer.getPrioritizedTasks()) {
                System.out.println(task);
            }

            taskServer.stop();
            kvServer.stop();
        }
    }

    public static void printAll(TaskManager manager) {
        System.out.println("Список задач:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Список Epic-задач:");
        for (Epic epic : manager.getEpics()) {
            System.out.println(epic);
        }
        System.out.println("Список подзадач:");
        for (Subtask sub : manager.getAllSubtasks()) {
            System.out.println(sub);
        }
    }
}