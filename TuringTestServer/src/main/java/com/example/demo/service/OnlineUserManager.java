package com.example.demo.service;
 
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
 
import java.util.concurrent.ConcurrentHashMap;
 
@Component
public class OnlineUserManager {
    // 这个哈希表用来表示当前用户在游戏大厅的在线状态
    private ConcurrentHashMap<Long, WebSocketSession> gameHall = new ConcurrentHashMap<>();
 
    // 玩家进入游戏大厅
    public void enterGameHall(Long uid,WebSocketSession webSocketSession){
        gameHall.put(uid,webSocketSession);
    }
 
    // 玩家退出游戏大厅
    public void exitGameHall(Long uid){
        gameHall.remove(uid);
    }

    public WebSocketSession getFromGameHall(Long uid){
        return gameHall.get(uid);
    }

    private ConcurrentHashMap<Long, WebSocketSession> gameRoom = new ConcurrentHashMap<>();
 
    // 玩家进入游戏房间
    public void enterGameRoom(long uid,WebSocketSession webSocketSession){
        gameRoom.put(uid,webSocketSession);
    }
 
    // 玩家退出游戏房间
    public void exitGameRoom(long uid){
        gameRoom.remove(uid);
    }
 
    public WebSocketSession getFromGameRoom(long uid){
        return gameRoom.get(uid);
    }
}