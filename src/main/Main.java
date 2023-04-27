package main;

import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import service.TaskManager;
import java.util.ArrayList;

public class Main {
    private static final TaskManager taskManager = new TaskManager();

    public static void main(String[] args) {
        Task task1 = new Task("Корм", "Купить корм для хомяка", Status.NEW);
        Task task2 = new Task("Фитнес", "Сходить в спортзал", Status.NEW);
        taskManager.createNewTask(task1);
        taskManager.createNewTask(task2);

        Epic epic1 = new Epic("Переезд", "Переехать в новую квартиру");
        taskManager.createNewEpic(epic1);
        SubTask subTask1 = new SubTask(epic1.getId(), "Вещи", "Собрать вещи для переезда",
                Status.NEW);
        SubTask subTask2 = new SubTask(epic1.getId(), "Кот", "Перевезти кота в новую квартиру",
                Status.NEW);
        taskManager.createNewSubtask(subTask1);
        taskManager.createNewSubtask(subTask2);

        Epic epic2 = new Epic("Кофемашина", "Купить новую кофемашину");
        taskManager.createNewEpic(epic2);
        SubTask subTask3 = new SubTask(epic2.getId(), "Магазин", "Съездить в магазин",
                Status.IN_PROGRESS);
        taskManager.createNewSubtask(subTask3);

        printAllTasks();

        task1.setStatus(Status.IN_PROGRESS);
        task2.setStatus(Status.DONE);
        taskManager.updateTask(task1);
        taskManager.updateTask(task2);

        subTask1.setStatus(Status.IN_PROGRESS);
        subTask2.setStatus(Status.IN_PROGRESS);
        subTask3.setStatus(Status.DONE);
        taskManager.updateSubTask(subTask1);
        taskManager.updateSubTask(subTask2);
        taskManager.updateSubTask(subTask3);

        System.out.println();
        System.out.println("Задачи после изменения статусов:");
        printAllTasks();

        taskManager.removeTaskById(task1.getId());
        taskManager.removeEpicById(epic2.getId());

        System.out.println();
        System.out.println("Задачи после удаления:");
        printAllTasks();
    }

    private static void printAllTasks() {
        ArrayList<Task> tasks = taskManager.getAllTasks();
        for (Task task : tasks) {
            System.out.println(task);
        }

        ArrayList<Epic> epics = taskManager.getAllEpics();
        for (Epic epic : epics) {
            System.out.println(epic);
        }

        ArrayList<SubTask> subTasks = taskManager.getAllSubTasks();
        for (SubTask subTask : subTasks) {
            System.out.println(subTask);
        }
    }
}
