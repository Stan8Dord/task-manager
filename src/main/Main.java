package main;

import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

public class Main {
    public static void main(String[] args) {
        InMemoryTaskManager manager = (InMemoryTaskManager) Managers.getDefault();
        Task task1 = manager.createTask(new Task("Задача1", "Сделать ДЗ"));
        Task task2 = manager.createTask(new Task("Задача2", "Погулять"));
        Epic epic1 = manager.createEpic(new Epic("Большая задача1", "Построить дом"));
        Epic epic2 = manager.createEpic(new Epic("Большая задача2", "Похудеть"));
        Subtask subtask = manager.createSubtask(new Subtask("Купить участок",
                "Выбрать место и купить землю", epic1.getId()));
        subtask = manager.createSubtask(new Subtask("Поставить дом",
                "Построить или купить дом", epic1.getId()));
        Subtask subtask3 = manager.createSubtask(new Subtask("Меньше есть",
                "Больше двигаться, меньше есть!", epic2.getId()));

        printAll(manager);

        manager.getSubtaskById(2);
        manager.getSubtaskById(1);
        manager.getSubtaskById(3);

        System.out.println();
        System.out.println("1. Количество задач в истории просмотров " + manager.historyManager.getHistory().size() + ":");
        for (Task task : manager.historyManager.getHistory()) {
            System.out.println(task);
        }
        System.out.println();

        task1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);
        task2.setStatus(TaskStatus.DONE);
        manager.updateTask(task2);
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);
        subtask3.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask3);

        manager.getTaskById(1);
        manager.getEpicById(2);

        System.out.println("Изменены статусы:");
        printAll(manager);

        manager.removeTaskById(1);
        manager.removeEpicById(2);
        manager.removeSubtaskById(1);
        System.out.println("Удалена часть задач.");
        printAll(manager);

        manager.getTaskById(2);
        manager.getSubtaskById(2);
        System.out.println();
        System.out.println("2. Количество задач в истории просмотров " + manager.historyManager.getHistory().size() + ":");
        for (Task task : manager.historyManager.getHistory()) {
            System.out.println(task);
        }
        System.out.println();

        manager.getTaskById(2);
        manager.getSubtaskById(2);
        manager.getTaskById(2);
        manager.getSubtaskById(2);
        manager.getTaskById(2);
        manager.getSubtaskById(2);
        manager.getTaskById(2);
        manager.getSubtaskById(2);

        System.out.println("3. Количество задач в истории просмотров " + manager.historyManager.getHistory().size() + ":");
        for (Task task : manager.historyManager.getHistory()) {
            System.out.println(task);
        }
        System.out.println();
    }

    private static void printAll (TaskManager manager) {
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


