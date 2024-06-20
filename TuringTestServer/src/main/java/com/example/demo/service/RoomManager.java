package com.example.demo.service;

import com.alibaba.fastjson.JSON;
import com.example.demo.constant.Constants;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
 
// 房间管理器
@Component
public class RoomManager {
    //使用哈希表存储每个游戏房间并对其进行管理

    private ConcurrentHashMap<String,Room> rooms = new ConcurrentHashMap<>();
    // 通过用户id 与房间id 维护玩家和房间之间的关系
    private ConcurrentHashMap<Long,String>  usrIdToRoomId = new ConcurrentHashMap<>();
    
    public void add(Room room, long userId1,long userId2){
        // 添加一个房间到房间管理器的同时，也将两个玩家的 userId 添加到 usrIdToRoomId 中，便于维护玩家和房间之间的关系
        rooms.put(room.getRoomId(),room);
        usrIdToRoomId.put(userId1,room.getRoomId());
        usrIdToRoomId.put(userId2,room.getRoomId());
    }
 
    public void remove(String roomId, long userId1,long userId2) throws IOException {
        // 移除一个房间的同时也要同时移除两个玩家的信息
        rooms.remove(roomId);
        usrIdToRoomId.remove(userId1);
        usrIdToRoomId.remove(userId2);
        ChatMessageApi chatmsgapi = new ChatMessageApi();
        chatmsgapi.setMessage("");
        chatmsgapi.setRoom_id(roomId);
        String str = JSON.toJSONString(chatmsgapi);
        sendPostRequest(Constants.SERVER_URL,str);
    }
 
    public Room getByRoomId(String roomId){
        return rooms.get(roomId);
    }
 
    // 通过用户id 查找对应的房间
    public Room getRoomByUserId(long userId){
        String rooId = usrIdToRoomId.get(userId);
        if(rooId == null){
            // rooId == null 表示游戏房间不存在
            // userId -> roomId 映射关系不存在，直接返回 null
            return null;
        }
        return rooms.get(rooId);
    }

    private static void sendPostRequest(String url, String json) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            StringEntity entity = new StringEntity(json,"UTF-8");
            post.setEntity(entity);
            post.setHeader("Content-type", "application/json");
            try (CloseableHttpResponse response = httpClient.execute(post)) {

            }
        }
    }
}
