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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.endpoint_processor.EndpointProcessor;
import server.exceptions.RequestFailedException;
import server.exceptions.ResponseFailedException;
import service.Managers;
import service.task.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private Gson gson;
    private HttpServer httpServer;
    private static TaskManager taskManager;
    private static Logger logger;

    public HttpTaskServer() throws IOException {
        taskManager = Managers.getDefault();
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new TaskHandler());
        gson = Managers.getGson();
        logger = LogManager.getLogger(HttpTaskServer.class);
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
        private Map<Pair, EndpointProcessor> endpointMap;

        public TaskHandler() {
            endpointMap = new HashMap<>();
            fillEndpointMapWithValues();
        }

        @Override
        public void handle(HttpExchange exchange) {
            String path = exchange.getRequestURI().toString();
            EndpointProcessor endpointProcessor = getEndpointProcessor(path, exchange.getRequestMethod());
            endpointProcessor.process(exchange, path, taskManager);
        }

        private EndpointProcessor getEndpointProcessor(String path, String requestMethod) {
            for (Map.Entry<Pair, EndpointProcessor> entry : endpointMap.entrySet()) {
                Pair pair = entry.getKey();
                if (Pattern.matches(pair.getLeft(), path) && requestMethod.equals(pair.getRight())) {
                    return entry.getValue();
                }
            }
            return unknownRequestProcessor;
        }

        EndpointProcessor removeEpic = (exchange, path, taskManager) -> {
            int epicId;
            epicId = getTaskId(PathType.EPIC, path);
            boolean isRemoved = taskManager.removeEpic(epicId);
            String jsonResponse = "{\"isRemoved\": " + isRemoved + "}";
            writeResponse(exchange, jsonResponse, 200);
        };

        EndpointProcessor removeSubtask = (exchange, path, taskManager) -> {
            int subtaskId;
            subtaskId = getTaskId(PathType.SUBTASK, path);
            boolean isRemoved = taskManager.removeSubtask(subtaskId);
            String jsonResponse = "{\"isRemoved\": " + isRemoved + "}";
            writeResponse(exchange, jsonResponse, 200);
        };

        EndpointProcessor removeTask = (exchange, path, taskManager) -> {
            int taskId;
            taskId = getTaskId(PathType.TASK, path);
            boolean isRemoved = taskManager.removeTask(taskId);
            String jsonResponse = "{\"isRemoved\": " + isRemoved + "}";
            writeResponse(exchange, jsonResponse, 200);

        };

        EndpointProcessor removeAllEpics = (exchange, path, taskManager) -> {
            taskManager.removeAllEpics();
            writeResponse(exchange, "Все эпики удалены", 200);
        };

        EndpointProcessor removeAllSubtasks = (exchange, path, taskManager) -> {
            taskManager.removeAllSubtasks();
            writeResponse(exchange, "Все подзадачи удалены", 200);
        };

        EndpointProcessor removeAllTasks = (exchange, path, taskManager) -> {
            taskManager.removeAllTasks();
            writeResponse(exchange, "Все задачи удалены", 200);
        };

        EndpointProcessor addOrUpdateTask = new EndpointProcessor() {
            @Override
            public void process(HttpExchange exchange, String path, TaskManager taskManager) {

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
                    logger.error("Ошибка при чтении тела запроса при добавлении или " +
                            "обновлении задачи: " + exception.getMessage());
                    throw new RequestFailedException("При чтении тела запроса возникла ошибка: "
                            + exception.getMessage());
                }
            }
        };

        EndpointProcessor addOrUpdateSubtask = new EndpointProcessor() {
            @Override
            public void process(HttpExchange exchange, String path, TaskManager taskManager) {
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
                    logger.error("Ошибка при чтении тела запроса при добавлении или " +
                            "обновлении подзадачи: " + exception.getMessage());
                    throw new RequestFailedException("При чтении тела запроса возникла ошибка: "
                            + exception.getMessage());
                }
            }
        };

        EndpointProcessor addOrUpdateEpic = new EndpointProcessor() {
            @Override
            public void process(HttpExchange exchange, String path, TaskManager taskManager) {
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
                    logger.error("Ошибка при чтении тела запроса при добавлении или " +
                            "обновлении эпика: " + exception.getMessage());
                    throw new RequestFailedException("При чтении тела запроса возникла ошибка: "
                            + exception.getMessage());
                }
            }
        };

        EndpointProcessor getEpicSubtasks = new EndpointProcessor() {
            @Override
            public void process(HttpExchange exchange, String path, TaskManager taskManager) {
                int epicId;
                epicId = getTaskId(PathType.SUBTASKS_OF_EPIC, path);
                Type subtasksListType = new TypeToken<List<Subtask>>() {
                }.getType();
                String subtaskOfEpic = gson.toJson(taskManager.getEpicSubtasks(epicId), subtasksListType);
                writeResponse(exchange, subtaskOfEpic, 200);
            }
        };

        EndpointProcessor getHistory = (exchange, path, taskManager) -> {
            Type historyListType = new TypeToken<List<Task>>() {
            }.getType();
            String history = gson.toJson(taskManager.getHistory(), historyListType);
            writeResponse(exchange, history, 200);
        };

        EndpointProcessor getPrioritizedTasks = (exchange, path, taskManager) -> {
            Type prioritizedListType = new TypeToken<List<Task>>() {
            }.getType();
            String prioritizedTasks = gson.toJson(taskManager.getPrioritizedTasks(), prioritizedListType);
            writeResponse(exchange, prioritizedTasks, 200);
        };

        EndpointProcessor getSubtask = new EndpointProcessor() {
            @Override
            public void process(HttpExchange exchange, String path, TaskManager taskManager) {
                int subtaskId;
                subtaskId = getTaskId(PathType.SUBTASK, path);
                Type subtaskType = new TypeToken<Subtask>() {
                }.getType();
                String subtask = gson.toJson(taskManager.getSubtask(subtaskId), subtaskType);
                writeResponse(exchange, subtask, 200);
            }
        };

        EndpointProcessor getEpic = new EndpointProcessor() {
            @Override
            public void process(HttpExchange exchange, String path, TaskManager taskManager) {
                int epicId;
                epicId = getTaskId(PathType.EPIC, path);
                Type epicType = new TypeToken<Epic>() {
                }.getType();
                String epic = gson.toJson(taskManager.getEpic(epicId), epicType);
                writeResponse(exchange, epic, 200);
            }
        };

        EndpointProcessor getTask = new EndpointProcessor() {
            @Override
            public void process(HttpExchange exchange, String path, TaskManager taskManager) {
                int taskId;
                taskId = getTaskId(PathType.TASK, path);
                Type taskType = new TypeToken<Task>() {
                }.getType();
                String task = gson.toJson(taskManager.getTask(taskId), taskType);
                writeResponse(exchange, task, 200);
            }
        };

        EndpointProcessor getAllSubtasks = (exchange, path, taskManager) -> {
            Type subtaskListType = new TypeToken<List<Subtask>>() {
            }.getType();
            String subtasks = gson.toJson(taskManager.getAllSubtasks(), subtaskListType);
            writeResponse(exchange, subtasks, 200);
        };

        EndpointProcessor getAllEpics = (exchange, path, taskManager) -> {
            Type epicListType = new TypeToken<List<Epic>>() {
            }.getType();
            String epics = gson.toJson(taskManager.getAllEpics(), epicListType);
            writeResponse(exchange, epics, 200);
        };

        EndpointProcessor getAllTasks = new EndpointProcessor() {
            @Override
            public void process(HttpExchange exchange, String path, TaskManager taskManager) {
                Type taskListType = new TypeToken<List<Task>>() {
                }.getType();
                String tasks = gson.toJson(taskManager.getAllTasks(), taskListType);
                writeResponse(exchange, tasks, 200);
            }
        };

        EndpointProcessor unknownRequestProcessor = (exchange, path, taskManager)
                -> writeResponse(exchange, "Неверный запрос", 400);


        private int getTaskId(PathType type, String path) {
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
                int id = Integer.parseInt(idAsString);
                return id;
            } catch (NumberFormatException exception) {
                logger.error(String.format("При считывании id: %s произошло исключение: %s",
                        idAsString, exception.getMessage()));
                throw new RequestFailedException(String.format("При считывании id: %s произошло исключение: %s",
                        idAsString, exception.getMessage()));
            }
        }

        private void fillEndpointMapWithValues() {
            Pair getAllTasksPair = new Pair("^/tasks/task/$", "GET");
            endpointMap.put(getAllTasksPair, getAllTasks);

            Pair getAllSubtasksPair = new Pair("^/tasks/subtask/$", "GET");
            endpointMap.put(getAllSubtasksPair, getAllSubtasks);

            Pair getAllEpicsPair = new Pair("^/tasks/epic/$", "GET");
            endpointMap.put(getAllEpicsPair, getAllEpics);

            Pair historyPair = new Pair("^/tasks/history/$", "GET");
            endpointMap.put(historyPair, getHistory);

            Pair prioritizedTasksPair = new Pair("^/tasks/$", "GET");
            endpointMap.put(prioritizedTasksPair, getPrioritizedTasks);

            Pair getTaskPair = new Pair("^/tasks/task/\\?id=\\d+$", "GET");
            endpointMap.put(getTaskPair, getTask);

            Pair getEpicPair = new Pair("^/tasks/epic/\\?id=\\d+$", "GET");
            endpointMap.put(getEpicPair, getEpic);

            Pair getSubtaskPair = new Pair("^/tasks/subtask/\\?id=\\d+$", "GET");
            endpointMap.put(getSubtaskPair, getSubtask);

            Pair getEpicSubtasksPair = new Pair("^/tasks/subtask/epic/\\?id=\\d+$", "GET");
            endpointMap.put(getEpicSubtasksPair, getEpicSubtasks);

            Pair removeAllTasksPair = new Pair("^/tasks/task/$", "DELETE");
            endpointMap.put(removeAllTasksPair, removeAllTasks);

            Pair removeAllSubtaskPair = new Pair("^/tasks/subtask/$", "DELETE");
            endpointMap.put(removeAllSubtaskPair, removeAllSubtasks);

            Pair removeAllEpicsPair = new Pair("^/tasks/epic/$", "DELETE");
            endpointMap.put(removeAllEpicsPair, removeAllEpics);

            Pair removeTaskPair = new Pair("^/tasks/task/\\?id=\\d+$", "DELETE");
            endpointMap.put(removeTaskPair, removeTask);

            Pair removeEpicPair = new Pair("^/tasks/epic/\\?id=\\d+$", "DELETE");
            endpointMap.put(removeEpicPair, removeEpic);

            Pair removeSubtaskPair = new Pair("^/tasks/subtask/\\?id=\\d+$", "DELETE");
            endpointMap.put(removeSubtaskPair, removeSubtask);

            Pair postTaskPair = new Pair("^/tasks/task/$", "POST");
            endpointMap.put(postTaskPair, addOrUpdateTask);

            Pair postSubtaskPair = new Pair("^/tasks/subtask/$", "POST");
            endpointMap.put(postSubtaskPair, addOrUpdateSubtask);

            Pair postEpicPair = new Pair("^/tasks/epic/$", "POST");
            endpointMap.put(postEpicPair, addOrUpdateEpic);
        }

        private void writeResponse(HttpExchange exchange, String responseString, int responseCode) {
            if (responseString.isBlank()) {
                try {
                    exchange.sendResponseHeaders(responseCode, 0);
                } catch (IOException exception) {
                    logger.error("При отправке пустого ответа произошло исключение" + exception.getMessage());
                    throw new ResponseFailedException("При отправке пустого тела ответа произошло исключение" +
                            exception.getMessage());
                }
            } else {
                byte[] bytes = responseString.getBytes(DEFAULT_CHARSET);
                try {
                    exchange.sendResponseHeaders(responseCode, bytes.length);
                } catch (IOException exception) {
                    logger.error("При отправке заголовка ответа произошло исключение" + exception.getMessage());
                    throw new ResponseFailedException("При отправке заголовка ответа произошло исключение" +
                            exception.getMessage());
                }
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                } catch (IOException exception) {
                    logger.error("При отправке тела ответа произошло исключение" + exception.getMessage());
                    throw new ResponseFailedException("При отправке тела ответа произошло исключение" +
                            exception.getMessage());
                }
            }
            exchange.close();
        }
    }
}
