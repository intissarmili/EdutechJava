package models;

import java.util.Date;

public class reservation {
    private int id;
    private String topic;
    private Date start_time;
    private String  status;
    private int duration;
    private int avaibility_id;

    public reservation(int id, String topic, Date start_time, String status, int duration, int avaibility_id) {
        this.id = id;
        this.topic = topic;
        this.start_time = start_time;
        this.status = status;
        this.duration = duration;
        this.avaibility_id = avaibility_id;
         }

    public reservation(String topic, Date start_time, String status, int duration, int avaibility_id) {
        this.topic = topic;
        this.start_time = start_time;
        this.status = status;
        this.duration = duration;
        this.avaibility_id = avaibility_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getAvaibility_id() {
        return avaibility_id;
    }

    public void setAvaibility_id(int avaibility_id) {
        this.avaibility_id = avaibility_id;
    }
}
