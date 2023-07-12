package service.task;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {
    TaskManager taskManager = new InMemoryTaskManager();

    @BeforeEach
    public void setTaskManager() {
        super.setTaskManager(taskManager);
    }

    @Test
    void testNewManagerHasEmptyTaskLists() {
        List<Task> tasks = taskManager.getAllTasks();
        List<Epic> epics = taskManager.getAllEpics();
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        List<Task> history = taskManager.getHistory();
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();

        assertTrue(tasks.isEmpty(), "У вновь созданного менеджера список задач не пустой");
        assertTrue(epics.isEmpty(), "У вновь созданного менеджера список эпиков не пустой");
        assertTrue(subtasks.isEmpty(), "У вновь созданного менеджера список подзадач не пустой");
        assertTrue(history.isEmpty(), "У вновь созданного менеджера история не пустая");
        assertTrue(prioritizedTasks.isEmpty(), "У вновь созданного менеджера список задач по приоритету не " +
                "пустой");
    }

}