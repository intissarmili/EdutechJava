package model;

public class CategoryEvent {
    private int id;
    private String location;
    private String type;
    private String duration;

    public CategoryEvent() {
    }

    public CategoryEvent(int id, String location, String type, String duration) {
        this.id = id;
        this.location = location;
        this.type = type;
        this.duration = duration;
    }

    public CategoryEvent(String location, String type, String duration) {
        this.location = location;
        this.type = type;
        this.duration = duration;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
