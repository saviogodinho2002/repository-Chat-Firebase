package com.savio.chatfirebase;

public class Notification extends Message{
    private String fromName;

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public Notification() {
    }
}
