package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {

    private List<Integer> subtaskIds;

    private LocalDateTime endTime;

    public Epic(Integer id, String name, String description) {
        super(id, name, description, Status.NEW);
        setType("Epic");
        subtaskIds = new ArrayList<>();
    }

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        setType("Epic");
        subtaskIds = new ArrayList<>();
    }

    public Epic(Integer id, String name, String description, LocalDateTime startTime, int duration,
                LocalDateTime endTime, Status status) {
        super(id, name, description, startTime, duration, status);
        setType("Epic");
        this.endTime = endTime;
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void removeSubtaskId(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    public void removeAllSubtaskIds() {
        subtaskIds.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIds, epic.subtaskIds) && Objects.equals(endTime, epic.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds, endTime);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description.length=" + getDescription().length() +
                ", startTime=" + getTimeOrNull(getStartTime()) +
                ", duration=" + getDuration() +
                ", endTime=" + getTimeOrNull(getEndTime()) +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }


}
