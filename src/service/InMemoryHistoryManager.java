package service;

import model.Task;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{
    private final int historyMaxSize;
    private List<Task> taskHistory;

    public InMemoryHistoryManager() {
        historyMaxSize = 10;
        taskHistory = new LinkedList<>();
    }

    @Override
    public void add(Task task) {
        taskHistory.add(task);
        if (taskHistory.size() > historyMaxSize) {
            taskHistory.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory;
    }
}
