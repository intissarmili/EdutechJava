package models;

public class Feed {
    private int id;
    private String publication;

    public Feed() {
    }

    public Feed(int id, String publication) {
        this.id = id;
        this.publication = publication;
    }

    public Feed(String publication) {
        this.publication = publication;
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

    @Override
    public String toString() {
        return publication;
    }
}

