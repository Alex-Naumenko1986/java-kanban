package service.task;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import service.Managers;
import service.history.HistoryManager;
import service.task.exceptions.ManagerIllegalOperationException;
import service.task.exceptions.TimeIntersectionException;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int newTaskId;
    protected HashMap<Integer, Task> idToTask;
    protected HashMap<Integer, Subtask> idToSubtask;
    protected HashMap<Integer, Epic> idToEpic;
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
        validateOnTimeIntersection(task);
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
        validateOnTimeIntersection(subtask);
        idToSubtask.put(subtask.getId(), subtask);
        Epic epic = idToEpic.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(id);
            updateEpicTime(subtask.getEpicId());
            updateEpicStatus(subtask.getEpicId());
        } else {
            throw new ManagerIllegalOperationException(String.format("Попытка добавить подзадачу с несуществующим " +
                    "эпиком, id эпика: %d", subtask.getEpicId()));
        }
        prioritizedTasks.add(subtask);
        return id;
    }

    @Override
    public void updateTask(Task task) {
        if (idToTask.containsKey(task.getId())) {
            validateOnTimeIntersection(task);
            idToTask.replace(task.getId(), task);
            updateTaskInPrioritizedSet(task);
        } else {
            throw new ManagerIllegalOperationException(String.format("Неверный идентификатор обновляемой задачи. " +
                    "Задачи с id %d не существует", task.getId()));
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (idToEpic.containsKey(epic.getId())) {
            idToEpic.replace(epic.getId(), epic);
            updateTaskInPrioritizedSet(epic);
        } else {
            throw new ManagerIllegalOperationException(String.format("Неверный идентификатор обновляемого эпика. " +
                    "Эпика с id %d не существует", epic.getId()));
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (idToSubtask.containsKey(subtask.getId())) {
            validateOnTimeIntersection(subtask);
            idToSubtask.replace(subtask.getId(), subtask);
            updateTaskInPrioritizedSet(subtask);
            Epic epic = idToEpic.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(subtask.getEpicId());
                updateEpicTime(subtask.getEpicId());
                updateTaskInPrioritizedSet(epic);
            } else {
                throw new ManagerIllegalOperationException(String.format("Попытка обновить подзадачу с " +
                        "несуществующим эпиком, id эпика: %d", subtask.getEpicId()));
            }
        } else {
            throw new ManagerIllegalOperationException(String.format("Неверный идентификатор обновляемой подзадачи. " +
                    "Подзадачи с id %d не существует", subtask.getId()));
        }
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
        } else {
            throw new ManagerIllegalOperationException(String.format("Попытка получить список подзадач для " +
                    "несуществующего эпика с id %d", epicId));
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


    private void validateOnTimeIntersection(Task task) {
        for (Task prioritizedTask : prioritizedTasks) {
            if (prioritizedTask.intersectsWith(task)) {
                throw new TimeIntersectionException(String.format("Задачи с id: %d и id: %d пересекаются " +
                        "по времени выполнения", prioritizedTask.getId(), task.getId()));
            }
        }
    }
}