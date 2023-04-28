package model;

public class Subtask extends Task {
    private int epicId;

    public Subtask(int epicId, String name, String description, Status status) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description.length=" + getDescription().length() +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }
}
