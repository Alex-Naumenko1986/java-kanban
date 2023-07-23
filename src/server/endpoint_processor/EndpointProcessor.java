package server.endpoint_processor;


import com.sun.net.httpserver.HttpExchange;
import service.task.TaskManager;

public interface EndpointProcessor {
    void process(HttpExchange exchange, String path, TaskManager taskManager);
}
