package service;

import model.*;
import java.util.ArrayList;

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
    ArrayList<Subtask> getEpicSubtasks(int epicId);
}
