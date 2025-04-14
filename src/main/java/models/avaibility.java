package models;

public class avaibility {
    private int id;
    private String date;
    private String startTime;
    private String endTime;
    private int tutorId;

    public avaibility() {}

    public avaibility(int id, String date, String startTime, String endTime, int tutorId) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tutorId = tutorId;
    }

    public avaibility(String date, String startTime, String endTime, int tutorId) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tutorId = tutorId;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public int getTutorId() { return tutorId; }
    public void setTutorId(int tutorId) { this.tutorId = tutorId; }
}
