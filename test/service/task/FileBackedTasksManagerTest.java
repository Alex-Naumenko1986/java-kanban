package service.task;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.task.exceptions.ManagerSaveException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

//Исключительные случаи, такие как удаление задачи из пустого списка, тестируются в классе TaskManagerTest

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
        taskManager.addNewEpic(epic1);

        taskManager = Managers.loadTaskManagerFromFile();
        List<Epic> epics = taskManager.getAllEpics();
        assertEquals(1, epics.size(), "Размер полученного списка эпиков не равен 1");
        assertEquals(epic1, epics.get(0), "Эпики не совпадают");
    }

    @Test
    void loadingWithEmptyHistory() {
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);
        taskManager.addNewEpic(epic1);

        taskManager = Managers.loadTaskManagerFromFile();

        List<Task> history = taskManager.getHistory();
        assertEquals(0, history.size(), "Размер полученной истории не равен 0");
    }

    @Test
    void checkLoadingTasks() {
        taskManager.addNewTask(taskWithTime1);
        taskManager.addNewTask(taskWithTime2);

        int epic1Id = taskManager.addNewEpic(epic1);
        subtaskWithTime1.setEpicId(epic1Id);
        subtaskWithTime2.setEpicId(epic1Id);
        taskManager.addNewSubtask(subtaskWithTime1);
        taskManager.addNewSubtask(subtaskWithTime2);

        taskManager.addNewEpic(epic2);

        taskManager = Managers.loadTaskManagerFromFile();
        List<Task> savedTasks = taskManager.getAllTasks();
        List<Epic> savedEpics = taskManager.getAllEpics();
        List<Subtask> savedSubtasks = taskManager.getAllSubtasks();

        List<Task> expectedTasks = List.of(taskWithTime1, taskWithTime2);
        List<Epic> expectedEpics = List.of(epic1, epic2);
        List<Subtask> expectedSubtasks = List.of(subtaskWithTime1, subtaskWithTime2);

        assertEquals(expectedTasks, savedTasks, "Списки задач не совпадают");
        assertEquals(expectedEpics, savedEpics, "Списки эпиков не совпадают");
        assertEquals(expectedSubtasks, savedSubtasks, "Списки подзадач не совпадают");
    }

    @Test
    void checkLoadingHistory() {
        int task1Id = taskManager.addNewTask(taskWithTime1);
        int task2Id = taskManager.addNewTask(taskWithTime2);

        int epic1Id = taskManager.addNewEpic(epic1);
        subtaskWithTime1.setEpicId(epic1Id);
        subtaskWithTime2.setEpicId(epic1Id);
        int subtask1Id = taskManager.addNewSubtask(subtaskWithTime1);
        int subtask2Id = taskManager.addNewSubtask(subtaskWithTime2);

        taskManager.getTask(task2Id);
        taskManager.getTask(task1Id);
        taskManager.getSubtask(subtask1Id);
        taskManager.getEpic(epic1Id);
        taskManager.getSubtask(subtask2Id);
        taskManager.getTask(task2Id);

        taskManager = Managers.loadTaskManagerFromFile();

        List<Task> expectedHistory = List.of(taskWithTime1, subtaskWithTime1, epic1, subtaskWithTime2, taskWithTime2);
        List<Task> loadedHistory = taskManager.getHistory();

        assertEquals(expectedHistory, loadedHistory, "Загруженная история не совпадает с ожидаемой");
    }

    @Test
    void checkLoadingPrioritizedTasks() {
        taskManager.addNewTask(taskWithTime1);
        taskManager.addNewTask(taskWithTime2);

        int epic1Id = taskManager.addNewEpic(epic1);
        subtaskWithTime1.setEpicId(epic1Id);
        subtaskWithTime2.setEpicId(epic1Id);
        taskManager.addNewSubtask(subtaskWithTime1);
        taskManager.addNewSubtask(subtaskWithTime2);

        taskManager = Managers.loadTaskManagerFromFile();

        List<Task> loadedPrioritizedTasks = taskManager.getPrioritizedTasks();
        List<Task> expectedPrioritizedTasks = List.of(taskWithTime2, taskWithTime1, epic1, subtaskWithTime2,
                subtaskWithTime1);
        assertEquals(expectedPrioritizedTasks, loadedPrioritizedTasks, "Загруженный список задач по " +
                "приоритету не совпадает с ожидаемым");
    }

    @Test
    void checkLoadingWithInvalidFileContents() {
        createInvalidFile();
        ManagerSaveException exception = assertThrows(ManagerSaveException.class,
                Managers::loadTaskManagerFromFile);
        assertEquals("Неверный формат файла: resources\\tasks.csv. " +
                "Недостаточное количество элементов в строке: 2", exception.getMessage(), "Неверное сообщение" +
                "исключения");
    }

    private void createInvalidFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("id,type,name,status,description,startTime,duration,endTime,epic");
        sb.append("\n");
        sb.append("2,TASK,Test task2,Test 2 description,2023-06-30T12:00,60,2023-06-30T13:00");
        sb.append("\n\n");
        sb.append("null");

        try (FileWriter fileWriter = new FileWriter("resources/tasks.csv");
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(sb.toString());
        } catch (IOException e) {
            throw new ManagerSaveException(String.format("Ошибка при попытке записи в файл: %s" +
                    ", произошло исключение: %s", "resources/tasks.csv", e));
        }
    }
}