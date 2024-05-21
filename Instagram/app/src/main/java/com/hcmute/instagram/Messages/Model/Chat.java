package com.hcmute.instagram.Messages.Model;

import com.google.firebase.firestore.FieldValue;

public class Chat {

    private String id;
    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    private Object timestamp; // Use Object type for compatibility with FieldValue.serverTimestamp()

    public Chat() {
        // Default constructor required for Firestore
    }

    public Chat(String id, String sender, String receiver, String message, boolean isseen, Object timestamp) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
