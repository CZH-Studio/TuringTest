package com.example.demo.service;
 
import java.io.IOException;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.ApplicationContextProvider;
import com.example.demo.Response;
import com.example.demo.constant.Constants;
import com.example.demo.dto.GameMessege;
import com.example.demo.entity.User;//---?
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
 
// 这个类表示一个游戏房间
public class Room {
    private String roomId;
    private User user1;
    private User user2;
    private long whiteUser; // 先手方的玩家id
    private boolean Bot = false; // 后手方的玩家id

    public ChatHistory chatHistory = new ChatHistory();//---------?

    private OnlineUserManager onlineUserManager;
 
    // 引入 roomManager 用于房间销毁
    private RoomManager roomManager;
 
    // 用于将 JSON 格式的字符串转换 Java 对象
    private ObjectMapper objectMapper = new ObjectMapper();

    private UserServiceImpl userService;

    private ModelServiceImpl modelService;
 
    // // 引入 UserMapper，用于更新比赛数据
    // private UserMapper userMapper;

 
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
        chatHistory.setUid(user1.getUid());
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }
    
    public long getWhiteUser() {
        return whiteUser;
    }

    public void setWhiteUser(long whiteUser) {
        this.whiteUser = whiteUser;
    }

    public boolean getBot() {
        return Bot;
    }

    public void setBot(boolean bot) {
        Bot = bot;
    }

    public Room() {
        // 构造 Room 的时候生成一个唯一的字符串表示房间 id.
        // 使用 UUID 来作为房间 id
        roomId = UUID.randomUUID().toString();
        ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        // 通过入口类中记录的 context 来手动获取到前面的 RoomManager 和 OnlineUserManager
        // ApplicationContext context = new AnnotationConfigApplicationContext(DemoApplication.class);
        onlineUserManager = context.getBean(OnlineUserManager.class);
        roomManager = context.getBean(RoomManager.class);
        // userMapper = context.getBean(UserMapper.class);
    }
 
    // 处理一次落子的操作
    public void putChess(String payload) throws IOException {
        // 1. 记录当前落子的位置
        // 将 json 格式的字符串转换成 Java 对象
        GameMessege<String> gamemsg = objectMapper.readValue(payload, new TypeReference<GameMessege<String>>(){});
        Response<String> response = new Response<>();
        // 当前这个子是玩家1 落的还是玩家2 落的. 根据这个玩家1 和 玩家2 来决定往数组中是写 1 还是 2
        boolean isUser1 = gamemsg.getUid() == user1.getUid();
 
        // 4. 给房间里的所有客户端返回响应
        response.setData(gamemsg.getData());
        response.setUid((long)1);
        System.out.println("用户"+ (isUser1?user1.getUsername():user2.getUsername())+"说: " + gamemsg.getData());
 
        // 要想给用户发送 websocket 数据, 就需要获取到这个用户的 WebSocketSession
        WebSocketSession session1 = onlineUserManager.getFromGameRoom(user1.getUid());
        WebSocketSession session2 = onlineUserManager.getFromGameRoom(user2.getUid());
 
        // 万一当前查到的会话为空(玩家已经下线了) 特殊处理一下
        if (session1 == null) {
            // 玩家1 已经下线了. 直接认为玩家2 获胜!
            response.setUid((long)-200);
            response.setData("a");
            session2.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            userService.updateScoreByUid(user2.getUid(),(long)1);
            userService.updateScoreByUid(user1.getUid(),(long)-1);
            System.out.println("玩家1 掉线!");
            System.out.println("游戏结束! 房间即将销毁! roomId=" + roomId + " 获胜方为: " + user2.getUsername());
            // 销毁房间
            roomManager.remove(roomId, user1.getUid(), user2.getUid());
            return;
        }
        if (session2 == null) {
            // 玩家2 已经下线. 直接认为玩家1 获胜!
            response.setUid((long)-200);
            response.setData("a");
            userService.updateScoreByUid(user1.getUid(),(long)1);
            userService.updateScoreByUid(user2.getUid(),(long)-1);
            System.out.println("玩家2 掉线!");
            System.out.println("游戏结束! 房间即将销毁! roomId=" + roomId + " 获胜方为: " + user1.getUsername());
            // 销毁房间
            roomManager.remove(roomId, user1.getUid(), user2.getUid());
            return;
        }
        // 把响应构造成 JSON 字符串, 通过 session 进行传输.
        if (isUser1) {
            session2.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }
        else{
            session1.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }

    }


    public void Botfirst(WebSocketSession session){
        //数据库中获取回答
        String extractData = "你好！";
        chatHistory.addToHistory(new ChatMessage("assistant",extractData));
        Response<String> response = new Response<>();
        response.setData(extractData);
        response.setUid((long)1);
        try{
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}