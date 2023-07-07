package service;

import service.history.HistoryManager;
import service.history.InMemoryHistoryManager;
import service.task.FileBackedTasksManager;
import service.task.TaskManager;

import java.io.File;

public class Managers {
    private static TaskManager taskManager;
    private static HistoryManager historyManager;
    private static File file = new File("resources/task_backup.csv");

    private Managers() {
    }

    public static TaskManager getDefault() {
        taskManager = new FileBackedTasksManager(file);
        return taskManager;
    }

    public static TaskManager loadTaskManagerFromFile() {
        taskManager = FileBackedTasksManager.loadFromFile(file);
        return taskManager;
    }

    public static HistoryManager getDefaultHistory() {
        historyManager = new InMemoryHistoryManager();
        return historyManager;
    }
}
