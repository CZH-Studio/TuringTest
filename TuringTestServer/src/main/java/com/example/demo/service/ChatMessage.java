package com.example.demo.service;

public class ChatMessage extends ChatText {

    public long timestamp;
    public String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public ChatMessage() {
    }

    public ChatMessage(String role, String content) {
        // this.timestamp = System.currentTimeMillis();
        this.role = role;
        this.content = content;
    }
}

class ChatText {
    public String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
}

