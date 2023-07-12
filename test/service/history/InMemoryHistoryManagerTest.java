package service.history;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Epic epic;
    private Subtask subtask1;
    private Subtask subtask2;

    @BeforeEach
    void beforeEach() {
        historyManager = Managers.getDefaultHistory();
        task1 = new Task(1, "Test Task1", "Test Task1 description", Status.NEW);
        task2 = new Task(2, "Test Task2", "Test Task2 description", Status.NEW);
        epic = new Epic(3, "Test epic", "Test epic description");
        subtask1 = new Subtask(4, "Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, 3);
        subtask2 = new Subtask(5, "Test Subtask2", "Test Subtask2 description",
                Status.DONE, 3);
    }

    @Test
    void add() {
        historyManager.add(task1);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История пустая.");
        assertEquals(1, history.size(), "История пустая.");
        assertEquals(task1, history.get(0), "Задачи не совпадают");
    }

    @Test
    void emptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertEquals(0, history.size(), "Размер списка при пустой истории не равен 0");
    }

    @Test
    void duplicateTasks() {
        historyManager.add(task2);
        historyManager.add(subtask1);
        historyManager.add(epic);
        historyManager.add(subtask2);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epic);

        List<Task> expectedHistory = List.of(subtask1, subtask2, task1, task2, epic);

        List<Task> actualHistory = historyManager.getHistory();

        assertEquals(expectedHistory, actualHistory, "Полученная история просмотра задач не совпадает" +
                "с ожидаемой");
    }

    @Test
    void removeFromBeginningOfHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epic);

        historyManager.remove(1);

        List<Task> expectedHistory = List.of(task2, epic);

        List<Task> actualHistory = historyManager.getHistory();

        assertEquals(expectedHistory, actualHistory, "Полученная история просмотра задач не совпадает" +
                "с ожидаемой");
    }

    @Test
    void removeFromMiddleOfHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epic);

        historyManager.remove(2);

        List<Task> expectedHistory = List.of(task1, epic);

        List<Task> actualHistory = historyManager.getHistory();

        assertEquals(expectedHistory, actualHistory, "Полученная история просмотра задач не совпадает" +
                "с ожидаемой");
    }

    @Test
    void removeFromEndOfHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epic);

        historyManager.remove(3);

        List<Task> expectedHistory = List.of(task1, task2);

        List<Task> actualHistory = historyManager.getHistory();

        assertEquals(expectedHistory, actualHistory, "Полученная история просмотра задач не совпадает" +
                "с ожидаемой");
    }

    @Test
    void removeTaskWithNonexistentId() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epic);

        boolean isRemoved = historyManager.remove(10);
        assertFalse(isRemoved, "Задача с несуществующим id была удалена");
    }
}