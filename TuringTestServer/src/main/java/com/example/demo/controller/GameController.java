package com.example.demo.controller;

import java.io.IOException;
import java.util.Random;

import com.alibaba.fastjson.JSON;
import com.example.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.demo.Response;
import com.example.demo.constant.Constants;
import com.example.demo.dto.GameMessege;
import com.example.demo.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
 
@Component
public class GameController extends TextWebSocketHandler {
 
    @Autowired
    private OnlineUserManager onlineUserManager;
 
    @Autowired
    private RoomManager roomManager;
 
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private ApiSender apiSender;
    // @Resource
    // private UserMapper userMapper;
    private Boolean isFinished = false;
 
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 1. 获取玩家信息
        Long uid = Long.parseLong(session.getAttributes().get("uid").toString());
        User user = userService.getUserByUid(uid);
        // if(user == null){
        //     gameReadyResponse.setStatus(-200);
        //     gameReadyResponse.setReason("用户未登录");
        //     session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(gameReadyResponse)));
        //     return;
        // }
        // 2. 判断当前玩家是否已经进入房间
        Response<String> response = new Response<>();
        Room room = roomManager.getRoomByUserId(user.getUid());
        if (room == null) { 
            response.setUid((long)0);
            response.setData("玩家尚未匹配到!");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            return;
        }
        // 3. 判断当前玩家是否多开(如果一个账号一边在游戏大厅一边在游戏房间，这种也视为多开)
        if (onlineUserManager.getFromGameHall(user.getUid()) != null || onlineUserManager.getFromGameRoom(user.getUid()) != null) {
            response.setUid((long)0);
            response.setData("repeatConnection");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            return;
        }

        // 4. 设置当前玩家上线（进入游戏房间）
        onlineUserManager.enterGameRoom(user.getUid(), session);
 
        // 5. 把两个玩家加入到游戏房间中
        synchronized (room) {
            if (room.getBot()){
                room.setUser2(Constants.ChatBot);
                if (room.getUser1() == null) {
                    room.setUser1(user);
                    System.out.println("玩家" + user.getUsername() + "已经准备就绪");

                    Random random = new Random();
                    int num = random.nextInt(10);
                    if (num % 2 == 0) {
                        room.setWhiteUser(room.getUser1().getUid());
                        noticeGameReady(room,room.getUser1(),room.getUser2());
                    } else{
                        room.setWhiteUser(room.getUser2().getUid());
                        noticeGameReady(room,room.getUser2(),room.getUser1());
                    }
                    return;
                }
            }else{
                if (room.getUser1() == null) {
                    room.setUser1(user);
                    System.out.println("玩家1 " + user.getUsername() + " 已经准备就绪");
                    return;
                }
                if (room.getUser2() == null) {
                    room.setUser2(user);
                    System.out.println("玩家2 " + user.getUsername() + " 已经准备就绪");
    
                    Random random = new Random();
                    int num = random.nextInt(10);
                    if (num % 2 == 0) {
                        room.setWhiteUser(room.getUser1().getUid());
                        noticeGameReady(room,room.getUser1(),room.getUser2());
                    } else{
                        room.setWhiteUser(room.getUser2().getUid());
                        noticeGameReady(room,room.getUser2(),room.getUser1());
                    }
    
                    // 当两个玩家都加入成功后, 让服务器给这两个玩家都返回 websocket 的响应数据.
                    return;
                }
            }
            
        }
        // 6. 如果又有其他玩家连接到已经满了的房间，给出一个提示。（这种情况理论上不存在）
        response.setUid((long)0);
        response.setData("当前房间已满");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
    //返回先手
    private void noticeGameReady(Room room, User thisUser, User thatUser) throws IOException {
        Response<String> resp = new Response<>();
        resp.setData("a");
        if (room.getBot()){
            if (thatUser == Constants.ChatBot){
                resp.setUid((long)-100);
                WebSocketSession webSocketSession = onlineUserManager.getFromGameRoom(thisUser.getUid());
                webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
                System.out.println("通知玩家 " + thisUser.getUsername() + " 先手");
                System.out.println("chatbot 后手");
            }
            else{
                resp.setUid((long)-101);
                WebSocketSession webSocketSession = onlineUserManager.getFromGameRoom(thatUser.getUid());
                webSocketSession = onlineUserManager.getFromGameRoom(thatUser.getUid());
                webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
                System.out.println("通知玩家 " + thatUser.getUsername() + " 后手");
                room.Botfirst(webSocketSession);
            }
        }else{
            resp.setUid((long)-100);
            WebSocketSession webSocketSession = onlineUserManager.getFromGameRoom(thisUser.getUid());
            webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            System.out.println("通知玩家 " + thisUser.getUsername() + " 先手");
            resp.setUid((long)-101);
            webSocketSession = onlineUserManager.getFromGameRoom(thatUser.getUid());
            webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            System.out.println("通知玩家 " + thatUser.getUsername() + " 后手");
        }
    }
    
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
    }

    private void askBot(String payload, User user1, ChatHistory chatHistory) throws IOException {
        GameMessege<String> gamemsg = objectMapper.readValue(payload, new TypeReference<GameMessege<String>>(){});
        System.out.println("用户"+ user1.getUsername()+"说: " + gamemsg.getData());
        //构造ChatRequestDTO
        ChatMessage chatmsg = new ChatMessage("user",gamemsg.getData());
        //调用ChatBot接口
        chatHistory.addToHistory(chatmsg);
        try {
            modelService.chat(chatHistory);
        }catch (Exception e){
            //大模型服务器宕机（还没写处理）
            e.printStackTrace();
        }

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 1. 先从 session 中获取当前用户的身份信息
        Long uid = Long.parseLong(session.getAttributes().get("uid").toString());
        User user = userService.getUserByUid(uid);
        Room room =  roomManager.getRoomByUserId(user.getUid());
        String payload = message.getPayload();
        if (isFinished){
            try {
                GameMessege<Boolean> gamemsg = objectMapper.readValue(payload, new TypeReference<GameMessege<Boolean>>(){});
                Response<String> response = new Response<>();
                response.setData("a");
                if (gamemsg.getData() == room.getBot()) {
                    response.setUid((long)-200);
                    userService.updateScoreByUid(user.getUid(),(long)1);
                }else{
                    response.setUid((long)-201);
                    userService.updateScoreByUid(user.getUid(),(long)-1);
                }
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                return;
            } catch (Exception e) {
            }
        }
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
        try {
            GameMessege<String> gamemsg = objectMapper.readValue(payload, new TypeReference<GameMessege<String>>(){});
            if (gamemsg.getData().startsWith("finish") && gamemsg.getUid()==((long)-300)) {
                isFinished = true;
                return;
            }
        } catch (Exception e) {
        }
        try {
            GameMessege<Boolean> gamemsg = objectMapper.readValue(payload, new TypeReference<GameMessege<Boolean>>(){});
            Response<String> response = new Response<>();
            System.out.println("send message to " + gamemsg.getUid() + "result");
            response.setUid((long)-200);
            response.setData("a");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            return;
        } catch (Exception e) {
        }
        if (user == null){
            System.out.println("[handleTextMessage]当前玩家"+ user.getUsername()+"未登录");
            return;
        }
        // 2. 根据玩家id 获取房间对象

 
        // 通过room对象处理这次请求
        if (room.getBot()){
            // askBot(message.getPayload(),room.getUser1(),room.chatHistory);
            System.out.println("用户"+ (user.getUsername())+"说: ");
            apiSender.add(message.getPayload());
        }
        else{
            room.putChess(message.getPayload());
        }
    }
 
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 玩家异常下线
        // 1. 获取玩家信息
        Long uid = Long.parseLong(session.getAttributes().get("uid").toString());
        User user = userService.getUserByUid(uid);
        WebSocketSession webSocketSession = onlineUserManager.getFromGameRoom(user.getUid());
        if(webSocketSession == session){
            // 2. 退出游戏房间
            onlineUserManager.exitGameRoom(user.getUid());
        }
        System.out.println("当前用户: " + user.getUsername()+" 游戏房间连接异常！");
 
        // 通知对手获胜了
        Room room = roomManager.getRoomByUserId(user.getUid());
        if (room.getBot()){
            userService.updateScoreByUid(user.getUid(),(long)-1);
            roomManager.remove(room.getRoomId(), room.getUser1().getUid(), room.getUser2().getUid());
        }else if (!isFinished){
            noticeThatUserWin(user);
        }
    }
 
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 玩家正常下线
        // 1. 获取玩家信息
        Long uid = Long.parseLong(session.getAttributes().get("uid").toString());
        User user = userService.getUserByUid(uid);
        WebSocketSession webSocketSession = onlineUserManager.getFromGameRoom(user.getUid());
        Room room = roomManager.getRoomByUserId(user.getUid());
        if(webSocketSession == session){
            // 2. 退出游戏房间
            onlineUserManager.exitGameRoom(user.getUid());
        }
        System.out.println("当前用户: " + user.getUsername()+" 离开房间");

        if (room.getBot()){
            userService.updateScoreByUid(user.getUid(),(long)-1);
            roomManager.remove(room.getRoomId(), room.getUser1().getUid(), room.getUser2().getUid());
        }else if(!isFinished){
            noticeThatUserWin(user);
        }
//        String reason = status.getReason();
//        int code = status.getCode();
//        System.out.println("afterConnectionClosed断开连接，因为" + reason + "，状态码：" + code);
    }

    //通知对方获胜
    private void noticeThatUserWin(User user) throws IOException {
        // 1. 根据当前玩家, 找到玩家所在的房间
        Room room = roomManager.getRoomByUserId(user.getUid());
        if (room == null) {
            // 这个情况意味着房间已经被释放了, 也就没有 "对手" 了
            System.out.println("当前房间已关闭, 无需通知对手!");
            return;
        }
 
        // 2. 根据房间找到对手
        User thatUser = (user == room.getUser1()) ? room.getUser2() : room.getUser1();
        // 3. 获取对手的在线状态
        WebSocketSession thatUserSession = onlineUserManager.getFromGameRoom(thatUser.getUid());
        WebSocketSession UserSession = onlineUserManager.getFromGameRoom(user.getUid());
        if (thatUserSession == null) {
            // 对手掉线了!
            System.out.println("对方掉线了, 无需通知!");
            return;
        }
        // 4. 构造一个响应, 来通知对手, 你是获胜方
        Response<String> resp = new Response<>();
        resp.setData("a");
        resp.setUid((long)-200);
        thatUserSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
//        resp.setUid((long)-201);
//        UserSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
 
        // 5. 更新玩家的分数信息
        userService.updateScoreByUid(user.getUid(),(long)-1);
        userService.updateScoreByUid(thatUser.getUid(),(long)1);
 
        // 6. 释放房间对象
        roomManager.remove(room.getRoomId(), room.getUser1().getUid(), room.getUser2().getUid());
    }
}