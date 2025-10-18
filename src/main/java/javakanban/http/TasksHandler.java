package javakanban.http;

import com.google.gson.Gson;
import javakanban.manager.task.TaskManager;
import javakanban.models.Task;

import java.util.List;

public class TasksHandler extends AbstractTaskHandler<Task> {

    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson, Task.class);
    }


    @Override
    protected List<Task> getAllEntities() {
        return taskManager.getAllTasks();
    }

    @Override
    protected Task getEntityById(int id) {
        return taskManager.getTaskById(id);
    }

    @Override
    protected Task createEntity(Task entity) {
        System.out.println("Создание задачи: " + entity);
        try {
            Task result = taskManager.createTask(entity);
            System.out.println("Задача создана: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("Ошибка при создании задачи: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected Task updateEntity(Task entity) {
        return taskManager.updateTask(entity);
    }

    @Override
    protected void deleteEntity(int id) {
        taskManager.deleteTaskById(id);
    }

    @Override
    protected void setEntityId(Task entity, int id) {
        entity.setId(id);
    }

    @Override
    protected String getBasePath() {
        return "/tasks";
    }
}

