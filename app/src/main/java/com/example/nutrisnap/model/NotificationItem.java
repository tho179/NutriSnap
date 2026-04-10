package com.example.nutrisnap.model;

public class NotificationItem {
    private String id;
    private String title;
    private String message;
    private long timestamp;
    private boolean isRead;
    private String type; // "SYSTEM" or "REMINDER"

    public NotificationItem() {
    }

    public NotificationItem(String id, String title, String message, long timestamp) {
        this(id, title, message, timestamp, "REMINDER");
    }

    public NotificationItem(String id, String title, String message, long timestamp, String type) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = false;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}