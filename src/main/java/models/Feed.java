package models;

import java.time.LocalDateTime;

public class Feed {
    private int id;
    private String publication;
    private LocalDateTime lastModified;

    public Feed() {
    }

    public Feed(int id, String publication) {
        this.id = id;
        this.publication = publication;
        this.lastModified = LocalDateTime.now();
    }

    public Feed(int id, String publication, LocalDateTime lastModified) {
        this.id = id;
        this.publication = publication;
        this.lastModified = lastModified;
    }

    public Feed(String publication) {
        this.publication = publication;
        this.lastModified = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPublication() {
        return publication;
    }

    public void setPublication(String publication) {
        this.publication = publication;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return publication;
    }
}

