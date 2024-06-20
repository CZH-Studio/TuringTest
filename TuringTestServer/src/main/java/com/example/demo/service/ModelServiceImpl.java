package com.example.demo.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

@Component
public class ModelServiceImpl implements ModelService{

    @Autowired
    private Batcher batcher;

    @Override
    public void chat(ChatHistory chatHistory) throws Exception {
        batcher.add(chatHistory);
    }
}
