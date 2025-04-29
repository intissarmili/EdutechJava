package models;

import java.time.LocalDateTime;

public class FeedHistory {
    private int id;
    private int feedId;
    private String oldContent;
    private String newContent;
    private String actionType; // CREATE, UPDATE, DELETE
    private LocalDateTime timestamp;
    private String userInfo; // Optional: could store user who made the change

    public FeedHistory() {
    }

    public FeedHistory(int feedId, String oldContent, String newContent, String actionType) {
        this.feedId = feedId;
        this.oldContent = oldContent;
        this.newContent = newContent;
        this.actionType = actionType;
        this.timestamp = LocalDateTime.now();
    }

    public FeedHistory(int id, int feedId, String oldContent, String newContent, String actionType, LocalDateTime timestamp) {
        this.id = id;
        this.feedId = feedId;
        this.oldContent = oldContent;
        this.newContent = newContent;
        this.actionType = actionType;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }

    public String getOldContent() {
        return oldContent;
    }

    public void setOldContent(String oldContent) {
        this.oldContent = oldContent;
    }

    public String getNewContent() {
        return newContent;
    }

    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public String toString() {
        return "FeedHistory{" +
                "id=" + id +
                ", feedId=" + feedId +
                ", actionType='" + actionType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 