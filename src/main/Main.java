package main;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import service.Managers;
import service.history.HistoryManager;
import service.task.TaskManager;

import java.util.List;

public class Main {
    private static final TaskManager taskManager = Managers.getDefault();
    private static final HistoryManager historyManager = Managers.getDefaultHistory();

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
        Subtask subtask3 = new Subtask(epic1.getId(), "Мебель", "Купить мебель в новую квартиру",
                Status.NEW);
        taskManager.createNewSubtask(subtask1);
        taskManager.createNewSubtask(subtask2);
        taskManager.createNewSubtask(subtask3);

        Epic epic2 = new Epic("Кофемашина", "Купить новую кофемашину");
        taskManager.createNewEpic(epic2);

        taskManager.getTask(task1.getId());
        printHistory();
        taskManager.getTask(task2.getId());
        printHistory();
        taskManager.getEpic(epic1.getId());
        printHistory();
        taskManager.getEpic(epic2.getId());
        printHistory();
        taskManager.getSubtask(subtask1.getId());
        printHistory();
        taskManager.getSubtask(subtask2.getId());
        printHistory();
        taskManager.getSubtask(subtask3.getId());
        printHistory();
        taskManager.getTask(task1.getId());
        printHistory();
        taskManager.getEpic(epic1.getId());
        printHistory();
        taskManager.getSubtask(subtask1.getId());
        printHistory();
        taskManager.getSubtask(subtask3.getId());
        printHistory();

        taskManager.removeTask(task1.getId());
        System.out.println("История просмотров после удаления задачи 1:");
        printHistory();

        taskManager.removeEpic(epic1.getId());
        System.out.println("История просмотров после удаления эпика с тремя подзадачами:");
        printHistory();

    }

    private static void printHistory() {
        List<Task> history = historyManager.getHistory();
        for (Task task : history) {
            System.out.println(task);
        }
        System.out.println();
    }
}