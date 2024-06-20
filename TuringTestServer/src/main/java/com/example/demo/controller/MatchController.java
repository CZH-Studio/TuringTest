package com.example.demo.controller;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.demo.Response;
import com.example.demo.dto.GameMessege;
import com.example.demo.entity.User;
import com.example.demo.service.Matcher;
import com.example.demo.service.OnlineUserManager;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
 
// 通过这个类来处理匹配功能中的 websocket 请求
@Component
public class MatchController extends TextWebSocketHandler {

    private ObjectMapper objectMapper = new ObjectMapper();
 
    @Autowired
    private OnlineUserManager onlineUserManager;
 
    @Autowired
    private Matcher matcher;

    @Autowired
    private UserService userService;

    // @OnOpen
    // public void handleOpen(@PathParam("userid") String userid, WebSocketSession session) {
    //     System.out.println("Received userid parameter: " + userid);
    //     // 在这里根据接收到的userid参数执行相应的逻辑
    // }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 玩家上线，加入到 OnlineUserManager 中
        // 1. 获取当前的用户信息（谁在游戏大厅创建连接）
        Long uid = Long.parseLong(session.getAttributes().get("uid").toString());
        System.out.println(uid);

        // 2. 判断当前用户是否已经登录
        Response<Void> response = new Response<>();
        if (onlineUserManager.getFromGameHall(uid) != null ) {
            // 当前用户已经登录
            response.setUid((long)0);
            /**
             *  先通过 ObjectMapper 把 MathResponse 对象转成 JSON 字符串
             *  然后再包装上一层 TextMessage,再进行传输
             *  TextMessage 就表示一个文本格式的 websocket 数据包
              */
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));//-------?
            session.close();
            return;
        }
 
        // 3. 设置在线状态
        onlineUserManager.enterGameHall(uid,session);
        System.out.println("玩家"+userService.getUserByUid(uid).getUsername()+"进入游戏大厅");
    }
 
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理开始匹配 和 停止匹配
        Long uid = Long.parseLong(session.getAttributes().get("uid").toString());
        User user = userService.getUserByUid(uid);
        // 获取到客户端给服务器发送的数据
        String payload = message.getPayload();
        try {
            GameMessege<String> gamemsg = objectMapper.readValue(payload, new TypeReference<GameMessege<String>>(){});
            if (gamemsg.getData().startsWith("heartbeat")) {
                Response<String> response = new Response<>();
                response.setUid(uid);
                response.setData(gamemsg.getData());
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                return;
            }
        } catch (Exception e) {
        }

        // 当前这个数据载荷是一个 JSON 格式的字符串，需要将它转换成 Java 对象（ MatchRequest )
        GameMessege<String> gamemsg = objectMapper.readValue(payload, new TypeReference<GameMessege<String>>(){}); // 从客户端获取的数据
        // Response<String> response = new Response<>(); // 给客户端返回的数据
        if (gamemsg.getData().equals("match")) {
            // 进入匹配队列, 加入用户
            matcher.add(user);
            // 返回响应给前端
            // response.setUid(uid);
            // response.setData("starmatch");
        // }else if(gamemsg.getData().equals("stopmatch")) {
        //     // 退出匹配队列, 将用户移除
        //     matcher.remove(user);
        //     response.setData("stopMatch");
        //     response.setUid();
        }else{
            session.close();
            return;
        }
        // session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
 
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 玩家下线，从 OnlineUserManager 中删除
        // 1. 获取用户信息
        Long uid = Long.parseLong(session.getAttributes().get("uid").toString());
        User user = userService.getUserByUid(uid);
        WebSocketSession webSocketSession = onlineUserManager.getFromGameHall(user.getUid());
        if(webSocketSession == session) {
            // 2. 设置在线状态
            onlineUserManager.exitGameHall(user.getUid());
        }
        // 如果玩家正在匹配中，而 websocket 连接断开了就应该移除匹配队列
        matcher.remove(user);
        System.out.println("handleTransportError断开连接");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 玩家下线，从 OnlineUserManager 中删除
        // 1. 获取用户信息
        Long uid = Long.parseLong(session.getAttributes().get("uid").toString());
        User user = userService.getUserByUid(uid);
        WebSocketSession webSocketSession = onlineUserManager.getFromGameHall(user.getUid());
        if(webSocketSession == session) {
            // 2. 设置在线状态
            onlineUserManager.exitGameHall(user.getUid());
        }
        // 如果玩家正在匹配中，而 websocket 连接断开了就应该移除匹配队列
        matcher.remove(user);
        String reason = status.getReason();
        int code = status.getCode();
        System.out.println("afterConnectionClosed断开连接，因为" + reason + "，状态码：" + code);
    }
}