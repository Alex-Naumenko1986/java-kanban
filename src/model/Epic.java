package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void removeSubtaskId(int id) {
        subtaskIds.remove(Integer.valueOf(id));
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
