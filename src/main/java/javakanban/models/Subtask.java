package javakanban.models;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (this.epicId == id) {
            this.epicId = -1;
        }
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public Subtask copy() {
        Subtask copy = new Subtask(this.getName(), this.getDescription(), this.epicId);
        copy.setId(this.getId());
        copy.setStatus(this.getStatus());
        return copy;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", description='" + getDescription() + '\'' +
                ", epicId=" + epicId +
                '}';
    }
}