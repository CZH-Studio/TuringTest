package com.example.demo.constant;

import com.example.demo.entity.User;

//final修饰符
public final class Constants {
    //私有构造方法
    private static final User CreateBot() {
        User Bot = new User();
        Bot.setUid((long)-114514);
        return Bot;
    }
    public static final User ChatBot = CreateBot();

    public static final String SERVER_URL = "http://localhost:5000/chat";
}