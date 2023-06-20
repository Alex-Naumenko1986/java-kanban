package service.task;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    ArrayList<Task> getAllTasks();
    ArrayList<Subtask> getAllSubtasks();
    ArrayList<Epic> getAllEpics();
    void removeAllTasks();
    void removeAllSubtasks();
    void removeAllEpics();
    Task getTask(int id);
    Subtask getSubtask(int id);
    Epic getEpic(int id);
    void createNewTask(Task task);
    void createNewEpic(Epic epic);
    void createNewSubtask(Subtask subtask);
    void updateTask(Task task);
    void updateEpic(Epic epic);
    void updateSubtask(Subtask subtask);
    void removeTask(int id);
    void removeEpic(int id);

    void removeSubtask(int id);

    List<Task> getHistory();

    ArrayList<Subtask> getEpicSubtasks(int epicId);
}
