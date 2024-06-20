package com.example.demo.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson.JSONObject;
import com.example.demo.Response;
import com.example.demo.constant.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class Batcher {

    private final List<List<ChatMessage>> batch = new ArrayList<>();
    private final List<Long> assign = new ArrayList<>();

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;
    
    public Batcher() {
        // 创建三个线程, 操作三个匹配队列
        Thread t = new Thread() {
            @Override
            public void run() {
                // 扫描 amateurQueue
                while (true) {
                    try {
                        Thread.sleep(500);
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handlerBatch(batch);
                }
            }
        };
        t.start();
    }
    public void add(ChatHistory chatHistory) {
        synchronized (batch) {
            batch.add(chatHistory.getHistory());
            batch.notify();
        }
        synchronized (assign) {
            assign.add(chatHistory.getUid());
            assign.notify();
        }
    }

    private void handlerBatch(List<List<ChatMessage>> batch) {
        if (batch.isEmpty()) {
            return;
        }
        try {
           String str = java2Python(batch);
           List<ChatMessage> msgs = JSONObject.parseArray(str.toString(), ChatMessage.class);
           for (ChatMessage msg: msgs) {
                Long uid = assign.get(0);
                Room room = roomManager.getRoomByUserId(uid);
                room.chatHistory.addToHistory(msg);
                Response<String> response = new Response<>();
                response.setData(msg.getContent());
                response.setUid((long)1);
                WebSocketSession session = onlineUserManager.getFromGameRoom(uid);
                if (session == null) {
                    // 玩家已经下线了. 直接认为bot 获胜!
                    userService.updateScoreByUid(uid,(long)-1);
                    System.out.println("玩家1 掉线!");
                    System.out.println("游戏结束! 房间即将销毁! roomId=" + room.getRoomId() + " 获胜方为: " + "Bot");
                    // 销毁房间
                    roomManager.remove(room.getRoomId(), uid, Constants.ChatBot.getUid());
                    return;
                }
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
           }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String java2Python(List<List<ChatMessage>> history) throws Exception {
      String str= JSON.toJSONString(history);
      str = str.replace("\"", "\\\"");
      String pythonPath = "../main.py";
      String[] arguments = new String[] {"python",pythonPath,str};//指定命令、路径、传递的参数
      StringBuilder sbrs = null;
      StringBuilder sberror = null;
      try {
        ProcessBuilder builder = new ProcessBuilder(arguments);
        Process process = builder.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));//获取字符输入流对象
        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream(), "utf-8"));//获取错误信息的字符输入流对象
        String line = null;
        sbrs = new StringBuilder();
        sberror = new StringBuilder();
        //记录输出结果
        while ((line = in.readLine()) != null) {
          sbrs.append(line);
        }
        //记录错误信息
        while ((line = error.readLine()) != null) {
          sberror.append(line);
        }
        in.close();
        process.waitFor();
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.out.println(sbrs);
      System.out.println(sberror);
      return sbrs.toString();
    }
}
