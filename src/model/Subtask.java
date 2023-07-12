package model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    private int epicId;

    public Subtask(Integer id, String name, String description, Status status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, Status status) {
        super(name, description, status);
        this.epicId = 0;
    }

    public Subtask(String name, String description, Status status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(Integer id, String name, String description, LocalDateTime startTime, int duration,
                   Status status, int epicId) {
        super(id, name, description, startTime, duration, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, LocalDateTime startTime, int duration, Status status, int epicId) {
        super(name, description, startTime, duration, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, LocalDateTime startTime, int duration, Status status) {
        super(name, description, startTime, duration, status);
        this.epicId = 0;
    }


    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description.length=" + getDescription().length() +
                ", startTime=" + getTimeOrNull(getStartTime()) +
                ", duration=" + getDuration() +
                ", endTime=" + getTimeOrNull(getEndTime()) +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }
}
