package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task implements Comparable<Task> {
    private String type;
    private Integer id;
    private String name;

    private String description;

    private LocalDateTime startTime;

    private int duration;

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm");


    private Status status;

    public Task() {
        this.type = "Task";
    }

    public Task(Integer id, String name, String description, Status status) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = null;
        this.duration = 0;
    }

    public Task(String name, String description, Status status) {
        this();
        this.id = null;
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = null;
        this.duration = 0;
    }

    public Task(Integer id, String name, String description, LocalDateTime startTime, int duration, Status status) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
        this.status = status;
    }

    public Task(String name, String description, LocalDateTime startTime, int duration, Status status) {
        this();
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.duration = duration;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        if (startTime != null) {
            return startTime.plus(Duration.ofMinutes(duration));
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description.length=" + description.length() +
                ", startTime=" + getTimeOrNull(startTime) +
                ", duration=" + duration +
                ", endTime=" + getTimeOrNull(getEndTime()) +
                ", status=" + status +
                '}';
    }

    public String getTimeOrNull(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return localDateTime.format(formatter);
        } else {
            return "null";
        }
    }

    @Override
    public int compareTo(Task o) {
        if (this.equals(o)) {
            return 0;
        }

        if (this.startTime == null && o.startTime == null) {
            return Integer.compare(this.id, o.id);
        }

        if (this.startTime == null) {
            return 1;
        }

        if (o.startTime == null) {
            return -1;
        }

        if (this.startTime.isEqual(o.startTime)) {
            return Integer.compare(this.id, o.id);
        }
        ;

        return this.startTime.compareTo(o.startTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return duration == task.duration && Objects.equals(id, task.id)
                && Objects.equals(name, task.name)
                && Objects.equals(description, task.description)
                && Objects.equals(startTime, task.startTime)
                && status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, startTime, duration, status);
    }

    public boolean intersectsWith(Task task) {

        if (this.startTime == null || task.startTime == null) {
            return false;
        }

        if (this.startTime.isBefore(task.startTime) && this.getEndTime().isAfter(task.startTime)) {
            return true;
        }

        if (this.startTime.isBefore(task.getEndTime()) && this.getEndTime().isAfter(task.getEndTime())) {
            return true;
        }

        if (this.startTime.isBefore(task.startTime) && this.getEndTime().isAfter(task.getEndTime())) {
            return true;
        }

        if (this.startTime.isAfter(task.startTime) && this.getEndTime().isBefore(task.getEndTime())) {
            return true;
        }
        ;

        return false;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
