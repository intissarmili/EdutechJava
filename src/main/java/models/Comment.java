package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Comment {
    private int id;
    private String content;
    private String username;
    private LocalDateTime date;
    
    public Comment() {
    }
    
    public Comment(String content, String username) {
        this.content = content;
        this.username = username;
        this.date = LocalDateTime.now();
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public String getFormattedDate() {
        if (date == null) {
            return "N/A";
        }
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
} 