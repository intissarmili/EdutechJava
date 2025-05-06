package models;

import java.time.LocalDateTime;

public class Event {
    private int id;
    private String title;
    private String description;  // Nouvel attribut
    private LocalDateTime dateTime;
    private int categoryevent_id;
    private String photo;  // Nouvel attribut

    public Event() {}

    public Event(String title, String description, int categoryevent_id, LocalDateTime dateTime, String photo) {
        this.title = title;
        this.description = description;
        this.categoryevent_id = categoryevent_id;
        this.dateTime = dateTime;
        this.photo = photo;
    }

    public Event(int id, String title, String description, int categoryevent_id, LocalDateTime dateTime, String photo) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryevent_id = categoryevent_id;
        this.dateTime = dateTime;
        this.photo = photo;
    }

    // Constructeurs existants pour maintenir la compatibilité avec le code actuel
    public Event(String title, int categoryevent_id, LocalDateTime dateTime) {
        this.title = title;
        this.categoryevent_id = categoryevent_id;
        this.dateTime = dateTime;
        this.description = null;
        this.photo = null;
    }

    public Event(int id, String title, int categoryevent_id, LocalDateTime dateTime) {
        this.id = id;
        this.title = title;
        this.categoryevent_id = categoryevent_id;
        this.dateTime = dateTime;
        this.description = null;
        this.photo = null;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategoryevent_id() { return categoryevent_id; }
    public void setCategoryevent_id(int categoryevent_id) { this.categoryevent_id = categoryevent_id; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
}