package service;

import model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private int newTaskId;
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Subtask> subtasks;
    private HashMap<Integer, Epic> epics;

    public TaskManager() {
        newTaskId = 0;
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void removeAllTasks() {
        tasks.clear();
    }

    public void removeAllSubtasks() {
        subtasks.clear();
    }

    public void removeAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void createNewTask(Task task) {
        int id = generateId();
        task.setId(id);
        tasks.put(task.getId(), task);
    }

    public void createNewEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        epics.put(epic.getId(), epic);
    }

    public void createNewSubtask(Subtask subtask) {
        int id = generateId();
        subtask.setId(id);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(id);
            updateEpicStatus(subtask.getEpicId());
        }
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.replace(task.getId(), task);
        }
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            epics.replace(epic.getId(), epic);
        }
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            subtasks.replace(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
        }
    }

    public void removeTaskById(int id) {
        tasks.remove(id);
    }

    public void removeEpicById(int id) {
        Epic epic = epics.get(id);

        if (epic != null) {
            List<Integer> epicSubtasksIds = epics.get(id).getSubtaskIds();
            for (Integer subtaskId : epicSubtasksIds) {
                subtasks.remove(subtaskId);
            }
            epics.remove(id);
        }
    }

    public void removeSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);

        if (subtask != null) {
            int epicId = subtask.getEpicId();
            subtasks.remove(id);
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epicId);
            }
        }
    }

    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        ArrayList<Subtask> subtasksOfEpic = new ArrayList<>();

        if (epic != null) {
            List<Integer> epicSubtasksIds = epic.getSubtaskIds();
            for (Integer subtaskId : epicSubtasksIds) {
                subtasksOfEpic.add(subtasks.get(subtaskId));
            }
        }
        return subtasksOfEpic;
    }

    private int generateId() {
        return ++newTaskId;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
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
