import tasks.Task;
import tasks.Epic;
import tasks.Subtask;
import manager.Manager;

public class Main {
    public static void main(String[] args) {
        Manager manager = new Manager();
        Task task1 = manager.createTask(new Task("Задача1", "Сделать ДЗ", "NEW"));
        Task task2 = manager.createTask(new Task("Задача2", "Погулять", "NEW"));
        Epic epic1 = manager.createEpic(new Epic("Большая задача1", "Построить дом", "NEW"));
        Epic epic2 = manager.createEpic(new Epic("Большая задача2", "Похудеть", "NEW"));
        Subtask subtask = manager.createSubtask(new Subtask("Купить участок",
                "Выбрать место и купить землю", "NEW", epic1.getId()));
        subtask = manager.createSubtask(new Subtask("Поставить дом",
                "Построить или купить дом", "NEW", epic1.getId()));
        Subtask subtask3 = manager.createSubtask(new Subtask("Меньше есть",
                "Больше двигаться, меньше есть!", "NEW", epic2.getId()));

        printAll(manager);

        task1.setStatus("IN_PROGRESS");
        manager.updateTask(task1);
        task2.setStatus("DONE");
        manager.updateTask(task2);
        subtask.setStatus("DONE");
        manager.updateSubtask(subtask);
        subtask3.setStatus("IN_PROGRESS");
        manager.updateSubtask(subtask3);

        System.out.println("Изменены статусы:");
        printAll(manager);

        manager.removeTaskById(1);
        manager.removeEpicById(2);
        manager.removeSubtaskById(1);
        System.out.println("Удалена часть задач.");
        printAll(manager);
    }

    private static void printAll (Manager manager) {
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


