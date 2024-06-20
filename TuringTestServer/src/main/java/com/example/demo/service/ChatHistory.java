package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// import org.springframework.stereotype.Component;

// @Component
public class ChatHistory {

    private long uid;
    final int maxMessages = 100;
    final List<ChatMessage> chatHistory = new ArrayList<>(100);
    final Lock readLock;
    final Lock writeLock;

    public ChatHistory() {
        var lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }

    /**
     * Get a copy of history message.
     */
    public List<ChatMessage> getHistory() {
        this.readLock.lock();
        try {
            return List.copyOf(chatHistory);
        } finally {
            this.readLock.unlock();
        }
    }

    public void addToHistory(ChatMessage message) {
        this.writeLock.lock();
        try {
            this.chatHistory.add(message);
            if (this.chatHistory.size() > maxMessages) {
                this.chatHistory.remove(0);
            }
        } finally {
            this.writeLock.unlock();
        }
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
    
}