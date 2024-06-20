package com.example.demo.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ModelService {
    void chat(ChatHistory history) throws Exception;

//    ChatMessage extract() throws Exception;
}
