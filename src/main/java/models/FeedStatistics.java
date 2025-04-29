package models;

public class FeedStatistics {
    private int feedId;
    private String publication;
    private int commentCount;
    private int totalLikes;
    private int totalDislikes;
    
    public FeedStatistics() {
    }
    
    public FeedStatistics(int feedId, String publication, int commentCount, int totalLikes, int totalDislikes) {
        this.feedId = feedId;
        this.publication = publication;
        this.commentCount = commentCount;
        this.totalLikes = totalLikes;
        this.totalDislikes = totalDislikes;
    }
    
    public int getFeedId() {
        return feedId;
    }
    
    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }
    
    public String getPublication() {
        return publication;
    }
    
    public void setPublication(String publication) {
        this.publication = publication;
    }
    
    public int getCommentCount() {
        return commentCount;
    }
    
    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
    
    public int getTotalLikes() {
        return totalLikes;
    }
    
    public void setTotalLikes(int totalLikes) {
        this.totalLikes = totalLikes;
    }
    
    public int getTotalDislikes() {
        return totalDislikes;
    }
    
    public void setTotalDislikes(int totalDislikes) {
        this.totalDislikes = totalDislikes;
    }
    
    @Override
    public String toString() {
        return "Publication #" + feedId + ": " 
                + commentCount + " commentaires, " 
                + totalLikes + " likes, " 
                + totalDislikes + " dislikes";
    }
} 