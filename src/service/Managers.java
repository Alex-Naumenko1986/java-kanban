package service;

import com.google.gson.Gson;
import service.history.HistoryManager;
import service.history.InMemoryHistoryManager;
import service.task.HttpTaskManager;
import service.task.TaskManager;

import java.net.URI;

public class Managers {

    private static Gson gson;
    private static URI url = URI.create("http://localhost:8078");

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new HttpTaskManager(url);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }


}
