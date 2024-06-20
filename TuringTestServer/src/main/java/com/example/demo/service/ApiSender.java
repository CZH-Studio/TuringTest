package com.example.demo.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson.JSON;
import com.example.demo.Response;
import com.example.demo.constant.Constants;
import com.example.demo.dto.GameMessege;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ApiSender {

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<String> payloadList = new ArrayList<>();
    public ApiSender() {
        // 创建三个线程, 操作三个匹配队列
        Thread t = new Thread() {
            @Override
            public void run() {
                // 扫描 amateurQueue
                while (true) {
                    try {
                        askApi(payloadList);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    public void add(String payload) {
        synchronized (payloadList) {
            payloadList.add(payload);
            payloadList.notify();
        }
    }

    private void askApi(List<String> payloadList) throws IOException {
        if (payloadList.size() == 0) {
            return;
        }
        String payload = payloadList.get(0);
        payloadList.remove(0);
        GameMessege<String> gamemsg = objectMapper.readValue(payload, new TypeReference<GameMessege<String>>(){});
        Long uid = gamemsg.getUid();
        System.out.println(gamemsg.getData());
        String roomId = roomManager.getRoomByUserId(uid).getRoomId();
        String url = Constants.SERVER_URL;
        ChatMessageApi chatmsgapi = new ChatMessageApi();
        chatmsgapi.setMessage(gamemsg.getData());
        chatmsgapi.setRoom_id(roomId);
        String str = JSON.toJSONString(chatmsgapi);
        System.out.println(str);
        try {
            String res = sendPostRequest(url, str);
            JSONObject jsonObject = JSONObject.parseObject(res);
            String ans = jsonObject.getString("message");
            Response<String> response = new Response<>();
            response.setData(ans);
            response.setUid((long)1);
            WebSocketSession session = onlineUserManager.getFromGameRoom(uid);
            if (session == null) {
                // 玩家已经下线了. 直接认为bot 获胜!
                userService.updateScoreByUid(uid,(long)-1);
                System.out.println("玩家1 掉线!");
                System.out.println("游戏结束! 房间即将销毁! roomId=" + roomId + " 获胜方为: " + "Bot");
                // 销毁房间
                roomManager.remove(roomId, uid, Constants.ChatBot.getUid());
                return;
            }
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static String sendPostRequest(String url, String json) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            StringEntity entity = new StringEntity(json,"UTF-8");
            post.setEntity(entity);
            post.setHeader("Content-type", "application/json");
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }
}
