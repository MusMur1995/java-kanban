package javakanban.http;

import com.google.gson.Gson;
import javakanban.manager.task.TaskManager;
import javakanban.models.Subtask;

import java.util.List;

public class SubtasksHandler extends AbstractTaskHandler<Subtask> {

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson, Subtask.class);
    }

    @Override
    protected List<Subtask> getAllEntities() {
        return taskManager.getAllSubtasks();
    }

    @Override
    protected Subtask getEntityById(int id) {
        return taskManager.getSubtaskById(id);
    }

    @Override
    protected Subtask createEntity(Subtask entity) {
        return taskManager.createSubtask(entity);
    }

    @Override
    protected Subtask updateEntity(Subtask entity) {
        return taskManager.updateSubtask(entity);
    }

    @Override
    protected void deleteEntity(int id) {
        taskManager.deleteSubtaskById(id);
    }

    @Override
    protected void setEntityId(Subtask entity, int id) {
        entity.setId(id);
    }

    @Override
    protected String getBasePath() {
        return "/subtasks";
    }
}