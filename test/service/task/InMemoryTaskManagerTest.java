package service.task;

import org.junit.jupiter.api.BeforeEach;

public class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {
    TaskManager taskManager = new InMemoryTaskManager();

    @BeforeEach
    public void setTaskManager() {
        super.setTaskManager(taskManager);
    }

}