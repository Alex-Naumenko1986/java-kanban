package server;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Managers;
import service.task.TaskManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private HttpTaskServer httpTaskServer;
    TaskManager taskManager;
    HttpClient client;
    KVServer kvServer;
    Gson gson;
    private Epic epic1;
    private Epic epic2;
    private Task task1;
    private Subtask subtask1;
    private Task task2;
    private Subtask subtask2;

    @BeforeEach
    void beforeEach() {
        try {
            kvServer = new KVServer();
        } catch (IOException e) {
            System.out.println("При создании KVServer возникло исключение " + e);
        }
        kvServer.start();
        try {
            httpTaskServer = new HttpTaskServer();
        } catch (IOException e) {
            System.out.println("При создании httpTaskServer возникло исключение " + e);
        }
        client = HttpClient.newHttpClient();
        gson = Managers.getGson();
        epic1 = new Epic("Test epic1", "Test epic1 description");
        epic2 = new Epic("Test epic2", "Test epic2 description");
        task1 = new Task("Test task1", "Test task1 description",
                LocalDateTime.of(2023, 6, 30, 13, 0), 40, Status.NEW);
        task2 = new Task("Test task2", "Test task2 description",
                LocalDateTime.of(2023, 6, 30, 12, 0), 60, Status.NEW);
        subtask1 = new Subtask("Test subtask1", "Test subtask1 description",
                LocalDateTime.of(2023, 7, 4, 17, 30), 40,
                Status.NEW);
        subtask2 = new Subtask("Test subtask2", "Test subtask2 description",
                LocalDateTime.of(2023, 7, 3, 14, 30), 60,
                Status.NEW);
        httpTaskServer.start();
        taskManager = httpTaskServer.getTaskManager();
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);
        int epicId = taskManager.addNewEpic(epic1);
        subtask1.setEpicId(epicId);
        subtask2.setEpicId(epicId);
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        taskManager.addNewEpic(epic2);
    }

    @AfterEach
    void afterEach() {
        taskManager.removeAllSubtasks();
        taskManager.removeAllTasks();
        taskManager.removeAllEpics();
        httpTaskServer.stop();
        kvServer.stop();
    }

    @Test
    void getAllTasks() {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        Type taskListType = new TypeToken<List<Task>>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Task> tasks = gson.fromJson(response.body(), taskListType);
            assertFalse(tasks.isEmpty(), "Список задач пустой");
            assertEquals(2, tasks.size(), "Размер списка задач не равен 2");
            assertEquals(task1, tasks.get(0), "Задачи не совпадают");
            assertEquals(task2, tasks.get(1), "Задачи не совпадают");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getAllTaskWithEmptyTaskList() {
        taskManager.removeAllTasks();
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        Type taskListType = new TypeToken<List<Task>>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Task> tasks = gson.fromJson(response.body(), taskListType);
            assertTrue(tasks.isEmpty(), "Список задач не пустой");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getAllEpics() {
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        Type epicListType = new TypeToken<List<Epic>>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Epic> epics = gson.fromJson(response.body(), epicListType);
            assertFalse(epics.isEmpty(), "Список эпиков пустой");
            assertEquals(2, epics.size(), "Размер списка эпиков не равен 2");
            assertEquals(epic1, epics.get(0), "Эпики не совпадают");
            assertEquals(epic2, epics.get(1), "Эпики не совпадают");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getAllEpicsWithEmptyEpicList() {
        taskManager.removeAllEpics();
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        Type epicListType = new TypeToken<List<Epic>>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Epic> epics = gson.fromJson(response.body(), epicListType);
            assertTrue(epics.isEmpty(), "Список эпиков не пустой");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getAllSubtasks() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        Type subtaskListType = new TypeToken<List<Subtask>>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Subtask> subtasks = gson.fromJson(response.body(), subtaskListType);
            assertFalse(subtasks.isEmpty(), "Список подзадач пустой");
            assertEquals(2, subtasks.size(), "Размер списка подзадач не равен 2");
            assertEquals(subtask1, subtasks.get(0), "Подзадачи не совпадают");
            assertEquals(subtask2, subtasks.get(1), "Подзадачи не совпадают");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getAllSubtaskWithEmptySubtaskList() {
        taskManager.removeAllSubtasks();
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        Type subtaskListType = new TypeToken<List<Subtask>>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Subtask> subtasks = gson.fromJson(response.body(), subtaskListType);
            assertTrue(subtasks.isEmpty(), "Список подзадач не пустой");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getPrioritizedTasks() {
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        Type taskListType = new TypeToken<List<? extends Task>>() {
        }.getType();
        RuntimeTypeAdapterFactory<Task> factory = RuntimeTypeAdapterFactory.of(Task.class, "type").
                registerSubtype(Task.class, "Task").
                registerSubtype(Subtask.class, "Subtask").registerSubtype(Epic.class, "Epic");
        Gson gsonWithFactory = new GsonBuilder().registerTypeAdapterFactory(factory).create();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Task> prioritizedTasks = gsonWithFactory.fromJson(response.body(), taskListType);
            assertFalse(prioritizedTasks.isEmpty(), "Список задач по приоритету пустой");
            List<Task> expectedPrioritizedTasks = List.of(task2, task1, epic1, subtask2, subtask1, epic2);
            assertEquals(expectedPrioritizedTasks, prioritizedTasks, "Список задач по приоритету" +
                    "не совпадает");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getPrioritizedTasksWithEmptyTaskList() {
        taskManager.removeAllTasks();
        taskManager.removeAllEpics();
        taskManager.removeAllSubtasks();
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        Type taskListType = new TypeToken<List<? extends Task>>() {
        }.getType();
        RuntimeTypeAdapterFactory<Task> factory = RuntimeTypeAdapterFactory.of(Task.class, "type").
                registerSubtype(Task.class, "Task").
                registerSubtype(Subtask.class, "Subtask").registerSubtype(Epic.class, "Epic");
        Gson gsonWithFactory = new GsonBuilder().registerTypeAdapterFactory(factory).create();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Task> prioritizedTasks = gsonWithFactory.fromJson(response.body(), taskListType);
            assertTrue(prioritizedTasks.isEmpty(), "Список задач по приоритету не пустой");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getHistory() {
        taskManager.getTask(1);
        taskManager.getEpic(3);
        taskManager.getEpic(6);
        taskManager.getSubtask(4);
        URI url = URI.create("http://localhost:8080/tasks/history/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type taskListType = new TypeToken<List<? extends Task>>() {
        }.getType();
        RuntimeTypeAdapterFactory<Task> factory = RuntimeTypeAdapterFactory.of(Task.class, "type").
                registerSubtype(Task.class, "Task").
                registerSubtype(Subtask.class, "Subtask").registerSubtype(Epic.class, "Epic");
        Gson gsonWithFactory = new GsonBuilder().registerTypeAdapterFactory(factory).create();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Task> history = gsonWithFactory.fromJson(response.body(), taskListType);
            assertFalse(history.isEmpty(), "История пустая");
            List<Task> expectedHistory = List.of(task1, epic1, epic2, subtask1);
            assertEquals(expectedHistory, history, "Полученная история не совпадает с ожидаемой");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getEmptyHistory() {
        URI url = URI.create("http://localhost:8080/tasks/history/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type taskListType = new TypeToken<List<? extends Task>>() {
        }.getType();
        RuntimeTypeAdapterFactory<Task> factory = RuntimeTypeAdapterFactory.of(Task.class, "type").
                registerSubtype(Task.class, "Task").
                registerSubtype(Subtask.class, "Subtask").registerSubtype(Epic.class, "Epic");
        Gson gsonWithFactory = new GsonBuilder().registerTypeAdapterFactory(factory).create();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Task> history = gsonWithFactory.fromJson(response.body(), taskListType);
            assertTrue(history.isEmpty(), "История не пустая");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getTask() {
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type taskType = new TypeToken<Task>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Task task = gson.fromJson(response.body(), taskType);
            assertNotNull(task, "Полученная задача равна null");
            assertEquals(task1, task, "Задачи не совпадают");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getTaskWithEmptyTaskList() {
        taskManager.removeAllTasks();
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type taskType = new TypeToken<Task>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Task task = gson.fromJson(response.body(), taskType);
            assertNull(task, "Полученная задача не равна null");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getTaskWithWrongId() {
        URI url = URI.create("http://localhost:8080/tasks/task/?id=10");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type taskType = new TypeToken<Task>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Task task = gson.fromJson(response.body(), taskType);
            assertNull(task, "Полученная задача не равна null");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getSubtask() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=4");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type subtaskType = new TypeToken<Subtask>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Subtask subtask = gson.fromJson(response.body(), subtaskType);
            assertNotNull(subtask, "Полученная подзадача равна null");
            assertEquals(subtask1, subtask, "Подзадачи не совпадают");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getSubtaskWithEmptySubtaskList() {
        taskManager.removeAllSubtasks();
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=4");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type subtaskType = new TypeToken<Subtask>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Subtask subtask = gson.fromJson(response.body(), subtaskType);
            assertNull(subtask, "Полученная подзадача не равна null");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getSubtaskWithWrongId() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=10");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type subtaskType = new TypeToken<Subtask>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Subtask subtask = gson.fromJson(response.body(), subtaskType);
            assertNull(subtask, "Полученная подзадача не равна null");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getEpic() {
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type epicType = new TypeToken<Epic>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Epic epic = gson.fromJson(response.body(), epicType);
            assertNotNull(epic, "Полученный эпик равен null");
            assertEquals(epic1, epic, "Эпики не совпадают");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getEpicWithEmptyEpicList() {
        taskManager.removeAllEpics();
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type epicType = new TypeToken<Epic>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Epic epic = gson.fromJson(response.body(), epicType);
            assertNull(epic, "Полученный эпик не равен null");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getEpicWithWrongId() {
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=10");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type epicType = new TypeToken<Epic>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Epic epic = gson.fromJson(response.body(), epicType);
            assertNull(epic, "Полученный эпик не равен null");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }


    @Test
    void getEpicSubtasks() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type subtaskListType = new TypeToken<List<Subtask>>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Subtask> subtasks = gson.fromJson(response.body(), subtaskListType);
            List<Subtask> expectedSubtasks = List.of(subtask1, subtask2);
            assertFalse(subtasks.isEmpty(), "Список подзадач эпика пустой");
            assertEquals(2, subtasks.size(), "Неверный размер полученного списка подзадач");
            assertEquals(expectedSubtasks, subtasks, "Списки подзадач не совпадают");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getEpicSubtasksWithEmptySubtaskList() {
        taskManager.removeAllSubtasks();
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type subtaskListType = new TypeToken<List<Subtask>>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Subtask> subtasks = gson.fromJson(response.body(), subtaskListType);
            assertTrue(subtasks.isEmpty(), "Список подзадач эпика не пустой");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void getEpicSubtasksWithWrongEpicId() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=10");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        Type subtaskListType = new TypeToken<List<Subtask>>() {
        }.getType();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            List<Subtask> subtasks = gson.fromJson(response.body(), subtaskListType);
            assertTrue(subtasks.isEmpty(), "Список подзадач эпика не пустой");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void addTask() {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        Task task = new Task("Test task", "Test task description", Status.NEW);
        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int id = jsonObject.get("id").getAsInt();
            assertEquals(7, id, "Полученное id новой задачи не равно 7");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void addTaskWhichIntersectsWithTime() {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        Task task = new Task("Test task", "Test task description",
                LocalDateTime.of(2023, 6, 30, 13, 30), 40, Status.NEW);
        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int id = jsonObject.get("id").getAsInt();
            assertEquals(-1, id, "Задача, пересекающаяся по времени, была добавлена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void addSubtask() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Subtask subtask = new Subtask("Test subtask", "Test subtask description", Status.NEW, 3);
        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int id = jsonObject.get("id").getAsInt();
            assertEquals(7, id, "Полученное id новой подзадачи не равно 7");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void addSubtaskWhichIntersectsInTime() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Subtask subtask = new Subtask("Test subtask", "Test subtask description",
                LocalDateTime.of(2023, 7, 4, 18, 0), 40,
                Status.NEW, 3);
        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int id = jsonObject.get("id").getAsInt();
            assertEquals(-1, id, "Подзадача, пересекающаяся по времени, была добавлена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void addSubtaskWithWrongEpicId() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Subtask subtask = new Subtask("Test subtask", "Test subtask description",
                LocalDateTime.of(2023, 7, 4, 18, 0), 40,
                Status.NEW, 10);
        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int id = jsonObject.get("id").getAsInt();
            assertEquals(-1, id, "Подзадача с неверным id эпика была добавлена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void addEpic() {
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        Epic epic = new Epic("Test epic", "Test epic description");
        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int id = jsonObject.get("id").getAsInt();
            assertEquals(7, id, "Полученное id нового эпика не равно 7");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void updateTask() {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        Task task = new Task(1, "Test task", "Test task description", Status.NEW);
        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isUpdated = jsonObject.get("isUpdated").getAsBoolean();
            assertTrue(isUpdated, "Задача не обновлена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void updateTaskWithEmptyTaskList() {
        taskManager.removeAllTasks();
        URI url = URI.create("http://localhost:8080/tasks/task/");
        Task task = new Task(1, "Test task", "Test task description", Status.NEW);
        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isUpdated = jsonObject.get("isUpdated").getAsBoolean();
            assertFalse(isUpdated, "Задача была обновлена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void updateTaskWithWrongId() {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        Task task = new Task(10, "Test task", "Test task description", Status.NEW);
        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isUpdated = jsonObject.get("isUpdated").getAsBoolean();
            assertFalse(isUpdated, "Задача с неверным идентификатором была обновлена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void updateTaskWhichIntersectsInTime() {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        Task task = new Task(2, "Test task", "Test task description",
                LocalDateTime.of(2023, 6, 30, 13, 30), 40, Status.NEW);

        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isUpdated = jsonObject.get("isUpdated").getAsBoolean();
            assertFalse(isUpdated, "Задача была обновлена с пересечением по времени");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void updateSubtask() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Subtask subtask = new Subtask(4, "Test subtask", "Test subtask description",
                LocalDateTime.of(2023, 7, 4, 17, 30), 40,
                Status.NEW, 3);
        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isUpdated = jsonObject.get("isUpdated").getAsBoolean();
            assertTrue(isUpdated, "Подзадача не обновлена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void updateSubtaskWithWrongId() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Subtask subtask = new Subtask(10, "Test subtask", "Test subtask description",
                LocalDateTime.of(2023, 7, 4, 17, 30), 40,
                Status.NEW, 3);
        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isUpdated = jsonObject.get("isUpdated").getAsBoolean();
            assertFalse(isUpdated, "Подзадача с неверным идентификатором была обновлена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void updateSubtaskWithWrongEpicId() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Subtask subtask = new Subtask(4, "Test subtask", "Test subtask description",
                LocalDateTime.of(2023, 7, 4, 17, 30), 40,
                Status.NEW, 10);
        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isUpdated = jsonObject.get("isUpdated").getAsBoolean();
            assertFalse(isUpdated, "Подзадача с неверным идентификатором эпика была обновлена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void updateSubtaskWithEmptySubtaskList() {
        taskManager.removeAllSubtasks();
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Subtask subtask = new Subtask(4, "Test subtask", "Test subtask description",
                LocalDateTime.of(2023, 7, 4, 17, 30), 40,
                Status.NEW, 3);
        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isUpdated = jsonObject.get("isUpdated").getAsBoolean();
            assertFalse(isUpdated, "Подзадача, пересекающаяся по времени, была обновлена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void updateSubtaskWhichIntersectsInTime() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Subtask subtask = new Subtask(5, "Test subtask", "Test subtask description",
                LocalDateTime.of(2023, 7, 4, 17, 0), 40,
                Status.NEW, 3);
        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isUpdated = jsonObject.get("isUpdated").getAsBoolean();
            assertFalse(isUpdated, "Подзадача, пересекающаяся по времени, была обновлена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void updateEpic() {
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        Epic epic = new Epic(3, "Test epic", "Test epic description");
        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isUpdated = jsonObject.get("isUpdated").getAsBoolean();
            assertTrue(isUpdated, "Эпик не был обновлен");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void updateEpicWithEmptyEpicList() {
        taskManager.removeAllEpics();
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        Epic epic = new Epic(3, "Test epic", "Test epic description");
        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isUpdated = jsonObject.get("isUpdated").getAsBoolean();
            assertFalse(isUpdated, "Эпик был обновлен");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void updateEpicWithWrongId() {
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        Epic epic = new Epic(10, "Test epic", "Test epic description");
        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isUpdated = jsonObject.get("isUpdated").getAsBoolean();
            assertFalse(isUpdated, "Эпик был обновлен");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void removeAllTasks() {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseAsString = response.body();
            assertEquals("Все задачи удалены", responseAsString, "Сообщение от сервера не совпадает" +
                    "с ожидаемым");
            assertTrue(taskManager.getAllTasks().isEmpty(), "Список задач не пустой");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void removeAllSubtasks() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseAsString = response.body();
            assertEquals("Все подзадачи удалены", responseAsString, "Сообщение от сервера не " +
                    "совпадает с ожидаемым");
            assertTrue(taskManager.getAllSubtasks().isEmpty(), "Список подзадач не пустой");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void removeAllEpics() {
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseAsString = response.body();
            assertEquals("Все эпики удалены", responseAsString, "Сообщение от сервера не " +
                    "совпадает с ожидаемым");
            assertTrue(taskManager.getAllEpics().isEmpty(), "Список эпиков не пустой");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void removeTask() {
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isRemoved = jsonObject.get("isRemoved").getAsBoolean();
            assertTrue(isRemoved, "Задача не была удалена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void removeTaskFromEmptyTaskList() {
        taskManager.removeAllTasks();
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isRemoved = jsonObject.get("isRemoved").getAsBoolean();
            assertFalse(isRemoved, "Задача была удалена из пустого списка задач");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void removeTaskWithWrongId() {
        URI url = URI.create("http://localhost:8080/tasks/task/?id=10");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isRemoved = jsonObject.get("isRemoved").getAsBoolean();
            assertFalse(isRemoved, "Задача была удалена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void removeSubtask() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=4");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isRemoved = jsonObject.get("isRemoved").getAsBoolean();
            assertTrue(isRemoved, "Подзадача не была удалена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void removeSubtaskWithWrongId() {
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=10");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isRemoved = jsonObject.get("isRemoved").getAsBoolean();
            assertFalse(isRemoved, "Подзадача с неверным идентификатором была удалена");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void removeSubtaskWithEmptySubtaskList() {
        taskManager.removeAllSubtasks();
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=4");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isRemoved = jsonObject.get("isRemoved").getAsBoolean();
            assertFalse(isRemoved, "Подзадача была удалена из пустого списка");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void removeEpic() {
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isRemoved = jsonObject.get("isRemoved").getAsBoolean();
            assertTrue(isRemoved, "Эпик не был удален");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void removeEpicWithEmptyEpicList() {
        taskManager.removeAllEpics();
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isRemoved = jsonObject.get("isRemoved").getAsBoolean();
            assertFalse(isRemoved, "Эпик был удален из пустого списка");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void removeEpicWithWrongId() {
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=10");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            boolean isRemoved = jsonObject.get("isRemoved").getAsBoolean();
            assertFalse(isRemoved, "Эпик c неверным идентификатором был удален");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }

    @Test
    void badRequest() {
        URI url = URI.create("http://localhost:8080/tasks/wrong");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseAsString = response.body();
            assertEquals("Неверный запрос", responseAsString, "Ответ сервера не совпадает" +
                    "с ожидаемым");
        } catch (IOException | InterruptedException e) {
            System.out.println("При отправке запроса на сервер возникло ислючение: " + e);
            ;
        }
    }
}