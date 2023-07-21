package service;

import com.google.gson.Gson;
import service.history.HistoryManager;
import service.history.InMemoryHistoryManager;
import service.task.HttpTaskManager;
import service.task.TaskManager;

import java.net.URI;

public class Managers {
    private static TaskManager taskManager;
    private static HistoryManager historyManager;

    private static Gson gson;
    private static URI url = URI.create("http://localhost:8078");

    private Managers() {
    }

    public static TaskManager getDefault() {
        taskManager = new HttpTaskManager(url);
        return taskManager;
    }

    public static HistoryManager getDefaultHistory() {
        historyManager = new InMemoryHistoryManager();
        return historyManager;
    }

    public static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }


}
