package main;

import model.*;
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
        Subtask subtask1 = new Subtask(epic1.getId(), "Вещи", "Собрать вещи для переезда",
                Status.NEW);
        Subtask subtask2 = new Subtask(epic1.getId(), "Кот", "Перевезти кота в новую квартиру",
                Status.NEW);
        taskManager.createNewSubtask(subtask1);
        taskManager.createNewSubtask(subtask2);

        Epic epic2 = new Epic("Кофемашина", "Купить новую кофемашину");
        taskManager.createNewEpic(epic2);
        Subtask subtask3 = new Subtask(epic2.getId(), "Магазин", "Съездить в магазин",
                Status.IN_PROGRESS);
        taskManager.createNewSubtask(subtask3);

        printAllTasks();

        task1.setStatus(Status.IN_PROGRESS);
        task2.setStatus(Status.DONE);
        taskManager.updateTask(task1);
        taskManager.updateTask(task2);

        subtask1.setStatus(Status.IN_PROGRESS);
        subtask2.setStatus(Status.IN_PROGRESS);
        subtask3.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        taskManager.updateSubtask(subtask3);

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

        ArrayList<Subtask> subtasks = taskManager.getAllSubtasks();
        for (Subtask subtask : subtasks) {
            System.out.println(subtask);
        }
    }
}
