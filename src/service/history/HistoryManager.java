package service.history;

import model.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    boolean remove(int id);

    List<Task> getHistory();
}
