package service;

import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TaskManager {
    private int newTaskId;
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, SubTask> subTasks;
    private HashMap<Integer, Epic> epics;

    public TaskManager() {
        newTaskId = 0;
        tasks = new HashMap<>();
        subTasks = new HashMap<>();
        epics = new HashMap<>();
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void removeAllTasks() {
        tasks.clear();
    }

    public void removeAllSubTasks() {
        subTasks.clear();
    }

    public void removeAllEpics() {
        epics.clear();
        subTasks.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public SubTask getSubTaskById(int id) {
        return subTasks.get(id);
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

    public void createNewSubtask(SubTask subtask) {
        int id = generateId();
        subtask.setId(id);
        subTasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubTaskId(id);
            updateEpicStatus(subtask.getEpicId());
        }
    }

    public void updateTask(Task task) {
        tasks.replace(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        epics.replace(epic.getId(), epic);
    }

    public void updateSubTask(SubTask subTask) {
        subTasks.replace(subTask.getId(), subTask);
        updateEpicStatus(subTask.getEpicId());
    }

    public void removeTaskById(int id) {
        tasks.remove(id);
    }

    public void removeEpicById(int id) {
        Epic epic = epics.get(id);

        if (epic != null) {
            HashSet<Integer> epicSubtasksIds = epics.get(id).getSubtaskIds();
            for (Integer subtaskId : epicSubtasksIds) {
                subTasks.remove(subtaskId);
            }
            epics.remove(id);
        }
    }

    public void removeSubTaskById(int id) {
        SubTask subTask = subTasks.get(id);

        if (subTask != null) {
            int epicId = subTask.getEpicId();
            subTasks.remove(id);
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.removeSubTaskId(id);
                updateEpicStatus(epicId);
            }
        }
    }

    public ArrayList<SubTask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        ArrayList<SubTask> subTasksOfEpic = new ArrayList<>();

        if (epic != null) {
            HashSet<Integer> epicSubtasksIds = epic.getSubtaskIds();
            for (Integer subtaskId : epicSubtasksIds) {
                subTasksOfEpic.add(subTasks.get(subtaskId));
            }
        }
        return subTasksOfEpic;
    }

    private int generateId() {
        return ++newTaskId;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        boolean isNew = true;
        boolean isDone = true;

        if (epic != null) {
            ArrayList<SubTask> subTasksOfEpic = getEpicSubtasks(epicId);

            if (subTasksOfEpic.isEmpty()) {
                epic.setStatus(Status.NEW);
                updateEpic(epic);
                return;
            }
            for (SubTask subTask : subTasksOfEpic) {
                if (subTask.getStatus() != Status.NEW) {
                    isNew = false;
                }
                if (subTask.getStatus() != Status.DONE) {
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
