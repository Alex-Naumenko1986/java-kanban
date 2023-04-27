package model;

public class Task {
    private Integer id;
    private String name;
    private String description;
    private Status status;

    public Task(String name, String description, Status status) {
        this.id = null;
        this.name = name;
        this.description = description;
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

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description.length=" + description.length() +
                ", status=" + status +
                '}';
    }
}
