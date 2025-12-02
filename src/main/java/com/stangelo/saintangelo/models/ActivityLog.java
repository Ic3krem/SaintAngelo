package com.stangelo.saintangelo.models;

import java.time.LocalDateTime;

/**
 * ActivityLog model class
 */
public class ActivityLog {
    private int logId;
    private User user;
    private String action;
    private String details;
    private ActivityType activityType;
    private String ipAddress;
    private LocalDateTime timestamp;

    public ActivityLog(int logId, User user, String action, String details, ActivityType activityType,
                       String ipAddress, LocalDateTime timestamp) {
        this.logId = logId;
        this.user = user;
        this.action = action;
        this.details = details;
        this.activityType = activityType;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

