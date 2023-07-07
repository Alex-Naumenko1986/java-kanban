package service.task;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;
import service.task.exceptions.ManagerIllegalOperationException;
import service.task.exceptions.TimeIntersectionException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    private T taskManager;

    public void setTaskManager(T taskManager) {
        this.taskManager = taskManager;
    }

    @Test
    void epicWithNoSubtasksShouldHaveStatusNew() {
        Epic epic = new Epic("Test status", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        final Epic savedEpic = taskManager.getEpic(epicId);

        assertEquals(Status.NEW, savedEpic.getStatus(), "У эпика статус, отличный от NEW");
    }

    @Test
    void epicWithNewSubtasksShouldHaveStatusNew() {
        Epic epic = new Epic("Test status", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description", Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description", Status.NEW, epicId);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        Epic savedEpic = taskManager.getEpic(epicId);

        assertEquals(Status.NEW, savedEpic.getStatus(), "У эпика статус, отличный от NEW");
    }

    @Test
    void epicWithDoneSubtasksShouldHaveStatusDone() {
        Epic epic = new Epic("Test status", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description", Status.DONE,
                epicId);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description", Status.DONE,
                epicId);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        Epic savedEpic = taskManager.getEpic(epicId);

        assertEquals(Status.DONE, savedEpic.getStatus(), "У эпика статус, отличный от DONE");
    }

    @Test
    void epicWithNewAndDoneSubtasksShouldHaveStatusInProgress() {
        Epic epic = new Epic("Test status", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description", Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description", Status.DONE,
                epicId);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        Epic savedEpic = taskManager.getEpic(epicId);

        assertEquals(Status.IN_PROGRESS, savedEpic.getStatus(), "У эпика статус, отличный от IN_PROGRESS");
    }

    @Test
    void epicWithInProgressSubtasksShouldHaveStatusInProgress() {
        Epic epic = new Epic("Test status", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epicId);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description",
                Status.IN_PROGRESS, epicId);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        Epic savedEpic = taskManager.getEpic(epicId);

        assertEquals(Status.IN_PROGRESS, savedEpic.getStatus(), "У эпика статус, отличный от IN_PROGRESS");
    }

    @Test
    void checkEpicStatusWhenUpdatingSubtask() {
        Epic epic = new Epic("Test status", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.DONE, epicId);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description",
                Status.IN_PROGRESS, epicId);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        Epic savedEpic = taskManager.getEpic(epicId);
        assertEquals(Status.DONE, savedEpic.getStatus(), "Стутус эписка отличается от DONE");
    }

    @Test
    void checkEpicStatusWhenRemovingSubtask() {
        Epic epic = new Epic("Test status", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description",
                Status.IN_PROGRESS, epicId);
        taskManager.addNewSubtask(subtask1);
        int subtask2Id = taskManager.addNewSubtask(subtask2);

        taskManager.removeSubtask(subtask2Id);

        Epic savedEpic = taskManager.getEpic(epicId);
        assertEquals(Status.NEW, savedEpic.getStatus(), "Стутус эписка отличается от NEW");
    }

    @Test
    void checkEpicStatusWhenRemovingAllSubtasks() {
        Epic epic = new Epic("Test status", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.DONE, epicId);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description",
                Status.DONE, epicId);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        taskManager.removeAllSubtasks();

        Epic savedEpic = taskManager.getEpic(epicId);
        assertEquals(Status.NEW, savedEpic.getStatus(), "Стутус эписка отличается от NEW");
    }

    @Test
    void checkAddingNewSubtaskWithWrongEpicId() {
        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.DONE, 1);

        ManagerIllegalOperationException exception = assertThrows(ManagerIllegalOperationException.class,
                () -> taskManager.addNewSubtask(subtask1));

        assertEquals("Попытка добавить подзадачу с несуществующим эпиком, id эпика: 1",
                exception.getMessage());
    }

    @Test
    void checkUpdatingSubtaskWithWrongEpicId() {
        Epic epic = new Epic("Test status", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.DONE, epicId);
        taskManager.addNewSubtask(subtask1);

        subtask1.setEpicId(3);

        ManagerIllegalOperationException exception = assertThrows(ManagerIllegalOperationException.class,
                () -> taskManager.updateSubtask(subtask1));

        assertEquals("Попытка обновить подзадачу с несуществующим эпиком, id эпика: 3",
                exception.getMessage());
    }

    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", Status.NEW);
        final int taskId = taskManager.addNewTask(task);

        final Task savedTask = taskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void addNewEpic() {
        Epic epic = new Epic("Test epic", "Test epic description");
        final int epicId = taskManager.addNewEpic(epic);

        final Epic savedEpic = taskManager.getEpic(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = taskManager.getAllEpics();

        assertNotNull(epics, "Эпики на возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void addNewSubtask() {
        Epic epic = new Epic("Test epic", "Test epic description");
        final int epicId = taskManager.addNewEpic(epic);

        Subtask subtask = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epicId);
        final int subtaskId = taskManager.addNewSubtask(subtask);
        final Subtask savedSubtask = taskManager.getSubtask(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");
        assertEquals(epicId, savedSubtask.getEpicId(), "id эпиков не совпадают");
        final List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertNotNull(subtasks, "Подзадачи на возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.get(0), "Подзадачи не совпадают.");
    }

    @Test
    void getAllTasksWithNoTasksAdded() {
        List<Task> tasks = taskManager.getAllTasks();

        assertEquals(0, tasks.size(), "Размер возвращенного списка не равен нулю.");
    }

    @Test
    void getAllTasks() {
        Task task1 = new Task("Test Task1", "Test Task1 description", Status.NEW);
        Task task2 = new Task("Test Task2", "Test Task2 description", Status.NEW);

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
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epicId);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description",
                Status.IN_PROGRESS, epicId);
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
        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        Epic epic2 = new Epic("Test epic2", "Test epic2 description");

        taskManager.addNewEpic(epic1);
        taskManager.addNewEpic(epic2);

        List<Epic> epics = taskManager.getAllEpics();
        assertEquals(2, epics.size(), "Размер возвращаемого списка не равен 2");
        assertEquals(epic1, epics.get(0), "Эпики не совпадают");
        assertEquals(epic2, epics.get(1), "Эпики не совпадают");
    }

    @Test
    void removeAllTasks() {
        Task task1 = new Task("Test Task1", "Test Task1 description", Status.NEW);
        Task task2 = new Task("Test Task2", "Test Task2 description", Status.NEW);

        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        taskManager.removeAllTasks();
        List<Task> tasks = taskManager.getAllTasks();
        assertEquals(0, tasks.size(), "Размер списка после удаления не равен 0");
    }

    @Test
    void removeAllEpics() {
        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        Epic epic2 = new Epic("Test epic2", "Test epic2 description");
        int epic1Id = taskManager.addNewEpic(epic1);
        taskManager.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epic1Id);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description",
                Status.IN_PROGRESS, epic1Id);
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
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epicId);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description",
                Status.IN_PROGRESS, epicId);
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
        Task task1 = new Task("Test Task1", "Test Task1 description", Status.NEW);
        Task task2 = new Task("Test Task2", "Test Task2 description", Status.NEW);

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
        Task task1 = new Task("Test Task1", "Test Task1 description", Status.NEW);
        Task task2 = new Task("Test Task2", "Test Task2 description", Status.NEW);

        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Task task = taskManager.getTask(10);
        assertNull(task, "Задача не равна null");
    }

    @Test
    void getEpic() {
        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        Epic epic2 = new Epic("Test epic2", "Test epic2 description");
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
        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        Epic epic2 = new Epic("Test epic2", "Test epic2 description");
        taskManager.addNewEpic(epic1);
        taskManager.addNewEpic(epic2);

        Epic savedEpic = taskManager.getEpic(10);
        assertNull(savedEpic, "Эпик не равен null");
    }

    @Test
    void getSubtask() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epicId);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description",
                Status.IN_PROGRESS, epicId);
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
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epicId);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description",
                Status.IN_PROGRESS, epicId);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        Subtask subtask = taskManager.getSubtask(10);
        assertNull(subtask, "Подзадача не равна null");
    }

    @Test
    void updateTask() {
        Task task = new Task("Test Task", "Test Task description", Status.NEW);
        int taskId = taskManager.addNewTask(task);

        task.setName("Test Task changed");
        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);

        Task updatedTask = taskManager.getTask(taskId);
        assertEquals(task, updatedTask, "Задачи не совпадают.");
    }

    @Test
    void updateTaskWithEmptyTaskList() {
        Task task = new Task(1, "Test Task", "Test Task description", Status.NEW);
        ManagerIllegalOperationException exception = assertThrows(ManagerIllegalOperationException.class,
                () -> taskManager.updateTask(task));

        assertEquals("Неверный идентификатор обновляемой задачи. Задачи с id 1 не существует",
                exception.getMessage(), "Неверное сообщение исключения");
    }

    @Test
    void updateTaskWithWrongId() {
        Task task1 = new Task("Test Task1", "Test Task1 description", Status.NEW);
        Task task2 = new Task("Test Task2", "Test Task2 description", Status.NEW);

        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Task task3 = new Task(3, "Test Task3", "Test Task3 description", Status.NEW);

        ManagerIllegalOperationException exception = assertThrows(ManagerIllegalOperationException.class,
                () -> taskManager.updateTask(task3));

        assertEquals("Неверный идентификатор обновляемой задачи. Задачи с id 3 не существует",
                exception.getMessage(), "Неверное сообщение исключения");
    }

    @Test
    void updateEpic() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        epic.setName("Test epic changed");
        epic.setDescription("Test epic description changed");
        taskManager.updateEpic(epic);

        Epic updatedEpic = taskManager.getEpic(epicId);
        assertEquals(epic, updatedEpic, "Эпики не совпадают.");
    }

    @Test
    void updateEpicWithEmptyEpicList() {
        Epic epic = new Epic("Test epic", "Test epic description");
        epic.setId(1);
        ManagerIllegalOperationException exception = assertThrows(ManagerIllegalOperationException.class,
                () -> taskManager.updateEpic(epic));

        assertEquals("Неверный идентификатор обновляемого эпика. Эпика с id 1 не существует",
                exception.getMessage(), "Неверное сообщение исключения");
    }

    @Test
    void updateEpicWithWrongId() {
        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        Epic epic2 = new Epic("Test epic2", "Test epic2 description");
        taskManager.addNewEpic(epic1);
        taskManager.addNewEpic(epic2);

        Epic epic3 = new Epic("Test Epic3", "Test Epic3 description");
        epic3.setId(3);

        ManagerIllegalOperationException exception = assertThrows(ManagerIllegalOperationException.class,
                () -> taskManager.updateEpic(epic3));

        assertEquals("Неверный идентификатор обновляемого эпика. Эпика с id 3 не существует",
                exception.getMessage(), "Неверное сообщение исключения");
    }

    @Test
    void updateSubtask() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epicId);
        int subtaskId = taskManager.addNewSubtask(subtask1);

        subtask1.setName("Test Subtask1 changed");
        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);

        Subtask updatedSubtask = taskManager.getSubtask(subtaskId);

        assertEquals(Status.DONE, epic.getStatus(), "Статус эпика отличается от DONE");
        assertEquals(subtask1, updatedSubtask, "Подзадачи отличаются");
    }

    @Test
    void updateSubtaskWithEmptySubtaskList() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask(2, "Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epicId);
        ManagerIllegalOperationException exception = assertThrows(ManagerIllegalOperationException.class,
                () -> taskManager.updateSubtask(subtask1));

        assertEquals("Неверный идентификатор обновляемой подзадачи. Подзадачи с id 2 не существует",
                exception.getMessage(), "Неверное сообщение исключения");
    }

    @Test
    void updateSubtaskWithWrongId() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epicId);
        taskManager.addNewSubtask(subtask1);
        Subtask subtask2 = new Subtask(3, "Test Subtask2", "Test Subtask2 description",
                Status.IN_PROGRESS, epicId);

        ManagerIllegalOperationException exception = assertThrows(ManagerIllegalOperationException.class,
                () -> taskManager.updateSubtask(subtask2));

        assertEquals("Неверный идентификатор обновляемой подзадачи. Подзадачи с id 3 не существует",
                exception.getMessage(), "Неверное сообщение исключения");
    }

    @Test
    void removeTask() {
        Task task1 = new Task("Test Task1", "Test Task1 description", Status.NEW);
        Task task2 = new Task("Test Task2", "Test Task2 description", Status.NEW);

        int task1Id = taskManager.addNewTask(task1);
        int task2Id = taskManager.addNewTask(task2);

        boolean isRemoved = taskManager.removeTask(task1Id);
        Task removedTask = taskManager.getTask(task1Id);
        Task savedTask2 = taskManager.getTask(task2Id);

        assertTrue(isRemoved, "Задача не удалена");
        assertNull(removedTask, "Удаленная задача не равна null");
        assertEquals(savedTask2, task2, "Задачи 2 не совпадают. Возможно задача 2 была удалена");
    }

    @Test
    void removeTaskFromEmptyTaskList() {
        boolean isRemoved = taskManager.removeTask(1);
        assertFalse(isRemoved, "Задача была удалена из пустого списка задач");
    }

    @Test
    void removeTaskWithWrongId() {
        Task task = new Task("Test Task1", "Test Task1 description", Status.NEW);

        int taskId = taskManager.addNewTask(task);

        boolean isRemoved = taskManager.removeTask(10);
        Task savedTask = taskManager.getTask(taskId);
        assertFalse(isRemoved, "Задача с несуществующим id была удалена");
        assertEquals(task, savedTask, "Задачи не совпадают. Возможно, задача была удалена");
    }

    @Test
    void removeEpic() {
        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        Epic epic2 = new Epic("Test epic2", "Test epic2 description");

        int epic1Id = taskManager.addNewEpic(epic1);
        int epic2Id = taskManager.addNewEpic(epic2);

        boolean isRemoved = taskManager.removeEpic(epic1Id);
        Epic removedEpic = taskManager.getEpic(epic1Id);
        Epic savedEpic2 = taskManager.getEpic(epic2Id);

        assertTrue(isRemoved, "Эпик не удален");
        assertNull(removedEpic, "Удаленный эпик не равен null");
        assertEquals(savedEpic2, epic2, "Эпики 2 не совпадают. Возможно, эпик 2 был удален");
    }

    @Test
    void removeEpicFromEmptyEpicList() {
        boolean isRemoved = taskManager.removeEpic(1);
        assertFalse(isRemoved, "Эпик был удален из пустого списка эпиков");
    }

    @Test
    void removeEpicWithWrongId() {
        Epic epic = new Epic("Test epic1", "Test epic1 description");

        int epicId = taskManager.addNewEpic(epic);

        boolean isRemoved = taskManager.removeEpic(10);
        Epic savedEpic = taskManager.getEpic(epicId);
        assertFalse(isRemoved, "Эпик с несуществующим id был удален");
        assertEquals(epic, savedEpic, "Эпики не совпадают. Возможно, эпик был удален");
    }

    @Test
    void removeSubtask() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epicId);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description",
                Status.DONE, epicId);
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
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epicId);
        int subtaskId = taskManager.addNewSubtask(subtask1);

        boolean isRemoved = taskManager.removeSubtask(10);
        Subtask savedSubtask = taskManager.getSubtask(subtaskId);

        assertFalse(isRemoved, "Подзадача с неверным идентификатором была удалена");
        assertEquals(subtask1, savedSubtask, "Подзадачи не совпадают. Возможно, подзадача была удалена");
    }

    @Test
    void getHistory() {
        Task task1 = new Task("Test Task1", "Test Task1 description", Status.NEW);
        Task task2 = new Task("Test Task2", "Test Task2 description", Status.NEW);
        int task1Id = taskManager.addNewTask(task1);
        int task2Id = taskManager.addNewTask(task2);

        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epicId);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description",
                Status.DONE, epicId);
        int subtask1Id = taskManager.addNewSubtask(subtask1);
        int subtask2Id = taskManager.addNewSubtask(subtask2);

        taskManager.getEpic(epicId);
        taskManager.getSubtask(subtask2Id);
        taskManager.getTask(task1Id);
        taskManager.getTask(task2Id);
        taskManager.getSubtask(subtask1Id);
        taskManager.getTask(task1Id);

        List<Task> expectedHistory = List.of(epic, subtask2, task2, subtask1, task1);

        List<Task> actualHistory = taskManager.getHistory();

        assertEquals(expectedHistory, actualHistory, "Полученная история просмотра задач не совпадает" +
                "с ожидаемой");
    }

    @Test
    void getEpicSubtasks() {
        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        Epic epic2 = new Epic("Test epic2", "Test epic2 description");

        int epic1Id = taskManager.addNewEpic(epic1);
        int epic2Id = taskManager.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("Test Subtask1", "Test Subtask1 description",
                Status.IN_PROGRESS, epic1Id);
        Subtask subtask2 = new Subtask("Test Subtask2", "Test Subtask2 description",
                Status.DONE, epic1Id);
        Subtask subtask3 = new Subtask("Test Subtask3", "Test Subtask3 description",
                Status.DONE, epic2Id);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        taskManager.addNewSubtask(subtask3);

        List<Subtask> expectedSubtasks = List.of(subtask1, subtask2);

        List<Subtask> actualSubtasks = taskManager.getEpicSubtasks(epic1Id);
        assertEquals(expectedSubtasks, actualSubtasks, "Списки подзадач эпика 1 не совпадают");
    }

    @Test
    void getEpicSubtasksWhenNoSubtasksAdded() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);

        List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);

        assertEquals(0, subtasks.size(), "Размер списка подзадач не равен 0");
    }

    @Test
    void getEpicSubtasksWithWrongEpicId() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Test Subtask", "Test Subtask description",
                Status.IN_PROGRESS, epicId);
        taskManager.addNewSubtask(subtask);

        ManagerIllegalOperationException exception = assertThrows(ManagerIllegalOperationException.class,
                () -> taskManager.getEpicSubtasks(10));

        assertEquals("Попытка получить список подзадач для несуществующего эпика с id 10",
                exception.getMessage());
    }

    @Test
    void getPrioritizedTasks() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 1), 40, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 12, 0), 60, Status.NEW);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        int epic1Id = taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Test subtask1", "Test subtask1 description",
                LocalDateTime.of(2023, 7, 4, 17, 30), 40,
                Status.NEW, epic1Id);
        Subtask subtask2 = new Subtask("Test subtask2", "Test subtask2 description",
                LocalDateTime.of(2023, 7, 3, 14, 30), 90,
                Status.NEW, epic1Id);

        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        Epic epic2 = new Epic("Test epic2", "Test epic2 description");
        taskManager.addNewEpic(epic2);

        List<Task> expectedPrioritizedTasks = List.of(task2, task1, epic1, subtask2, subtask1, epic2);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Списки задач по приоритету не " +
                "совпадают");
    }

    @Test
    void checkEpicTimeCalculation() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Test subtask1", "Test subtask1 description",
                LocalDateTime.of(2023, 7, 4, 17, 30), 60,
                Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Test subtask2", "Test subtask2 description",
                LocalDateTime.of(2023, 7, 4, 16, 40), 40,
                Status.NEW, epicId);
        Subtask subtask3 = new Subtask("Test subtask3", "Test subtask3 description",
                LocalDateTime.of(2023, 7, 5, 12, 0), 100,
                Status.NEW, epicId);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        taskManager.addNewSubtask(subtask3);

        LocalDateTime startTime = epic.getStartTime();
        int duration = epic.getDuration();
        LocalDateTime endTime = epic.getEndTime();

        assertEquals(LocalDateTime.of(2023, 7, 4, 16, 40), startTime,
                "Неверное время начала эпика");
        assertEquals(200, duration, "Неверная продолжительность эпика");
        assertEquals(LocalDateTime.of(2023, 7, 5, 13, 40), endTime,
                "Неверное время окончания эпика");
    }

    @Test
    void checkTaskEndTimeCalculation() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 0), 40, Status.NEW);
        LocalDateTime endTime = task1.getEndTime();

        assertEquals(LocalDateTime.of(2023, 6, 30, 13, 40), endTime, "" +
                "Время окончания задачи не совпадает с ожидаемым");

    }

    @Test
    void checkSubtaskEndTimeCalculation() {
        Epic epic = new Epic("Test epic", "Test epic description");
        int epicId = taskManager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Test subtask1", "Test subtask1 description",
                LocalDateTime.of(2023, 7, 4, 17, 30), 60,
                Status.NEW, epicId);

        LocalDateTime endTime = subtask1.getEndTime();
        assertEquals(LocalDateTime.of(2023, 7, 4, 18, 30), endTime, "Время" +
                "окончания подзадачи не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenUpdatingTask() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 1), 40, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 12, 0), 60, Status.NEW);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        int epic1Id = taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Test subtask1", "Test subtask1 description",
                LocalDateTime.of(2023, 7, 4, 17, 30), 40,
                Status.NEW, epic1Id);

        taskManager.addNewSubtask(subtask1);

        task2.setStartTime(LocalDateTime.of(2023, 7, 4, 18, 11));
        taskManager.updateTask(task2);

        List<Task> expectedPrioritizedTasks = List.of(task1, epic1, subtask1, task2);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач" +
                "по приоритету не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenUpdatingSubtask() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 1), 40, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 12, 0), 60, Status.NEW);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        int epic1Id = taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Test subtask1", "Test subtask1 description",
                LocalDateTime.of(2023, 7, 4, 17, 30), 40,
                Status.NEW, epic1Id);

        taskManager.addNewSubtask(subtask1);

        subtask1.setStartTime(LocalDateTime.of(2023, 6, 30, 11, 15));
        taskManager.updateSubtask(subtask1);

        List<Task> expectedPrioritizedTasks = List.of(epic1, subtask1, task2, task1);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач" +
                "по приоритету не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenRemovingTask() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 1), 40, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 12, 0), 60, Status.NEW);
        taskManager.addNewTask(task1);
        int task2Id = taskManager.addNewTask(task2);

        taskManager.removeTask(task2Id);
        List<Task> expectedPrioritizedTasks = List.of(task1);

        List<Task> actualPrioritizedTask = taskManager.getPrioritizedTasks();
        assertEquals(expectedPrioritizedTasks, actualPrioritizedTask, "Полученный список задач по приоритету" +
                "не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenRemovingEpic() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 1), 40, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 12, 0), 60, Status.NEW);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Epic epic1 = new Epic("Test epic1", "Test epic1 description");
        int epic1Id = taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Test subtask1", "Test subtask1 description",
                LocalDateTime.of(2023, 7, 4, 17, 30), 40,
                Status.NEW, epic1Id);
        taskManager.addNewSubtask(subtask1);

        taskManager.removeEpic(epic1Id);

        List<Task> expectedPrioritizedTasks = List.of(task2, task1);
        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач по приоритету" +
                "не совпадает с ожидаемым");

    }

    @Test
    void checkTaskPrioritizationWhenRemovingSubtask() {
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
        int subtask1Id = taskManager.addNewSubtask(subtask1);

        taskManager.removeSubtask(subtask1Id);

        List<Task> expectedPrioritizedTasks = List.of(task2, task1, epic1);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач по приоритету" +
                "не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenRemovingAllTasks() {
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
        taskManager.addNewSubtask(subtask1);

        taskManager.removeAllTasks();

        List<Task> expectedPrioritizedTasks = List.of(epic1, subtask1);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач по приоритету" +
                "не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenRemovingAllEpics() {
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
        taskManager.addNewSubtask(subtask1);

        Epic epic2 = new Epic("Test epic2", "Test epic2 description");
        taskManager.addNewEpic(epic2);

        taskManager.removeAllEpics();

        List<Task> expectedPrioritizedTasks = List.of(task2, task1);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач по приоритету" +
                "не совпадает с ожидаемым");
    }

    @Test
    void checkTaskPrioritizationWhenRemovingAllSubtasks() {
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

        taskManager.removeAllSubtasks();

        List<Task> expectedPrioritizedTasks = List.of(task2, task1, epic1, epic2);

        List<Task> actualPrioritizedTasks = taskManager.getPrioritizedTasks();

        assertEquals(expectedPrioritizedTasks, actualPrioritizedTasks, "Полученный список задач по приоритету" +
                "не совпадает с ожидаемым");
    }

    @Test
    void checkTaskTimeIntersectionCase1() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 0), 30, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 13, 29), 60, Status.NEW);
        taskManager.addNewTask(task1);
        TimeIntersectionException exception = assertThrows(TimeIntersectionException.class,
                () -> taskManager.addNewTask(task2));

        assertEquals("Задачи с id: 1 и id: 2 пересекаются по времени выполнения", exception.getMessage());
    }

    @Test
    void checkTaskTimeIntersectionCase2() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 31), 30, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 13, 30), 60, Status.NEW);
        taskManager.addNewTask(task1);
        TimeIntersectionException exception = assertThrows(TimeIntersectionException.class,
                () -> taskManager.addNewTask(task2));

        assertEquals("Задачи с id: 1 и id: 2 пересекаются по времени выполнения", exception.getMessage());
    }

    @Test
    void checkTaskTimeIntersectionCase3() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 30), 60, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 13, 40), 30, Status.NEW);
        taskManager.addNewTask(task1);
        TimeIntersectionException exception = assertThrows(TimeIntersectionException.class,
                () -> taskManager.addNewTask(task2));

        assertEquals("Задачи с id: 1 и id: 2 пересекаются по времени выполнения", exception.getMessage());
    }

    @Test
    void checkTaskTimeIntersectionCase4() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 14, 29), 30, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 13, 30), 60, Status.NEW);
        taskManager.addNewTask(task1);
        TimeIntersectionException exception = assertThrows(TimeIntersectionException.class,
                () -> taskManager.addNewTask(task2));

        assertEquals("Задачи с id: 1 и id: 2 пересекаются по времени выполнения", exception.getMessage());
    }

    @Test
    void checkNoTaskTimeIntersectionCase1() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 0), 30, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 13, 30), 60, Status.NEW);
        taskManager.addNewTask(task1);
        assertDoesNotThrow(() -> taskManager.addNewTask(task2), "Было выброшено исключение при добавлении" +
                "задач, не пересекающихся по времени");
    }

    @Test
    void checkNoTaskTimeIntersectionCase2() {
        Task task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 14, 30), 30, Status.NEW);
        Task task2 = new Task("Test task2", "Test 2 description",
                LocalDateTime.of(2023, 6, 30, 13, 30), 60, Status.NEW);
        taskManager.addNewTask(task1);
        assertDoesNotThrow(() -> taskManager.addNewTask(task2), "Было выброшено исключение при добавлении" +
                "задач, не пересекающихся по времени");
    }
}