package service.task;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    private T taskManager;

    protected Epic epic1;
    protected Epic epic2;
    protected Subtask subtask1;
    protected Subtask subtask2;
    protected Subtask subtask3;
    protected Subtask subtaskWithTime1;
    protected Subtask subtaskWithTime2;
    protected Subtask subtaskWithTime3;
    protected Task task1;
    protected Task task2;
    protected Task taskWithTime1;
    protected Task taskWithTime2;

    public void setTaskManager(T taskManager) {
        this.taskManager = taskManager;
    }

    @BeforeEach
    public void createTasks() {
        epic1 = new Epic("Test epic1", "Test epic1 description");
        epic2 = new Epic("Test epic2", "Test epic2 description");
        subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description", Status.NEW);
        subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description", Status.NEW);
        subtask3 = new Subtask("Test Subtask3", "Test Subtask3 description", Status.NEW);
        task1 = new Task("Test Task1", "Test Task1 description", Status.NEW);
        task2 = new Task("Test Task2", "Test Task2 description", Status.NEW);
        taskWithTime1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 0), 40, Status.NEW);
        taskWithTime2 = new Task("Test task2", "Test task2 description",
                LocalDateTime.of(2023, 6, 30, 12, 0), 60, Status.NEW);
        subtaskWithTime1 = new Subtask("Test subtask1", "Test subtask1 description",
                LocalDateTime.of(2023, 7, 4, 17, 30), 40,
                Status.NEW);
        subtaskWithTime2 = new Subtask("Test subtask2", "Test subtask2 description",
                LocalDateTime.of(2023, 7, 3, 14, 30), 60,
                Status.NEW);
        subtaskWithTime3 = new Subtask("Test subtask3", "Test subtask3 description",
                LocalDateTime.of(2023, 7, 5, 12, 0), 100,
                Status.NEW);
    }

    @Test
    void epicWithNoSubtasksShouldHaveStatusNew() {
        int epicId = taskManager.addNewEpic(epic1);

        final Epic savedEpic = taskManager.getEpic(epicId);

        assertEquals(Status.NEW, savedEpic.getStatus(), "У эпика статус, отличный от NEW");
    }

    @Test
    void epicWithNewSubtasksShouldHaveStatusNew() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        Epic savedEpic = taskManager.getEpic(epicId);

        assertEquals(Status.NEW, savedEpic.getStatus(), "У эпика статус, отличный от NEW");
    }

    @Test
    void epicWithDoneSubtasksShouldHaveStatusDone() {
        int epicId = taskManager.addNewEpic(epic1);
        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);

        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        Epic savedEpic = taskManager.getEpic(epicId);

        assertEquals(Status.DONE, savedEpic.getStatus(), "У эпика статус, отличный от DONE");
    }

    @Test
    void epicWithNewAndDoneSubtasksShouldHaveStatusInProgress() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        subtask1.setStatus(Status.NEW);
        subtask2.setStatus(Status.DONE);

        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        Epic savedEpic = taskManager.getEpic(epicId);

        assertEquals(Status.IN_PROGRESS, savedEpic.getStatus(), "У эпика статус, отличный от IN_PROGRESS");
    }

    @Test
    void epicWithInProgressSubtasksShouldHaveStatusInProgress() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        subtask1.setStatus(Status.IN_PROGRESS);
        subtask2.setStatus(Status.IN_PROGRESS);

        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        Epic savedEpic = taskManager.getEpic(epicId);

        assertEquals(Status.IN_PROGRESS, savedEpic.getStatus(), "У эпика статус, отличный от IN_PROGRESS");
    }

    @Test
    void checkEpicStatusWhenUpdatingSubtask() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.IN_PROGRESS);

        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        Epic savedEpic = taskManager.getEpic(epicId);
        assertEquals(Status.DONE, savedEpic.getStatus(), "Стутус эписка отличается от DONE");
    }

    @Test
    void checkEpicStatusWhenRemovingSubtask() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        subtask1.setStatus(Status.NEW);
        subtask2.setStatus(Status.IN_PROGRESS);
        taskManager.addNewSubtask(subtask1);
        int subtask2Id = taskManager.addNewSubtask(subtask2);

        taskManager.removeSubtask(subtask2Id);

        Epic savedEpic = taskManager.getEpic(epicId);
        assertEquals(Status.NEW, savedEpic.getStatus(), "Стутус эписка отличается от NEW");
    }

    @Test
    void checkEpicStatusWhenRemovingAllSubtasks() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);

        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        taskManager.removeAllSubtasks();

        Epic savedEpic = taskManager.getEpic(epicId);
        assertEquals(Status.NEW, savedEpic.getStatus(), "Стутус эписка отличается от NEW");
    }

    @Test
    void checkAddingNewSubtaskWithWrongEpicId() {
        subtask1.setEpicId(1);

        int id = taskManager.addNewSubtask(subtask1);
        assertEquals(-1, id, "Подзадача с неверным id эпика была обновлена");
    }

    @Test
    void checkUpdatingSubtaskWithWrongEpicId() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        taskManager.addNewSubtask(subtask1);

        subtask1.setEpicId(3);

        boolean isUpdated = taskManager.updateSubtask(subtask1);

        assertFalse(isUpdated, "Подзадача с неверным id эпика была обновлена");
    }

    @Test
    void addNewTask() {
        final int taskId = taskManager.addNewTask(task1);

        final Task savedTask = taskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task1, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task1, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void addNewEpic() {
        final int epicId = taskManager.addNewEpic(epic1);

        final Epic savedEpic = taskManager.getEpic(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic1, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getAllEpics();

        assertNotNull(epics, "Эпики на возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic1, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void addNewSubtask() {
        final int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        final int subtaskId = taskManager.addNewSubtask(subtask1);
        final Subtask savedSubtask = taskManager.getSubtask(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask1, savedSubtask, "Подзадачи не совпадают.");
        assertEquals(epicId, savedSubtask.getEpicId(), "id эпиков не совпадают");
        final List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertNotNull(subtasks, "Подзадачи на возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask1, subtasks.get(0), "Подзадачи не совпадают.");
    }

    @Test
    void getAllTasksWithNoTasksAdded() {
        List<Task> tasks = taskManager.getAllTasks();

        assertEquals(0, tasks.size(), "Размер возвращенного списка не равен нулю.");
    }

    @Test
    void getAllTasks() {
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        List<Task> tasks = taskManager.getAllTasks();
        assertEquals(2, tasks.size(), "Размер возвращаемого списка не равен 2");
        assertEquals(task1, tasks.get(0), "Задачи не совпадают");
        assertEquals(task2, tasks.get(1), "Задачи не совпадают");
    }

    @Test
    void getAllSubtasksWithNoSubtasksAdded() {
        List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertEquals(0, subtasks.size(), "Размер возвращенного списка не равен нулю.");
    }

    @Test
    void getAllSubtasks() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);

        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        List<Subtask> subtasks = taskManager.getAllSubtasks();
        assertEquals(2, subtasks.size(), "Размер возвращаемого списка не равен 2");
        assertEquals(subtask1, subtasks.get(0), "Подзадачи не совпадают");
        assertEquals(subtask2, subtasks.get(1), "Подзадачи не совпадают");
    }

    @Test
    void getAllEpicsWithNoEpicsAdded() {
        List<Epic> epics = taskManager.getAllEpics();

        assertEquals(0, epics.size(), "Размер возвращенного списка не равен нулю.");
    }

    @Test
    void getAllEpics() {
        taskManager.addNewEpic(epic1);
        taskManager.addNewEpic(epic2);

        List<Epic> epics = taskManager.getAllEpics();
        assertEquals(2, epics.size(), "Размер возвращаемого списка не равен 2");
        assertEquals(epic1, epics.get(0), "Эпики не совпадают");
        assertEquals(epic2, epics.get(1), "Эпики не совпадают");
    }

    @Test
    void removeAllTasks() {
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        taskManager.removeAllTasks();
        List<Task> tasks = taskManager.getAllTasks();
        assertEquals(0, tasks.size(), "Размер списка после удаления не равен 0");
    }

    @Test
    void removeAllEpics() {
        int epic1Id = taskManager.addNewEpic(epic1);
        taskManager.addNewEpic(epic2);

        subtask1.setEpicId(epic1Id);
        subtask2.setEpicId(epic1Id);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        taskManager.removeAllEpics();
        List<Epic> epics = taskManager.getAllEpics();
        List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertEquals(0, epics.size(), "Размер списка эпиков не равен 0");
        assertEquals(0, subtasks.size(), "Размер списка подзадач не равен 0");
    }

    @Test
    void removeAllSubtasks() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        subtask1.setStatus(Status.IN_PROGRESS);
        subtask2.setEpicId(epicId);
        subtask2.setStatus(Status.IN_PROGRESS);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        taskManager.removeAllSubtasks();

        Epic savedEpic = taskManager.getEpic(epicId);
        List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertEquals(Status.NEW, savedEpic.getStatus(), "Статус эпика отличен от NEW");
        assertEquals(0, subtasks.size(), "Размер списка подзадач не равен 0");
    }

    @Test
    void getTask() {
        taskManager.addNewTask(task1);
        int task2Id = taskManager.addNewTask(task2);

        Task task2Saved = taskManager.getTask(task2Id);

        assertEquals(task2, task2Saved, "Задачи не совпадают");
    }

    @Test
    void getTaskWithEmptyTaskList() {
        Task task = taskManager.getTask(1);
        assertNull(task, "Задача не равна null");
    }

    @Test
    void getTaskWithWrongId() {
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Task task = taskManager.getTask(10);
        assertNull(task, "Задача не равна null");
    }

    @Test
    void getEpic() {
        taskManager.addNewEpic(epic1);
        int epic2Id = taskManager.addNewEpic(epic2);

        Epic savedEpic2 = taskManager.getEpic(epic2Id);
        assertEquals(epic2, savedEpic2, "Эпики не совпадают");
    }

    @Test
    void getEpicWithEmptyEpicList() {
        Epic epic = taskManager.getEpic(1);
        assertNull(epic, "Эпик не равен null");
    }

    @Test
    void getEpicWithWrongId() {
        taskManager.addNewEpic(epic1);
        taskManager.addNewEpic(epic2);

        Epic savedEpic = taskManager.getEpic(10);
        assertNull(savedEpic, "Эпик не равен null");
    }

    @Test
    void getSubtask() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        taskManager.addNewSubtask(subtask1);
        int subtask2Id = taskManager.addNewSubtask(subtask2);

        Subtask savedSubtask2 = taskManager.getSubtask(subtask2Id);
        assertEquals(epicId, savedSubtask2.getEpicId(), "Неверный идентификатор эпика у подзадачи");
        assertEquals(subtask2, savedSubtask2, "Подзадачи не совпадают");
    }

    @Test
    void getSubtaskWithEmptySubtaskList() {
        Subtask subtask = taskManager.getSubtask(1);
        assertNull(subtask, "Подзадача не равна null");
    }

    @Test
    void getSubtaskWithWrongId() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        Subtask subtask = taskManager.getSubtask(10);
        assertNull(subtask, "Подзадача не равна null");
    }

    @Test
    void updateTask() {
        int taskId = taskManager.addNewTask(task1);
        task1.setName("Test Task changed");
        task1.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task1);

        Task updatedTask = taskManager.getTask(taskId);
        assertEquals(task1, updatedTask, "Задачи не совпадают.");
    }

    @Test
    void updateTaskWithEmptyTaskList() {
        task1.setId(1);
        boolean isUpdated = taskManager.updateTask(task1);
        assertFalse(isUpdated, "Задача была обновлена при пустом списке задач");
    }

    @Test
    void updateTaskWithWrongId() {
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Task task3 = new Task(3, "Test Task3", "Test Task3 description", Status.NEW);

        boolean isUpdated = taskManager.updateTask(task3);
        assertFalse(isUpdated, "Задача с неверным идентификатором была обновлена");
    }

    @Test
    void updateEpic() {
        int epicId = taskManager.addNewEpic(epic1);

        epic1.setName("Test epic changed");
        epic1.setDescription("Test epic description changed");
        taskManager.updateEpic(epic1);

        Epic updatedEpic = taskManager.getEpic(epicId);
        assertEquals(epic1, updatedEpic, "Эпики не совпадают.");
    }

    @Test
    void updateEpicWithEmptyEpicList() {
        epic1.setId(1);
        boolean isUpdated = taskManager.updateEpic(epic1);

        assertFalse(isUpdated, "Эпик был обновлен");
    }

    @Test
    void updateEpicWithWrongId() {
        taskManager.addNewEpic(epic1);
        taskManager.addNewEpic(epic2);

        Epic epic3 = new Epic("Test Epic3", "Test Epic3 description");
        epic3.setId(3);

        boolean isUpdated = taskManager.updateEpic(epic3);
        assertFalse(isUpdated, "Эпик с неверным идентификатором был обновлен");
    }

    @Test
    void updateSubtask() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        subtask1.setStatus(Status.IN_PROGRESS);
        int subtaskId = taskManager.addNewSubtask(subtask1);

        subtask1.setName("Test Subtask1 changed");
        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);

        Subtask updatedSubtask = taskManager.getSubtask(subtaskId);

        assertEquals(Status.DONE, epic1.getStatus(), "Статус эпика отличается от DONE");
        assertEquals(subtask1, updatedSubtask, "Подзадачи отличаются");
    }

    @Test
    void updateSubtaskWithEmptySubtaskList() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setId(2);
        subtask1.setEpicId(epicId);

        boolean isUpdated = taskManager.updateSubtask(subtask1);
        assertFalse(isUpdated, "Подзадача была обновлена при пустом списке подзадач");
    }

    @Test
    void updateSubtaskWithWrongId() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        taskManager.addNewSubtask(subtask1);
        subtask2.setEpicId(epicId);
        subtask2.setId(3);
        boolean isUpdated = taskManager.updateSubtask(subtask2);

        assertFalse(isUpdated, "Подзадача с неверным идентификатором была обновлена");

    }

    @Test
    void removeTask() {
        int task1Id = taskManager.addNewTask(task1);
        int task2Id = taskManager.addNewTask(task2);

        boolean isRemoved = taskManager.removeTask(task1Id);
        Task removedTask = taskManager.getTask(task1Id);
        Task savedTask2 = taskManager.getTask(task2Id);

        assertTrue(isRemoved, "Задача не удалена");
        assertNull(removedTask, "Удаленная задача не равна null");
        assertEquals(task2, savedTask2, "Задачи 2 не совпадают. Возможно задача 2 была удалена");
    }

    @Test
    void removeTaskFromEmptyTaskList() {
        boolean isRemoved = taskManager.removeTask(1);
        assertFalse(isRemoved, "Задача была удалена из пустого списка задач");
    }

    @Test
    void removeTaskWithWrongId() {
        int taskId = taskManager.addNewTask(task1);

        boolean isRemoved = taskManager.removeTask(10);
        Task savedTask = taskManager.getTask(taskId);
        assertFalse(isRemoved, "Задача с несуществующим id была удалена");
        assertEquals(task1, savedTask, "Задачи не совпадают. Возможно, задача была удалена");
    }

    @Test
    void removeEpic() {
        int epic1Id = taskManager.addNewEpic(epic1);
        int epic2Id = taskManager.addNewEpic(epic2);

        boolean isRemoved = taskManager.removeEpic(epic1Id);
        Epic removedEpic = taskManager.getEpic(epic1Id);
        Epic savedEpic2 = taskManager.getEpic(epic2Id);

        assertTrue(isRemoved, "Эпик не удален");
        assertNull(removedEpic, "Удаленный эпик не равен null");
        assertEquals(epic2, savedEpic2, "Эпики 2 не совпадают. Возможно, эпик 2 был удален");
    }

    @Test
    void removeEpicFromEmptyEpicList() {
        boolean isRemoved = taskManager.removeEpic(1);
        assertFalse(isRemoved, "Эпик был удален из пустого списка эпиков");
    }

    @Test
    void removeEpicWithWrongId() {
        int epicId = taskManager.addNewEpic(epic1);

        boolean isRemoved = taskManager.removeEpic(10);
        Epic savedEpic = taskManager.getEpic(epicId);
        assertFalse(isRemoved, "Эпик с несуществующим id был удален");
        assertEquals(epic1, savedEpic, "Эпики не совпадают. Возможно, эпик был удален");
    }

    @Test
    void removeSubtask() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        int subtask1Id = taskManager.addNewSubtask(subtask1);
        int subtask2Id = taskManager.addNewSubtask(subtask2);

        boolean isRemoved = taskManager.removeSubtask(subtask1Id);
        Subtask removedSubtask = taskManager.getSubtask(subtask1Id);
        Subtask savedSubtask2 = taskManager.getSubtask(subtask2Id);

        assertTrue(isRemoved, "Подзадача не была удалена");
        assertNull(removedSubtask, "Удаленная подзадача не равна null");
        assertEquals(subtask2, savedSubtask2, "Подзадачи 2 не совпадают. Возможно, подзадача 2 была удалена");
    }

    @Test
    void removeSubtaskFromEmptySubtaskList() {
        boolean isRemoved = taskManager.removeSubtask(1);
        assertFalse(isRemoved, "Подзадача была удалена из пустого списка подзадач");
    }

    @Test
    void removeSubtaskWithWrongId() {
        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        int subtaskId = taskManager.addNewSubtask(subtask1);

        boolean isRemoved = taskManager.removeSubtask(10);
        Subtask savedSubtask = taskManager.getSubtask(subtaskId);

        assertFalse(isRemoved, "Подзадача с неверным идентификатором была удалена");
        assertEquals(subtask1, savedSubtask, "Подзадачи не совпадают. Возможно, подзадача была удалена");
    }

    @Test
    void getHistory() {
        int task1Id = taskManager.addNewTask(task1);
        int task2Id = taskManager.addNewTask(task2);

        int epicId = taskManager.addNewEpic(epic1);

        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        int subtask1Id = taskManager.addNewSubtask(subtask1);
        int subtask2Id = taskManager.addNewSubtask(subtask2);

        taskManager.getEpic(epicId);
        taskManager.getSubtask(subtask2Id);
        taskManager.getTask(task1Id);
        taskManager.getTask(task2Id);
        taskManager.getSubtask(subtask1Id);
        taskManager.getTask(task1Id);

        List<Task> expectedHistory = List.of(epic1, subtask2, task2, subtask1, task1);

        List<Task> actualHistory = taskManager.getHistory();

        assertEquals(expectedHistory, actualHistory, "Полученная история просмотра задач не совпадает" +
                "с ожидаемой");
    }

    @Test
    void getEpicSubtasks() {
        int epic1Id = taskManager.addNewEpic(epic1);
        int epic2Id = taskManager.addNewEpic(epic2);

        subtask1.setEpicId(epic1Id);
        subtask2.setEpicId(epic1Id);
        subtask3.setEpicId(epic2Id);

        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        taskManager.addNewSubtask(subtask3);

        List<Subtask> expectedSubtasks = List.of(subtask1, subtask2);

        List<Subtask> actualSubtasks = taskManager.getEpicSubtasks(epic1Id);
        assertEquals(expectedSubtasks, actualSubtasks, "Списки подзадач эпика 1 не совпадают");
    }

    @Test
    void getEpicSubtasksWhenNoSubtasksAdded() {
        int epicId = taskManager.addNewEpic(epic1);

        List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);

        assertEquals(0, subtasks.size(), "Размер списка подзадач не равен 0");
    }

    @Test
    void getEpicSubtasksWithWrongEpicId() {
        int epicId = taskManager.addNewEpic(epic1);
        subtask1.setEpicId(epicId);
        taskManager.addNewSubtask(subtask1);

        List<Subtask> subtasks = taskManager.getEpicSubtasks(10);
        assertTrue(subtasks.isEmpty(), "Полученный список подзадач не пустой");
    }

    @Test
    void getPrioritizedTasks() {
        taskManager.addNewTask(taskWithTime1);
        taskManager.addNewTask(taskWithTime2);

        int epic1Id = taskManager.addNewEpic(epic1);
        subtaskWithTime1.setEpicId(epic1Id);
        subtaskWithTime2.setEpicId(epic1Id);

        taskManager.addNewSubtask(subtaskWithTime1);
        taskManager.addNewSubtask(subtaskWithTime2);

        taskManager.addNewEpic(epic2);

        List<Task> expectedPrioritizedTasks = List.of(taskWithTime2, taskWithTime1, epic1, subtaskWithTime2,
                subtaskWithTime1, epic2);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Списки задач по приоритету не " +
                "совпадают");
    }

    @Test
    void checkEpicTimeCalculation() {
        int epicId = taskManager.addNewEpic(epic1);
        subtaskWithTime1.setEpicId(epicId);
        subtaskWithTime2.setEpicId(epicId);
        subtaskWithTime3.setEpicId(epicId);

        taskManager.addNewSubtask(subtaskWithTime1);
        taskManager.addNewSubtask(subtaskWithTime2);
        taskManager.addNewSubtask(subtaskWithTime3);

        LocalDateTime startTime = epic1.getStartTime();
        int duration = epic1.getDuration();
        LocalDateTime endTime = epic1.getEndTime();

        assertEquals(LocalDateTime.of(2023, 7, 3, 14, 30), startTime,
                "Неверное время начала эпика");
        assertEquals(200, duration, "Неверная продолжительность эпика");
        assertEquals(LocalDateTime.of(2023, 7, 5, 13, 40), endTime,
                "Неверное время окончания эпика");
    }

    @Test
    void checkTaskEndTimeCalculation() {
        LocalDateTime endTime = taskWithTime1.getEndTime();

        assertEquals(LocalDateTime.of(2023, 6, 30, 13, 40), endTime,
                "Время окончания задачи не совпадает с ожидаемым");

    }

    @Test
    void checkSubtaskEndTimeCalculation() {
        LocalDateTime endTime = subtaskWithTime1.getEndTime();
        assertEquals(LocalDateTime.of(2023, 7, 4, 18, 10), endTime, "Время" +
                "окончания подзадачи не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenUpdatingTask() {
        taskManager.addNewTask(taskWithTime1);
        taskManager.addNewTask(taskWithTime2);

        int epic1Id = taskManager.addNewEpic(epic1);
        subtaskWithTime1.setEpicId(epic1Id);

        taskManager.addNewSubtask(subtaskWithTime1);

        taskWithTime2.setStartTime(LocalDateTime.of(2023, 7, 4, 18, 11));
        taskManager.updateTask(taskWithTime2);

        List<Task> expectedPrioritizedTasks = List.of(taskWithTime1, epic1, subtaskWithTime1, taskWithTime2);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач" +
                "по приоритету не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenUpdatingSubtask() {
        taskManager.addNewTask(taskWithTime1);
        taskManager.addNewTask(taskWithTime2);

        int epic1Id = taskManager.addNewEpic(epic1);
        subtaskWithTime1.setEpicId(epic1Id);

        taskManager.addNewSubtask(subtaskWithTime1);

        subtaskWithTime1.setStartTime(LocalDateTime.of(2023, 6, 30, 11, 15));
        taskManager.updateSubtask(subtaskWithTime1);

        List<Task> expectedPrioritizedTasks = List.of(epic1, subtaskWithTime1, taskWithTime2, taskWithTime1);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач" +
                "по приоритету не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenRemovingTask() {
        taskManager.addNewTask(taskWithTime1);
        int task2Id = taskManager.addNewTask(taskWithTime2);

        taskManager.removeTask(task2Id);
        List<Task> expectedPrioritizedTasks = List.of(taskWithTime1);

        List<Task> actualPrioritizedTask = taskManager.getPrioritizedTasks();
        assertEquals(expectedPrioritizedTasks, actualPrioritizedTask, "Полученный список задач по приоритету" +
                "не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenRemovingEpic() {
        taskManager.addNewTask(taskWithTime1);
        taskManager.addNewTask(taskWithTime2);

        int epic1Id = taskManager.addNewEpic(epic1);
        subtaskWithTime1.setEpicId(epic1Id);
        taskManager.addNewSubtask(subtaskWithTime1);

        taskManager.removeEpic(epic1Id);

        List<Task> expectedPrioritizedTasks = List.of(taskWithTime2, taskWithTime1);
        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач по приоритету" +
                "не совпадает с ожидаемым");

    }

    @Test
    void checkTaskPrioritizationWhenRemovingSubtask() {
        taskManager.addNewTask(taskWithTime1);
        taskManager.addNewTask(taskWithTime2);

        int epic1Id = taskManager.addNewEpic(epic1);
        subtaskWithTime1.setEpicId(epic1Id);
        int subtask1Id = taskManager.addNewSubtask(subtaskWithTime1);

        taskManager.removeSubtask(subtask1Id);

        List<Task> expectedPrioritizedTasks = List.of(taskWithTime2, taskWithTime1, epic1);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач по приоритету" +
                "не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenRemovingAllTasks() {
        taskManager.addNewTask(taskWithTime1);
        taskManager.addNewTask(taskWithTime2);

        int epic1Id = taskManager.addNewEpic(epic1);
        subtaskWithTime1.setEpicId(epic1Id);
        taskManager.addNewSubtask(subtaskWithTime1);

        taskManager.removeAllTasks();

        List<Task> expectedPrioritizedTasks = List.of(epic1, subtaskWithTime1);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач по приоритету" +
                "не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenRemovingAllEpics() {
        taskManager.addNewTask(taskWithTime1);
        taskManager.addNewTask(taskWithTime2);

        int epic1Id = taskManager.addNewEpic(epic1);
        subtaskWithTime1.setEpicId(epic1Id);
        taskManager.addNewSubtask(subtaskWithTime1);

        taskManager.addNewEpic(epic2);

        taskManager.removeAllEpics();

        List<Task> expectedPrioritizedTasks = List.of(taskWithTime2, taskWithTime1);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач по приоритету" +
                "не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenRemovingAllSubtasks() {
        taskManager.addNewTask(taskWithTime1);
        taskManager.addNewTask(taskWithTime2);

        int epic1Id = taskManager.addNewEpic(epic1);
        subtaskWithTime1.setEpicId(epic1Id);
        subtaskWithTime2.setEpicId(epic1Id);
        taskManager.addNewSubtask(subtaskWithTime1);
        taskManager.addNewSubtask(subtaskWithTime2);

        taskManager.removeAllSubtasks();

        List<Task> expectedPrioritizedTasks = List.of(taskWithTime2, taskWithTime1, epic1);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач по приоритету" +
                "не совпадает с ожидаемым");
    }

    @Test
    void checkTaskTimeIntersectionCase1() {
        taskWithTime1.setStartTime(LocalDateTime.of(2023, 6, 30, 13, 0));
        taskWithTime1.setDuration(30);
        taskWithTime2.setStartTime(LocalDateTime.of(2023, 6, 30, 13, 29));
        taskWithTime2.setDuration(60);
        taskManager.addNewTask(taskWithTime1);
        int taskId = taskManager.addNewTask(taskWithTime2);
        assertEquals(-1, taskId, "Задача, пересекающаяся по времени, была добавлена");
    }

    @Test
    void checkTaskTimeIntersectionCase2() {
        taskWithTime1.setStartTime(LocalDateTime.of(2023, 6, 30, 13, 31));
        taskWithTime1.setDuration(30);
        taskWithTime2.setStartTime(LocalDateTime.of(2023, 6, 30, 13, 30));
        taskWithTime2.setDuration(60);
        taskManager.addNewTask(taskWithTime1);
        int taskId = taskManager.addNewTask(taskWithTime2);
        assertEquals(-1, taskId, "Задача, пересекающаяся по времени, была добавлена");
    }

    @Test
    void checkTaskTimeIntersectionCase3() {
        taskWithTime1.setStartTime(LocalDateTime.of(2023, 6, 30, 13, 30));
        taskWithTime1.setDuration(60);
        taskWithTime2.setStartTime(LocalDateTime.of(2023, 6, 30, 13, 40));
        taskWithTime2.setDuration(30);
        taskManager.addNewTask(taskWithTime1);
        int taskId = taskManager.addNewTask(taskWithTime2);
        assertEquals(-1, taskId, "Задача, пересекающаяся по времени, была добавлена");
    }

    @Test
    void checkTaskTimeIntersectionCase4() {
        taskWithTime1.setStartTime(LocalDateTime.of(2023, 6, 30, 14, 29));
        taskWithTime1.setDuration(30);
        taskWithTime2.setStartTime(LocalDateTime.of(2023, 6, 30, 13, 30));
        taskWithTime2.setDuration(60);
        taskManager.addNewTask(taskWithTime1);
        int taskId = taskManager.addNewTask(taskWithTime2);
        assertEquals(-1, taskId, "Задача, пересекающаяся по времени, была добавлена");
    }

    @Test
    void checkNoTaskTimeIntersectionCase1() {
        taskWithTime1.setStartTime(LocalDateTime.of(2023, 6, 30, 13, 0));
        taskWithTime1.setDuration(30);
        taskWithTime2.setStartTime(LocalDateTime.of(2023, 6, 30, 13, 30));
        taskWithTime2.setDuration(60);
        taskManager.addNewTask(taskWithTime1);
        int id = taskManager.addNewTask(taskWithTime2);
        assertNotEquals(-1, id, "Задача не была добавлена");
    }

    @Test
    void checkNoTaskTimeIntersectionCase2() {
        taskWithTime1.setStartTime(LocalDateTime.of(2023, 6, 30, 14, 30));
        taskWithTime1.setDuration(30);
        taskWithTime2.setStartTime(LocalDateTime.of(2023, 6, 30, 13, 30));
        taskWithTime2.setDuration(60);
        taskManager.addNewTask(taskWithTime1);
        int id = taskManager.addNewTask(taskWithTime2);
        assertNotEquals(-1, id, "Задача не была добавлена");
    }
}