package service.task;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import service.Managers;
import service.history.HistoryManager;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int newTaskId;
    protected Map<Integer, Task> idToTask;
    protected Map<Integer, Subtask> idToSubtask;
    protected Map<Integer, Epic> idToEpic;
    protected HistoryManager historyManager;

    protected Set<Task> prioritizedTasks;

    public InMemoryTaskManager() {
        newTaskId = 0;
        idToTask = new HashMap<>();
        idToSubtask = new HashMap<>();
        idToEpic = new HashMap<>();
        prioritizedTasks = new TreeSet<>();
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
        prioritizedTasks.removeAll(idToTask.values());
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
                updateEpicTime(epicId);
                updateTaskInPrioritizedSet(epic);
            }
        }
        prioritizedTasks.removeAll(idToSubtask.values());
        idToSubtask.clear();
    }

    @Override
    public void removeAllEpics() {
        for (Subtask subtask : idToSubtask.values()) {
            historyManager.remove(subtask.getId());
        }
        for (Epic epic : idToEpic.values()) {
            historyManager.remove(epic.getId());
        }

        prioritizedTasks.removeAll(idToSubtask.values());
        prioritizedTasks.removeAll(idToEpic.values());

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
    public int addNewTask(Task task) {
        int id = generateId();
        task.setId(id);
        if (isIntersectsInTime(task)) {
            return -1;
        }
        ;
        prioritizedTasks.add(task);
        idToTask.put(task.getId(), task);
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        prioritizedTasks.add(epic);
        idToEpic.put(epic.getId(), epic);
        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int id = generateId();
        subtask.setId(id);
        if (isIntersectsInTime(subtask)) {
            return -1;
        }
        ;
        idToSubtask.put(subtask.getId(), subtask);
        Epic epic = idToEpic.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(id);
            updateEpicTime(subtask.getEpicId());
            updateEpicStatus(subtask.getEpicId());
        } else {
            idToSubtask.remove(subtask.getId());
            return -1;
        }
        prioritizedTasks.add(subtask);
        return id;
    }

    @Override
    public boolean updateTask(Task task) {
        if (idToTask.containsKey(task.getId())) {
            if (isIntersectsInTime(task)) {
                return false;
            }
            idToTask.replace(task.getId(), task);
            updateTaskInPrioritizedSet(task);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (idToEpic.containsKey(epic.getId())) {
            idToEpic.replace(epic.getId(), epic);
            updateTaskInPrioritizedSet(epic);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        if (idToSubtask.containsKey(subtask.getId())) {
            if (isIntersectsInTime(subtask)) {
                return false;
            }
            Epic epic = idToEpic.get(subtask.getEpicId());
            if (epic == null) {
                return false;
            }
            idToSubtask.replace(subtask.getId(), subtask);
            updateTaskInPrioritizedSet(subtask);
            updateEpicStatus(subtask.getEpicId());
            updateEpicTime(subtask.getEpicId());
            updateTaskInPrioritizedSet(epic);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean removeTask(int id) {
        if (idToTask.containsKey(id)) {
            Task task = idToTask.get(id);
            historyManager.remove(id);
            idToTask.remove(id);
            prioritizedTasks.remove(task);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeEpic(int id) {
        Epic epic = idToEpic.get(id);

        if (epic != null) {
            List<Integer> epicSubtasksIds = idToEpic.get(id).getSubtaskIds();
            for (Integer subtaskId : epicSubtasksIds) {
                Subtask subtask = idToSubtask.get(subtaskId);
                prioritizedTasks.remove(subtask);
                historyManager.remove(subtaskId);
                idToSubtask.remove(subtaskId);
            }
            prioritizedTasks.remove(epic);
            historyManager.remove(id);
            idToEpic.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeSubtask(int id) {
        Subtask subtask = idToSubtask.get(id);

        if (subtask != null) {
            int epicId = subtask.getEpicId();
            prioritizedTasks.remove(subtask);
            historyManager.remove(id);
            idToSubtask.remove(id);
            Epic epic = idToEpic.get(epicId);
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epicId);
                updateEpicTime(epicId);
                updateTaskInPrioritizedSet(epic);
            }
            return true;
        }
        return false;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
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

    @Override
    public void load() {

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

    private void updateEpicTime(int epicId) {
        Epic epic = idToEpic.get(epicId);

        if (epic != null) {
            ArrayList<Subtask> subtasksOfEpic = getEpicSubtasks(epicId);
            if (subtasksOfEpic.isEmpty()) {
                epic.setStartTime(null);
                epic.setDuration(0);
                epic.setEndTime(null);
                return;
            }

            Optional<Subtask> startTime = subtasksOfEpic.stream().
                    filter(subtask -> subtask.getStartTime() != null).min(Comparator.comparing(Subtask::getStartTime));
            startTime.ifPresent(subtask -> epic.setStartTime(subtask.getStartTime()));

            Optional<Subtask> endTime = subtasksOfEpic.stream().
                    filter(subtask -> subtask.getEndTime() != null).max(Comparator.comparing(Subtask::getEndTime));
            endTime.ifPresent(subtask -> epic.setEndTime(subtask.getEndTime()));

            int sumOfDuration = subtasksOfEpic.stream().mapToInt(Subtask::getDuration).sum();
            epic.setDuration(sumOfDuration);
        }
    }

    private void updateTaskInPrioritizedSet(Task taskToUpdate) {
        boolean isRemoved = prioritizedTasks.removeIf(task -> task.getId().equals(taskToUpdate.getId()));
        if (isRemoved) {
            prioritizedTasks.add(taskToUpdate);
        }
    }


    private boolean isIntersectsInTime(Task task) {
        for (Task prioritizedTask : prioritizedTasks) {
            if (!(prioritizedTask instanceof Epic) && !(prioritizedTask.getId().equals(task.getId())) &&
                    prioritizedTask.intersectsWith(task)) {
                return true;
            }
        }
        return false;
    }
}