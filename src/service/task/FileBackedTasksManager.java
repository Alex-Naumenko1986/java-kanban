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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private File fileForSaving;

    public FileBackedTasksManager(File fileForSaving) {
        this.fileForSaving = fileForSaving;
    }

    public FileBackedTasksManager(HistoryManager historyManager, HashMap<Integer, Task> idToTask, HashMap<Integer,
            Epic> idToEpic, HashMap<Integer, Subtask> idToSubtask, File fileForSaving) {
        super();
        this.historyManager = historyManager;
        this.idToTask = idToTask;
        this.idToSubtask = idToSubtask;
        this.idToEpic = idToEpic;
        this.fileForSaving = fileForSaving;
    }

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        Task task1 = new Task("Корм", "Купить корм для хомяка", Status.NEW);
        Task task2 = new Task("Фитнес", "Сходить в спортзал", Status.NEW);
        taskManager.createNewTask(task1);
        taskManager.createNewTask(task2);

        Epic epic1 = new Epic("Переезд", "Переехать в новую квартиру");
        taskManager.createNewEpic(epic1);
        Subtask subtask1 = new Subtask(epic1.getId(), "Вещи", "Собрать вещи для переезда",
                Status.NEW);
        Subtask subtask2 = new Subtask(epic1.getId(), "Кот", "Перевезти кота в новую квартиру",
                Status.NEW);
        Subtask subtask3 = new Subtask(epic1.getId(), "Мебель", "Купить мебель в новую квартиру",
                Status.IN_PROGRESS);
        taskManager.createNewSubtask(subtask1);
        taskManager.createNewSubtask(subtask2);
        taskManager.createNewSubtask(subtask3);

        Epic epic2 = new Epic("Кофемашина", "Купить новую кофемашину");
        taskManager.createNewEpic(epic2);

        taskManager.getEpic(epic1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getSubtask(subtask2.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask3.getId());
        taskManager.getTask(task1.getId());

        System.out.println("Список задач в менеджере задач до восстановления данных из файла:");
        printTasks(taskManager.getAllTasks());
        printSubtasks(taskManager.getAllSubtasks());
        printEpics(taskManager.getAllEpics());
        System.out.println();

        System.out.println("История просмоторов задач до восстановления данных из файла:");
        printTasks(taskManager.getHistory());
        System.out.println();

        TaskManager taskManager2 = Managers.loadTaskManagerFromFile();

        System.out.println("Список задач в менеджере задач после восстановления данных из файла:");
        printTasks(taskManager2.getAllTasks());
        printSubtasks(taskManager2.getAllSubtasks());
        printEpics(taskManager2.getAllEpics());
        System.out.println();

        System.out.println("История просмоторов задач после восстановления данных из файла:");
        printTasks(taskManager2.getHistory());
    }

    private static void printTasks(List<Task> tasks) {
        for (Task task : tasks) {
            System.out.println(task);
        }
    }

    private static void printSubtasks(List<Subtask> subtasks) {
        for (Task subtask : subtasks) {
            System.out.println(subtask);
        }
    }

    private static void printEpics(List<Epic> epics) {
        for (Epic epic : epics) {
            System.out.println(epic);
        }
    }

    @Override
    public void createNewEpic(Epic epic) {
        super.createNewEpic(epic);
        save();
    }

    @Override
    public void createNewTask(Task task) {
        super.createNewTask(task);
        save();
    }

    @Override
    public void createNewSubtask(Subtask subtask) {
        super.createNewSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
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

        for (Integer id : historyIds) {
            Task task = idToTaskForHistory.get(id);
            if (task != null) {
                historyManager.add(task);
            }
        }
        return new FileBackedTasksManager(historyManager, idToTask, idToEpic, idToSubtask, file);
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
                if (parts.length < 5) {
                    throw new ManagerSaveException(String.format("Неверный формат файла: %s. " +
                            "Недостаточное количество элементов в строке: %d", file.getPath(), (lineIndex + 1)));
                }
                break;
            case SUBTASK:
                if (parts.length < 6) {
                    throw new ManagerSaveException(String.format("Неверный формат файла: %s. " +
                            "Недостаточное количество элементов в строке: %d", file.getPath(), (lineIndex + 1)));
                }
        }
    }

    private void save() {
        StringBuilder sb = new StringBuilder();
        boolean isEmpty = true;
        sb.append("id,type,name,status,description,epic\n");
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
                    subtask.getEpicId();
        }

        if (task instanceof Epic) {
            return task.getId() + "," + TaskType.EPIC + "," + task.getName() +
                    "," + task.getStatus() + "," + task.getDescription();
        }

        return task.getId() + "," + TaskType.TASK + "," + task.getName() +
                "," + task.getStatus() + "," + task.getDescription();
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");
        switch (TaskType.valueOf(parts[1])) {
            case TASK:
                return new Task(Integer.parseInt(parts[0]), parts[2], parts[4], Status.valueOf(parts[3]));
            case EPIC:
                return new Epic(Integer.parseInt(parts[0]), parts[2], parts[4], Status.valueOf(parts[3]));
            case SUBTASK:
                return new Subtask(Integer.parseInt(parts[0]), parts[2], parts[4], Status.valueOf(parts[3]),
                        Integer.parseInt(parts[5]));
        }
        return null;
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
