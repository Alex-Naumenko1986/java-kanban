package service.task;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileBackedTasksManagerTest extends TaskManagerTest<TaskManager> {
    private TaskManager taskManager = Managers.getDefault();

    @BeforeEach
    void beforeEach() {
        super.setTaskManager(taskManager);
    }

    @Test
    void loadingWithEmptyTaskList() {
        taskManager = Managers.loadTaskManagerFromFile();
        List<Task> tasks = taskManager.getAllTasks();
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        List<Epic> epics = taskManager.getAllEpics();

        assertEquals(0, tasks.size(), "Размер списка задач не равен 0");
        assertEquals(0, subtasks.size(), "Размер списка подзадач не равен 0");
        assertEquals(0, epics.size(), "Размер списка эпиков не равен 0");
    }

    @Test
    void loadingEpicWithNoSubtasks() {
        Epic epic = new Epic("Test epic", "Test epic description");
        taskManager.addNewEpic(epic);

        taskManager = Managers.loadTaskManagerFromFile();
        List<Epic> epics = taskManager.getAllEpics();
        assertEquals(1, epics.size(), "Размер полученного списка эпиков не равен 1");
        assertEquals(epic, epics.get(0), "Эпики не совпадают");
    }

    @Test
    void loadingWithEmptyHistory() {
        Task task1 = new Task("Test Task1", "Test Task1 description", Status.NEW);
        Task task2 = new Task("Test Task2", "Test Task2 description", Status.NEW);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Epic epic = new Epic("Test epic", "Test epic description");
        taskManager.addNewEpic(epic);

        taskManager = Managers.loadTaskManagerFromFile();

        List<Task> history = taskManager.getHistory();
        assertEquals(0, history.size(), "Размер полученной истории не равен 0");
    }

    @Test
    void checkLoadingTasks() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 1), 40, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 12, 0), 60, Status.NEW);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        int epic1Id = taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Test subtask1", "Test subtask1 description",
                LocalDateTime.of(2023, 6, 30, 11, 15), 40,
                Status.NEW, epic1Id);
        Subtask subtask2 = new Subtask("Test subtask2", "Test subtask2 description",
                LocalDateTime.of(2023, 6, 30, 10, 15), 50,
                Status.NEW, epic1Id);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        Epic epic2 = new Epic("Test epic2", "Test epic2 description");
        taskManager.addNewEpic(epic2);

        taskManager = Managers.loadTaskManagerFromFile();
        List<Task> savedTasks = taskManager.getAllTasks();
        List<Epic> savedEpics = taskManager.getAllEpics();
        List<Subtask> savedSubtasks = taskManager.getAllSubtasks();

        List<Task> expectedTasks = List.of(task1, task2);
        List<Epic> expectedEpics = List.of(epic1, epic2);
        List<Subtask> expectedSubtasks = List.of(subtask1, subtask2);

        assertEquals(expectedTasks, savedTasks, "Списки задач не совпадают");
        assertEquals(expectedEpics, savedEpics, "Списки эпиков не совпадают");
        assertEquals(expectedSubtasks, savedSubtasks, "Списки подзадач не совпадают");
    }

    @Test
    void checkLoadingHistory() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 1), 40, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 12, 0), 60, Status.NEW);
        int task1Id = taskManager.addNewTask(task1);
        int task2Id = taskManager.addNewTask(task2);

        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        int epic1Id = taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Test subtask1", "Test subtask1 description",
                LocalDateTime.of(2023, 6, 30, 11, 15), 40,
                Status.NEW, epic1Id);
        Subtask subtask2 = new Subtask("Test subtask2", "Test subtask2 description",
                LocalDateTime.of(2023, 6, 30, 10, 15), 50,
                Status.NEW, epic1Id);
        int subtask1Id = taskManager.addNewSubtask(subtask1);
        int subtask2Id = taskManager.addNewSubtask(subtask2);

        taskManager.getTask(task2Id);
        taskManager.getTask(task1Id);
        taskManager.getSubtask(subtask1Id);
        taskManager.getEpic(epic1Id);
        taskManager.getSubtask(subtask2Id);
        taskManager.getTask(task2Id);

        taskManager = Managers.loadTaskManagerFromFile();

        List<Task> expectedHistory = List.of(task1, subtask1, epic1, subtask2, task2);
        List<Task> loadedHistory = taskManager.getHistory();

        assertEquals(expectedHistory, loadedHistory, "Загруженная история не совпадает с ожидаемой");
    }

    @Test
    void checkLoadingPrioritizedTasks() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 1), 40, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 12, 0), 60, Status.NEW);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        int epic1Id = taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Test subtask1", "Test subtask1 description",
                LocalDateTime.of(2023, 6, 30, 11, 15), 40,
                Status.NEW, epic1Id);
        Subtask subtask2 = new Subtask("Test subtask2", "Test subtask2 description",
                LocalDateTime.of(2023, 6, 30, 10, 15), 50,
                Status.NEW, epic1Id);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        taskManager = Managers.loadTaskManagerFromFile();

        List<Task> loadedPrioritizedTasks = taskManager.getPrioritizedTasks();
        List<Task> expectedPrioritizedTasks = List.of(epic1, subtask2, subtask1, task2, task1);
        assertEquals(expectedPrioritizedTasks, loadedPrioritizedTasks, "Загруженный список задач по " +
                "приоритету не совпадает с ожидаемым");
    }
}