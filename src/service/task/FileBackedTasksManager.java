package service.task;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import service.Managers;
import service.history.HistoryManager;
import service.task.exceptions.ManagerSaveException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private File fileForSaving;

    public FileBackedTasksManager(File fileForSaving) {
        super();
        this.fileForSaving = fileForSaving;
        save();
    }

    public FileBackedTasksManager() {
        super();
    }

    public FileBackedTasksManager(HistoryManager historyManager, HashMap<Integer, Task> idToTask, HashMap<Integer,
            Epic> idToEpic, HashMap<Integer, Subtask> idToSubtask, Set<Task> prioritizedTasks, File fileForSaving) {
        super();
        this.historyManager = historyManager;
        this.idToTask = idToTask;
        this.idToSubtask = idToSubtask;
        this.idToEpic = idToEpic;
        this.prioritizedTasks = prioritizedTasks;
        this.fileForSaving = fileForSaving;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean isUpdated = super.updateTask(task);
        save();
        return isUpdated;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean isUpdated = super.updateEpic(epic);
        save();
        return isUpdated;
    }

    @Override
    public boolean updateSubtask(Subtask subtask) {
        boolean isUpdated = super.updateSubtask(subtask);
        save();
        return isUpdated;
    }

    @Override
    public boolean removeTask(int id) {
        boolean isRemoved = super.removeTask(id);
        save();
        return isRemoved;
    }

    @Override
    public boolean removeEpic(int id) {
        boolean isRemoved = super.removeEpic(id);
        save();
        return isRemoved;
    }

    @Override
    public boolean removeSubtask(int id) {
        boolean isRemoved = super.removeSubtask(id);
        save();
        return isRemoved;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        String content;
        HashMap<Integer, Task> idToTask = new HashMap<>();
        HashMap<Integer, Epic> idToEpic = new HashMap<>();
        HashMap<Integer, Subtask> idToSubtask = new HashMap<>();
        Set<Task> prioritizedTasks = new TreeSet<>();

        try {
            content = Files.readString(Path.of(file.getPath()));
        } catch (IOException e) {
            throw new ManagerSaveException(String.format("Ошибка при чтении из файла: %s" +
                    ", произошло исключение: %s", file.getPath(), e));
        }

        validateContentsOfFile(file, content);

        String[] lines = content.split("\n");
        int indexOfEmptyLine = findIndexOfEmptyLine(lines);

        for (int i = 1; i < indexOfEmptyLine; i++) {
            Task task = fromString(lines[i]);
            if (task instanceof Epic) {
                idToEpic.put(task.getId(), (Epic) task);
            } else if (task instanceof Subtask) {
                Subtask subtask = (Subtask) task;
                idToSubtask.put(task.getId(), subtask);
                addSubtaskIdToEpic(subtask, idToEpic.get(subtask.getEpicId()));
            } else if (task != null) {
                idToTask.put(task.getId(), task);
            }
        }
        List<Integer> historyIds = historyFromString(lines[indexOfEmptyLine + 1]);
        HistoryManager historyManager = Managers.getDefaultHistory();
        HashMap<Integer, Task> idToTaskForHistory = new HashMap<>();

        idToTaskForHistory.putAll(idToTask);
        idToTaskForHistory.putAll(idToEpic);
        idToTaskForHistory.putAll(idToSubtask);

        prioritizedTasks.addAll(idToTaskForHistory.values());

        for (Integer id : historyIds) {
            Task task = idToTaskForHistory.get(id);
            if (task != null) {
                historyManager.add(task);
            }
        }
        return new FileBackedTasksManager(historyManager, idToTask, idToEpic, idToSubtask, prioritizedTasks, file);
    }

    private static void validateContentsOfFile(File file, String content) {
        String[] lines = content.split("\n");
        int minLines = 3;
        if (lines.length < minLines) {
            throw new ManagerSaveException(String.format("Неверный формат файла: %s. Количество строк в файле" +
                    " должно быть не менее %d", file.getPath(), minLines));
        }

        int indexOfEmptyLine = findIndexOfEmptyLine(lines);
        if (indexOfEmptyLine == -1) {
            throw new ManagerSaveException(String.format("Неверный формат файла: %s. В файле отсутствует " +
                    "пустая строка, разделяющая задачи и историю просмотров.", file.getPath()));
        }

        for (int i = 1; i < indexOfEmptyLine; i++) {
            validateTaskLine(lines[i], i, file);
        }

        if (lines.length < indexOfEmptyLine + 2) {
            throw new ManagerSaveException(String.format("Неверный формат файла: %s. " +
                    "Отсутствует строка с информацией об истории просмотров задач", file.getPath()));
        }
    }

    private static void validateTaskLine(String line, int lineIndex, File file) {
        String[] parts = line.split(",");
        switch (TaskType.valueOf(parts[1])) {
            case TASK:
            case EPIC:
                if (parts.length < 8) {
                    throw new ManagerSaveException(String.format("Неверный формат файла: %s. " +
                            "Недостаточное количество элементов в строке: %d", file.getPath(), (lineIndex + 1)));
                }
                break;
            case SUBTASK:
                if (parts.length < 9) {
                    throw new ManagerSaveException(String.format("Неверный формат файла: %s. " +
                            "Недостаточное количество элементов в строке: %d", file.getPath(), (lineIndex + 1)));
                }
        }
    }

    protected void save() {
        StringBuilder sb = new StringBuilder();
        boolean isEmpty = true;
        sb.append("id,type,name,status,description,startTime,duration,endTime,epic\n");
        for (Task task : idToTask.values()) {
            sb.append(toString(task)).append(",\n");
            isEmpty = false;
        }

        for (Epic epic : idToEpic.values()) {
            sb.append(toString(epic)).append(",\n");
            isEmpty = false;
        }

        for (Subtask subtask : idToSubtask.values()) {
            sb.append(toString(subtask)).append(",\n");
            isEmpty = false;
        }

        if (!isEmpty) {
            removeLastComma(sb);
        }

        sb.append("\n");
        sb.append(historyToString(historyManager));

        try (FileWriter fileWriter = new FileWriter(fileForSaving);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(sb.toString());
        } catch (IOException e) {
            throw new ManagerSaveException(String.format("Ошибка при попытке записи в файл: %s" +
                    ", произошло исключение: %s", fileForSaving.getPath(), e));
        }
    }

    private String toString(Task task) {
        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            return subtask.getId() + "," + TaskType.SUBTASK + "," + subtask.getName() +
                    "," + subtask.getStatus() + "," + subtask.getDescription() + "," +
                    subtask.getStartTime() + "," + subtask.getDuration() + "," + subtask.getEndTime() + "," +
                    subtask.getEpicId();
        }

        if (task instanceof Epic) {
            return task.getId() + "," + TaskType.EPIC + "," + task.getName() +
                    "," + task.getStatus() + "," + task.getDescription() + "," +
                    task.getStartTime() + "," + task.getDuration() + "," + task.getEndTime();
        }

        return task.getId() + "," + TaskType.TASK + "," + task.getName() +
                "," + task.getStatus() + "," + task.getDescription() + "," +
                task.getStartTime() + "," + task.getDuration() + "," + task.getEndTime();
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");
        switch (TaskType.valueOf(parts[1])) {
            case TASK:
                return new Task(Integer.parseInt(parts[0]), parts[2], parts[4],
                        parseLocalDate(parts[5]), Integer.parseInt(parts[6]), Status.valueOf(parts[3]));
            case EPIC:
                return new Epic(Integer.parseInt(parts[0]), parts[2], parts[4], parseLocalDate(parts[5]),
                        Integer.parseInt(parts[6]), parseLocalDate(parts[7]), Status.valueOf(parts[3]));
            case SUBTASK:
                return new Subtask(Integer.parseInt(parts[0]), parts[2], parts[4], parseLocalDate(parts[5]),
                        Integer.parseInt(parts[6]), Status.valueOf(parts[3]), Integer.parseInt(parts[8]));
        }
        return null;
    }

    private static LocalDateTime parseLocalDate(String value) {
        if (value.equals("null")) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    private static void addSubtaskIdToEpic(Subtask subtask, Epic epic) {
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
        }
    }

    private static void removeLastComma(StringBuilder sb) {
        if (sb.indexOf(",") != -1) {
            sb.delete(sb.lastIndexOf(","), sb.lastIndexOf(",") + 1);
        }
    }

    private static int findIndexOfEmptyLine(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                return i;
            }
        }
        return -1;
    }


    private static String historyToString(HistoryManager manager) {
        List<Task> history = manager.getHistory();
        StringBuilder sb = new StringBuilder();
        for (Task task : history) {
            sb.append(task.getId());
            sb.append(",");
        }
        removeLastComma(sb);
        String value = sb.toString();
        return value.isEmpty() ? "null" : value;
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> historyIds = new ArrayList<>();
        if (value.equals("null")) {
            return historyIds;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            historyIds.add(Integer.parseInt(part));
        }
        return historyIds;
    }
}
