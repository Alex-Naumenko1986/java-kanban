package main;

import http_client.KVTaskClient;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import server.KVServer;
import service.Managers;
import service.task.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        KVServer kvServer = new KVServer();
        kvServer.start();
        KVTaskClient taskClient = new KVTaskClient(URI.create("http://localhost:8078"));
        taskClient.put("key1", "key1 value");
        taskClient.put("key2", "key2 value");
        System.out.println(taskClient.load("key1"));
        System.out.println(taskClient.load("key2"));
        taskClient.put("key1", "key1 new value");
        System.out.println(taskClient.load("key1"));

        TaskManager taskManager = Managers.getDefault();
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 0), 40, Status.NEW);
        Task task2 = new Task("Test task2", "Test task2 description",
                LocalDateTime.of(2023, 6, 30, 12, 0), 60, Status.NEW);
        int task1Id = taskManager.addNewTask(task1);
        int task2Id = taskManager.addNewTask(task2);

        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        Epic epic2 = new Epic("Test epic2", "Test epic2 description");
        int epic1Id = taskManager.addNewEpic(epic1);
        int epic2Id = taskManager.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("Test subtask1", "Test subtask1 description",
                LocalDateTime.of(2023, 7, 4, 17, 30), 40,
                Status.NEW);
        Subtask subtask2 = new Subtask("Test subtask2", "Test subtask2 description",
                LocalDateTime.of(2023, 7, 3, 14, 30), 60,
                Status.NEW);
        subtask1.setEpicId(epic1Id);
        subtask2.setEpicId(epic1Id);

        int subtask1Id = taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        taskManager.getTask(task2Id);
        taskManager.getTask(task1Id);
        taskManager.getEpic(epic1Id);
        taskManager.getSubtask(subtask1Id);
        taskManager.getEpic(epic2Id);

        System.out.println("Список задач до восстановления данных с сервера:");
        printTasks(taskManager.getAllTasks());
        printEpics(taskManager.getAllEpics());
        printSubtasks(taskManager.getAllSubtasks());
        System.out.println();

        System.out.println("Список задач по приоритету до восстановления данных с сервера:");
        printTasks(taskManager.getPrioritizedTasks());
        System.out.println();

        System.out.println("История просмотра задач до восстановления данных с сервера:");
        printTasks(taskManager.getHistory());
        System.out.println();

        taskManager.load();

        System.out.println("Список задач после восстановления данных с сервера:");
        printTasks(taskManager.getAllTasks());
        printEpics(taskManager.getAllEpics());
        printSubtasks(taskManager.getAllSubtasks());
        System.out.println();

        System.out.println("Список задач по приоритету после восстановления данных с сервера:");
        printTasks(taskManager.getPrioritizedTasks());
        System.out.println();

        System.out.println("История просмотра задач после восстановления данных с сервера:");
        printTasks(taskManager.getHistory());
        System.out.println();

        kvServer.stop();
    }

    private static void printSubtasks(ArrayList<Subtask> subtasks) {
        for (Subtask subtask : subtasks) {
            System.out.println(subtask);
        }
    }

    private static void printEpics(ArrayList<Epic> epics) {
        for (Epic epic : epics) {
            System.out.println(epic);
        }
    }

    public static void printTasks(List<Task> tasks) {
        for (Task task : tasks) {
            System.out.println(task);
        }
    }
}