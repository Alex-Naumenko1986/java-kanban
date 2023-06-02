package service.task;

import model.*;
import service.Managers;
import service.history.HistoryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private int newTaskId;
    private HashMap<Integer, Task> idToTask;
    private HashMap<Integer, Subtask> idToSubtask;
    private HashMap<Integer, Epic> idToEpic;
    private HistoryManager historyManager;

    public InMemoryTaskManager() {
        newTaskId = 0;
        idToTask = new HashMap<>();
        idToSubtask = new HashMap<>();
        idToEpic = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(idToTask.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(idToSubtask.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(idToEpic.values());
    }

    @Override
    public void removeAllTasks() {
        for (Task task : idToTask.values()) {
            historyManager.remove(task.getId());
        }
        idToTask.clear();
    }

    @Override
    public void removeAllSubtasks() {
        for (Subtask subtask : idToSubtask.values()) {
            historyManager.remove(subtask.getId());
            int epicId = subtask.getEpicId();
            Epic epic = idToEpic.get(epicId);
            if (epic != null) {
                epic.removeAllSubtaskIds();
                updateEpicStatus(epicId);
            }
        }
        idToSubtask.clear();
    }

    @Override
    public void removeAllEpics() {
        for (Subtask subtask : idToSubtask.values()) {
            historyManager.remove(subtask.getId());
        }
        for (Epic epic: idToEpic.values()) {
            historyManager.remove(epic.getId());
        }
        idToEpic.clear();
        idToSubtask.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = idToTask.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = idToSubtask.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = idToEpic.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public void createNewTask(Task task) {
        int id = generateId();
        task.setId(id);
        idToTask.put(task.getId(), task);
    }

    @Override
    public void createNewEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        idToEpic.put(epic.getId(), epic);
    }

    @Override
    public void createNewSubtask(Subtask subtask) {
        int id = generateId();
        subtask.setId(id);
        idToSubtask.put(subtask.getId(), subtask);
        Epic epic = idToEpic.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(id);
            updateEpicStatus(subtask.getEpicId());
        }
    }

    @Override
    public void updateTask(Task task) {
        if (idToTask.containsKey(task.getId())) {
            idToTask.replace(task.getId(), task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (idToEpic.containsKey(epic.getId())) {
            idToEpic.replace(epic.getId(), epic);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (idToSubtask.containsKey(subtask.getId())) {
            idToSubtask.replace(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
        }
    }

    @Override
    public void removeTask(int id) {
        historyManager.remove(id);
        idToTask.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = idToEpic.get(id);

        if (epic != null) {
            List<Integer> epicSubtasksIds = idToEpic.get(id).getSubtaskIds();
            for (Integer subtaskId : epicSubtasksIds) {
                historyManager.remove(subtaskId);
                idToSubtask.remove(subtaskId);
            }
            historyManager.remove(id);
            idToEpic.remove(id);
        }
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = idToSubtask.get(id);

        if (subtask != null) {
            int epicId = subtask.getEpicId();
            historyManager.remove(id);
            idToSubtask.remove(id);
            Epic epic = idToEpic.get(epicId);
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epicId);
            }
        }
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = idToEpic.get(epicId);
        ArrayList<Subtask> subtasksOfEpic = new ArrayList<>();

        if (epic != null) {
            List<Integer> epicSubtasksIds = epic.getSubtaskIds();
            for (Integer subtaskId : epicSubtasksIds) {
                subtasksOfEpic.add(idToSubtask.get(subtaskId));
            }
        }
        return subtasksOfEpic;
    }

    private int generateId() {
        return ++newTaskId;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = idToEpic.get(epicId);
        boolean isNew = true;
        boolean isDone = true;

        if (epic != null) {
            ArrayList<Subtask> subtasksOfEpic = getEpicSubtasks(epicId);

            if (subtasksOfEpic.isEmpty()) {
                epic.setStatus(Status.NEW);
                updateEpic(epic);
                return;
            }
            for (Subtask subtask : subtasksOfEpic) {
                if (subtask.getStatus() != Status.NEW) {
                    isNew = false;
                }
                if (subtask.getStatus() != Status.DONE) {
                    isDone = false;
                }
            }
            if (isNew) {
                epic.setStatus(Status.NEW);
                updateEpic(epic);
                return;
            }
            if (isDone) {
                epic.setStatus(Status.DONE);
                updateEpic(epic);
                return;
            }
            epic.setStatus(Status.IN_PROGRESS);
            updateEpic(epic);
        }
    }
}
