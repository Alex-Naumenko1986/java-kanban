package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Epic;
import model.Subtask;
import model.Task;
import service.Managers;
import service.task.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private Gson gson;
    private HttpServer httpServer;
    private TaskManager taskManager;

    public HttpTaskServer() throws IOException {
        taskManager = Managers.getDefault();
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TaskHandler());
        gson = Managers.getGson();
    }

    public void start() {
        httpServer.start();
        System.out.println("HttpTaskServer запущен на порту " + PORT);
    }

    public void stop() {
        httpServer.stop(1);
        System.out.println("HTTPTaskServer оставновлен");
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            String path = exchange.getRequestURI().toString();
            Endpoint endpoint = getEndpoint(path, exchange.getRequestMethod());
            switch (endpoint) {
                case GET_ALL_TASKS:
                    getAllTasks(exchange);
                    break;
                case GET_ALL_EPICS:
                    getAllEpics(exchange);
                    break;
                case GET_ALL_SUBTASKS:
                    getAllSubtasks(exchange);
                    break;
                case GET_TASK:
                    getTask(exchange, path);
                    break;
                case GET_EPIC:
                    getEpic(exchange, path);
                    break;
                case GET_SUBTASK:
                    getSubtask(exchange, path);
                    break;
                case HISTORY:
                    getHistory(exchange);
                    break;
                case GET_EPIC_SUBTASKS:
                    getEpicSubtasks(exchange, path);
                    break;
                case GET_PRIORITIZED_TASKS:
                    getPrioritizedTasks(exchange);
                case ADD_OR_UPDATE_TASK:
                    addOrUpdateTask(exchange);
                    break;
                case ADD_OR_UPDATE_SUBTASK:
                    addOrUpdateSubtask(exchange);
                    break;
                case ADD_OR_UPDATE_EPIC:
                    addOrUpdateEpic(exchange);
                    break;
                case REMOVE_ALL_TASKS:
                    removeAllTasks(exchange);
                    break;
                case REMOVE_ALL_SUBTASKS:
                    removeAllSubtasks(exchange);
                    break;
                case REMOVE_ALL_EPICS:
                    removeAllEpics(exchange);
                    break;
                case REMOVE_TASK:
                    removeTask(exchange, path);
                    break;
                case REMOVE_SUBTASK:
                    removeSubtask(exchange, path);
                    break;
                case REMOVE_EPIC:
                    removeEpic(exchange, path);
                    break;
                case UNKNOWN:
                    writeResponse(exchange, "Неверный запрос", 400);
            }
        }

        private void removeEpic(HttpExchange exchange, String path) {
            int epicId;
            epicId = getTaskId(PathType.EPIC, path);
            if (epicId != -1) {
                boolean isRemoved = taskManager.removeEpic(epicId);
                String jsonResponse = "{\"isRemoved\": " + isRemoved + "}";
                writeResponse(exchange, jsonResponse, 200);
            } else {
                writeResponse(exchange, "Получен неверный идентификатор эпика", 400);
            }
        }

        private void removeSubtask(HttpExchange exchange, String path) {
            int subtaskId;
            subtaskId = getTaskId(PathType.SUBTASK, path);
            if (subtaskId != -1) {
                boolean isRemoved = taskManager.removeSubtask(subtaskId);
                String jsonResponse = "{\"isRemoved\": " + isRemoved + "}";
                writeResponse(exchange, jsonResponse, 200);
            } else {
                writeResponse(exchange, "Получен неверный идентификатор подзадачи", 400);
            }
        }

        private void removeTask(HttpExchange exchange, String path) {
            int taskId;
            taskId = getTaskId(PathType.TASK, path);
            if (taskId != -1) {
                boolean isRemoved = taskManager.removeTask(taskId);
                String jsonResponse = "{\"isRemoved\": " + isRemoved + "}";
                writeResponse(exchange, jsonResponse, 200);
            } else {
                writeResponse(exchange, "Получен неверный идентификатор задачи", 400);
            }
        }

        private void removeAllEpics(HttpExchange exchange) {
            taskManager.removeAllEpics();
            writeResponse(exchange, "Все эпики удалены", 200);
        }

        private void removeAllSubtasks(HttpExchange exchange) {
            taskManager.removeAllSubtasks();
            writeResponse(exchange, "Все подзадачи удалены", 200);
        }

        private void removeAllTasks(HttpExchange exchange) {
            taskManager.removeAllTasks();
            writeResponse(exchange, "Все задачи удалены", 200);
        }

        private void addOrUpdateTask(HttpExchange exchange) {
            InputStream inputStream = exchange.getRequestBody();
            Type taskType = new TypeToken<Task>() {
            }.getType();
            try {
                String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                Task task = gson.fromJson(body, taskType);
                if (task.getId() == null) {
                    int id = taskManager.addNewTask(task);
                    String jsonResponse = "{\"id\": " + id + "}";
                    writeResponse(exchange, jsonResponse, 200);
                } else {
                    boolean isUpdated = taskManager.updateTask(task);
                    String jsonResponse = "{\"isUpdated\": " + isUpdated + "}";
                    writeResponse(exchange, jsonResponse, 200);
                }
            } catch (IOException | JsonSyntaxException exception) {
                System.out.println("Ошибка при чтении тела запроса: " + exception.getMessage());
                ;
            }
        }

        private void addOrUpdateSubtask(HttpExchange exchange) {
            InputStream inputStream = exchange.getRequestBody();
            Type subtaskType = new TypeToken<Subtask>() {
            }.getType();
            try {
                String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                Subtask subtask = gson.fromJson(body, subtaskType);
                if (subtask.getId() == null) {
                    int id = taskManager.addNewSubtask(subtask);
                    String jsonResponse = "{\"id\": " + id + "}";
                    writeResponse(exchange, jsonResponse, 200);
                } else {
                    boolean isUpdated = taskManager.updateSubtask(subtask);
                    String jsonResponse = "{\"isUpdated\": " + isUpdated + "}";
                    writeResponse(exchange, jsonResponse, 200);
                }
            } catch (IOException | JsonSyntaxException exception) {
                System.out.println("Ошибка при чтении тела запроса: " + exception.getMessage());
                ;
            }
        }

        private void addOrUpdateEpic(HttpExchange exchange) {
            InputStream inputStream = exchange.getRequestBody();
            Type epicType = new TypeToken<Epic>() {
            }.getType();
            try {
                String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                Epic epic = gson.fromJson(body, epicType);
                if (epic.getId() == null) {
                    int id = taskManager.addNewEpic(epic);
                    String jsonResponse = "{\"id\": " + id + "}";
                    writeResponse(exchange, jsonResponse, 200);
                } else {
                    boolean isUpdated = taskManager.updateEpic(epic);
                    String jsonResponse = "{\"isUpdated\": " + isUpdated + "}";
                    writeResponse(exchange, jsonResponse, 200);
                }
            } catch (IOException | JsonSyntaxException exception) {
                System.out.println("Ошибка при чтении тела запроса: " + exception.getMessage());
                ;
            }
        }

        private void getEpicSubtasks(HttpExchange exchange, String path) {
            int epicId;
            epicId = getTaskId(PathType.SUBTASKS_OF_EPIC, path);
            Type subtasksListType = new TypeToken<List<Subtask>>() {
            }.getType();
            if (epicId != -1) {
                String subtaskOfEpic = gson.toJson(taskManager.getEpicSubtasks(epicId), subtasksListType);
                writeResponse(exchange, subtaskOfEpic, 200);
            }
        }

        private void getHistory(HttpExchange exchange) {
            Type historyListType = new TypeToken<List<Task>>() {
            }.getType();
            String history = gson.toJson(taskManager.getHistory(), historyListType);
            writeResponse(exchange, history, 200);
        }

        private void getPrioritizedTasks(HttpExchange exchange) {
            Type prioritizedListType = new TypeToken<List<Task>>() {
            }.getType();
            String prioritizedTasks = gson.toJson(taskManager.getPrioritizedTasks(), prioritizedListType);
            writeResponse(exchange, prioritizedTasks, 200);
        }

        private void getSubtask(HttpExchange exchange, String path) {
            int subtaskId;
            subtaskId = getTaskId(PathType.SUBTASK, path);
            if (subtaskId != -1) {
                Type subtaskType = new TypeToken<Subtask>() {
                }.getType();
                String subtask = gson.toJson(taskManager.getSubtask(subtaskId), subtaskType);
                writeResponse(exchange, subtask, 200);
            } else {
                writeResponse(exchange, "Неверный идентификатор подзадачи в запросе", 400);
            }
        }

        private void getEpic(HttpExchange exchange, String path) {
            int epicId;
            epicId = getTaskId(PathType.EPIC, path);
            if (epicId != -1) {
                Type epicType = new TypeToken<Epic>() {
                }.getType();
                String epic = gson.toJson(taskManager.getEpic(epicId), epicType);
                writeResponse(exchange, epic, 200);
            } else {
                writeResponse(exchange, "Неверный идентификатор эпика в запросе", 400);
            }
        }

        private void getTask(HttpExchange exchange, String path) {
            int taskId;
            taskId = getTaskId(PathType.TASK, path);
            if (taskId != -1) {
                Type taskType = new TypeToken<Task>() {
                }.getType();
                String task = gson.toJson(taskManager.getTask(taskId), taskType);
                writeResponse(exchange, task, 200);
            } else {
                writeResponse(exchange, "Неверный идентификтор задачи в запросе", 400);
            }
        }

        private void getAllSubtasks(HttpExchange exchange) {
            Type subtaskListType = new TypeToken<List<Subtask>>() {
            }.getType();
            String subtasks = gson.toJson(taskManager.getAllSubtasks(), subtaskListType);
            writeResponse(exchange, subtasks, 200);
        }

        private void getAllEpics(HttpExchange exchange) {
            Type epicListType = new TypeToken<List<Epic>>() {
            }.getType();
            String epics = gson.toJson(taskManager.getAllEpics(), epicListType);
            writeResponse(exchange, epics, 200);
        }

        private void getAllTasks(HttpExchange exchange) {
            Type taskListType = new TypeToken<List<Task>>() {
            }.getType();
            String tasks = gson.toJson(taskManager.getAllTasks(), taskListType);
            writeResponse(exchange, tasks, 200);
        }

        private int getTaskId(PathType type, String path) {
            int id = -1;
            String idAsString = "";
            switch (type) {
                case TASK:
                    idAsString = path.replace("/tasks/task/?id=", "");
                    break;
                case SUBTASK:
                    idAsString = path.replace("/tasks/subtask/?id=", "");
                    break;
                case EPIC:
                    idAsString = path.replace("/tasks/epic/?id=", "");
                    break;
                case SUBTASKS_OF_EPIC:
                    idAsString = path.replace("/tasks/subtask/epic/?id=", "");
            }
            try {
                id = Integer.parseInt(idAsString);
                return id;
            } catch (NumberFormatException exception) {
                System.out.println(String.format("При считывании id: %s произошло исключение: %s",
                        idAsString, exception.getMessage()));
            }
            return -id;
        }

        private Endpoint getEndpoint(String path, String requestMethod) {
            switch (requestMethod) {
                case "GET":
                    if (Pattern.matches("^/tasks/task/$", path)) {
                        return Endpoint.GET_ALL_TASKS;
                    }
                    if (Pattern.matches("^/tasks/subtask/$", path)) {
                        return Endpoint.GET_ALL_SUBTASKS;
                    }
                    if (Pattern.matches("^/tasks/epic/$", path)) {
                        return Endpoint.GET_ALL_EPICS;
                    }
                    if (Pattern.matches("^/tasks/history/$", path)) {
                        return Endpoint.HISTORY;
                    }
                    if (Pattern.matches("^/tasks/$", path)) {
                        return Endpoint.GET_PRIORITIZED_TASKS;
                    }
                    if (Pattern.matches("^/tasks/task/\\?id=\\d+$", path)) {
                        return Endpoint.GET_TASK;
                    }
                    if (Pattern.matches("^/tasks/epic/\\?id=\\d+$", path)) {
                        return Endpoint.GET_EPIC;
                    }
                    if (Pattern.matches("^/tasks/subtask/\\?id=\\d+$", path)) {
                        return Endpoint.GET_SUBTASK;
                    }
                    if (Pattern.matches("^/tasks/subtask/epic/\\?id=\\d+$", path)) {
                        return Endpoint.GET_EPIC_SUBTASKS;
                    }
                case "DELETE":
                    if (Pattern.matches("^/tasks/task/$", path)) {
                        return Endpoint.REMOVE_ALL_TASKS;
                    }
                    if (Pattern.matches("^/tasks/subtask/$", path)) {
                        return Endpoint.REMOVE_ALL_SUBTASKS;
                    }
                    if (Pattern.matches("^/tasks/epic/$", path)) {
                        return Endpoint.REMOVE_ALL_EPICS;
                    }
                    if (Pattern.matches("^/tasks/task/\\?id=\\d+$", path)) {
                        return Endpoint.REMOVE_TASK;
                    }
                    if (Pattern.matches("^/tasks/epic/\\?id=\\d+$", path)) {
                        return Endpoint.REMOVE_EPIC;
                    }
                    if (Pattern.matches("^/tasks/subtask/\\?id=\\d+$", path)) {
                        return Endpoint.REMOVE_SUBTASK;
                    }
                case "POST":
                    if (Pattern.matches("^/tasks/task/$", path)) {
                        return Endpoint.ADD_OR_UPDATE_TASK;
                    }
                    if (Pattern.matches("^/tasks/subtask/$", path)) {
                        return Endpoint.ADD_OR_UPDATE_SUBTASK;
                    }
                    if (Pattern.matches("^/tasks/epic/$", path)) {
                        return Endpoint.ADD_OR_UPDATE_EPIC;
                    }
                default:
                    return Endpoint.UNKNOWN;
            }
        }

        private void writeResponse(HttpExchange exchange, String responseString, int responseCode) {
            if (responseString.isBlank()) {
                try {
                    exchange.sendResponseHeaders(responseCode, 0);
                } catch (IOException exception) {
                    System.out.println("При отправке ответа произошло исключение" + exception.getMessage());
                }
            } else {
                byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
                try {
                    exchange.sendResponseHeaders(responseCode, bytes.length);
                } catch (IOException exception) {
                    System.out.println("При отправке ответа произошло исключение" + exception.getMessage());
                }
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                } catch (IOException exception) {
                    System.out.println("При отправке ответа произошло исключение" + exception.getMessage());
                }
            }
            exchange.close();
        }
    }
}
