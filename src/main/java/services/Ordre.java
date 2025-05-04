package services;

public class Ordre {
    private int id;
    private int userId;
    private int certificationId;

    public Ordre() {
    }

    public Ordre(int userId, int certificationId) {
        this.userId = userId;
        this.certificationId = certificationId;
    }

    public Ordre(int id, int userId, int certificationId) {
        this.id = id;
        this.userId = userId;
        this.certificationId = certificationId;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCertificationId() {
        return certificationId;
    }

    public void setCertificationId(int certificationId) {
        this.certificationId = certificationId;
    }

}
