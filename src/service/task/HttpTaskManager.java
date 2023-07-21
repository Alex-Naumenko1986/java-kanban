package service.task;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import http_client.KVTaskClient;
import model.Epic;
import model.Subtask;
import model.Task;
import service.Managers;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpTaskManager extends FileBackedTasksManager {
    private KVTaskClient client;
    private URI KVServerURI;
    private Type taskType;
    private Type epicType;
    private Type subtaskType;
    private Type idType;
    private Gson gson;

    public HttpTaskManager(URI url) {
        client = new KVTaskClient(url);
        gson = Managers.getGson();
        KVServerURI = url;
        taskType = new TypeToken<List<Task>>() {
        }.getType();
        epicType = new TypeToken<List<Epic>>() {
        }.getType();
        subtaskType = new TypeToken<List<Subtask>>() {
        }.getType();
        idType = new TypeToken<List<Integer>>() {
        }.getType();
    }

    @Override
    protected void save() {
        List<Task> tasks = new ArrayList<>(idToTask.values());
        List<Epic> epics = new ArrayList<>(idToEpic.values());
        List<Subtask> subtasks = new ArrayList<>(idToSubtask.values());
        List<Integer> historyIds = getHistoryIds();
        String tasksAsJson = gson.toJson(tasks, taskType);
        String epicsAsJson = gson.toJson(epics, epicType);
        String subtasksAsJson = gson.toJson(subtasks, subtaskType);
        String historyAsJson = gson.toJson(historyIds, idType);
        client.put("tasks", tasksAsJson);
        client.put("epics", epicsAsJson);
        client.put("subtasks", subtasksAsJson);
        client.put("history", historyAsJson);
    }

    @Override
    public void load() {
        String tasksAsJson = client.load("tasks");
        String epicsAsJson = client.load("epics");
        String subtasksAsJson = client.load("subtasks");
        String historyIdsAsJson = client.load("history");
        List<Task> tasks = gson.fromJson(tasksAsJson, taskType);
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        List<Epic> epics = gson.fromJson(epicsAsJson, epicType);
        if (epics == null) {
            epics = new ArrayList<>();
        }
        List<Subtask> subtasks = gson.fromJson(subtasksAsJson, subtaskType);
        if (subtasks == null) {
            subtasks = new ArrayList<>();
        }
        List<Integer> historyIds = gson.fromJson(historyIdsAsJson, idType);
        if (historyIds == null) {
            historyIds = new ArrayList<>();
        }

        prioritizedTasks.addAll(tasks);
        prioritizedTasks.addAll(subtasks);
        prioritizedTasks.addAll(epics);

        idToTask = tasks.stream().collect(Collectors.toMap(Task::getId, task -> task));
        idToEpic = epics.stream().collect(Collectors.toMap(Epic::getId, epic -> epic));
        idToSubtask = subtasks.stream().collect(Collectors.toMap(Subtask::getId, subtask -> subtask));
        Map<Integer, Task> idToAllTasks = new HashMap<>();
        idToAllTasks.putAll(idToTask);
        idToAllTasks.putAll(idToEpic);
        idToAllTasks.putAll(idToSubtask);
        fillHistoryWithTasks(historyIds, idToAllTasks);
    }

    private void fillHistoryWithTasks(List<Integer> historyIds, Map<Integer, Task> idToAllTasks) {
        for (Integer id : historyIds) {
            Task task = idToAllTasks.get(id);
            historyManager.add(task);
        }
    }

    private List<Integer> getHistoryIds() {
        List<Integer> historyIds = getHistory().stream().map(Task::getId).collect(Collectors.toList());
        return historyIds;
    }
}
