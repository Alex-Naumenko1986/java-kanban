package main;

import model.*;
import service.*;
import java.util.List;

public class Main {
    private static final TaskManager taskManager = Managers.getDefault();
    private static final HistoryManager historyManager = Managers.getDefaultHistory();

    public static void main(String[] args) {
        Task task1 = new Task("Корм", "Купить корм для хомяка", Status.NEW);
        Task task2 = new Task("Фитнес", "Сходить в спортзал", Status.NEW);
        Task task3 = new Task("Окно", "Помыть окно в гостиной", Status.NEW);
        taskManager.createNewTask(task1);
        taskManager.createNewTask(task2);
        taskManager.createNewTask(task3);

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
        Subtask subtask3 = new Subtask(epic2.getId(), "Деньги", "Накопить деньги на кофемашину",
                Status.NEW);
        Subtask subtask4 = new Subtask(epic2.getId(), "Магазин", "Съездить в магазин",
                Status.NEW);
        taskManager.createNewSubtask(subtask3);
        taskManager.createNewSubtask(subtask4);

        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getTask(task3.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());
        taskManager.getSubtask(subtask3.getId());
        taskManager.getSubtask(subtask4.getId());
        taskManager.getTask(task1.getId());
        taskManager.getTask(task2.getId());
        List<Task> history = historyManager.getHistory();

        System.out.println("История просмотров после вызова методов getTask(), getSubtask() и getEpic():");
        printHistory(history);

        taskManager.getAllTasks();
        taskManager.getAllSubtasks();
        taskManager.getAllEpics();

        task1.setStatus(Status.IN_PROGRESS);
        subtask1.setStatus(Status.IN_PROGRESS);
        epic1.setName("Новая квартира");
        taskManager.updateTask(task1);
        taskManager.updateSubtask(subtask1);
        taskManager.updateEpic(epic1);

        taskManager.removeTask(task2.getId());
        taskManager.removeSubtask(subtask2.getId());
        taskManager.removeEpic(epic2.getId());

        taskManager.getEpicSubtasks(epic1.getId());

        List<Task> history2 = historyManager.getHistory();

        System.out.println();
        System.out.println("История просмотров после вызова других методов TaskManager:");
        printHistory(history2);

        taskManager.removeAllTasks();
        taskManager.removeAllSubtasks();
        taskManager.removeAllEpics();

        List<Task> history3 = historyManager.getHistory();

        System.out.println();
        System.out.println("История просмотров после удаления всех задач:");
        printHistory(history3);
    }

    private static void printHistory(List<Task> history) {
        for (Task task : history) {
            System.out.println(task);
        }
    }
}
