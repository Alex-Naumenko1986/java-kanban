package model;

import java.util.HashSet;

public class Epic extends Task {
    private HashSet<Integer> subtaskIds;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        subtaskIds = new HashSet<>();
    }

    public HashSet<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubTaskId(int id) {
        subtaskIds.add(id);
    }

    public void removeSubTaskId(int id) {
        subtaskIds.remove(id);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description.length=" + getDescription().length() +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}
