package com.example.demo.service;
 
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.example.demo.Response;
import com.example.demo.constant.Constants;
import com.example.demo.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
 
// 匹配器, 这个类是用来完成匹配功能的
@Component
public class Matcher {
    // 创建匹配队列 按等级划分
    // 1. 业余水平：score < 2000
    private Queue<User> amateurQueue = new LinkedList<>();
    // 2. 普通水平：score >= 2000 && score < 3000
    private Queue<User> normalQueue = new LinkedList<>();
    // 3. 大师水平：score >= 3000
    private Queue<User> masterQueue = new LinkedList<>();
 
    @Autowired
    private OnlineUserManager onlineUserManager;
 
    @Autowired
    private ObjectMapper objectMapper;
 
    @Autowired
    private RoomManager roomManager;
 
    /**
     * 将当前玩家添加到匹配队列中
     * @param user
     */
    public void add(User user) {
        // 按等级加入队列中
        if (user.getScore() < 2000) {
            synchronized (amateurQueue) {
                amateurQueue.offer(user);
                // 只要有用户进入了, 就进行唤醒
                amateurQueue.notify();
            }
            // 打印日志
            System.out.println("把玩家"+user.getUsername()+"加入到了amateurQueue 中");
        }else if (user.getScore() >= 2000 && user.getScore() < 3000) {
            synchronized (normalQueue) {
                normalQueue.offer(user);
                normalQueue.notify();
            }
            System.out.println("把玩家"+user.getUsername()+"加入到了normalQueue 中");
        }else {
            synchronized (masterQueue) {
                masterQueue.offer(user);
                masterQueue.notify();
            }
            System.out.println("把玩家"+user.getUsername()+"加入到了masterQueue 中");
        }
    }

    /**
     *  当玩家点击停止匹配
     * 就把当前玩家匹配队列中删除
     * @param user
     */
    public void remove(User user) {
        // 按照当前等级去对应匹配队列中删除
        if (user.getScore() < 2000) {
            synchronized (amateurQueue){
                amateurQueue.remove(user);
            }
            System.out.println("把玩家"+user.getUsername()+"移除了masterQueue");
        }else if (user.getScore() >= 2000 && user.getScore() < 3000) {
            synchronized (normalQueue) {
                normalQueue.remove(user);
            }
            System.out.println("把玩家"+user.getUsername()+"移除了normalQueue");
        }else {
            synchronized (masterQueue) {
                masterQueue.remove(user);
            }
            System.out.println("把玩家"+user.getUsername()+"移除了masterQueue");
        }
    }
 
    // 使用3个线程去一直的进行查看是否有2个以上的人, 如果有进行匹配
    public Matcher() {
        // 创建三个线程, 操作三个匹配队列
        Thread t1 = new Thread() {
            @Override
            public void run() {
                // 扫描 amateurQueue
                while (true) {
                    handlerMatch(amateurQueue);
                }
            }
        };
        t1.start();
        Thread t2 = new Thread() {
            @Override
            public void run() {
                // 扫描 normalQueue
                while (true) {
                    handlerMatch(normalQueue);
                }
            }
        };
        t2.start();
        Thread t3 = new Thread() {
            @Override
            public void run() {
                // 扫描 masterQueue
                while (true) {
                    handlerMatch(masterQueue);
                }
            }
        };
        t3.start();
    }

    private boolean isBot() {
//        return true;
         Random r = new Random();
         double d1 = r.nextDouble();
         return d1 < 0.5;
    }
 
    private void handlerMatch(Queue<User> matchQueue) {
        // 因为三个队列都调用了 handlerMatch 方法，因此对这个方法里面的操作进行加锁即可。
        // 针对形参进行加锁（传入不同的实参就可以对不同的队列对象进行加锁）
        synchronized (matchQueue) {
            try{
                // 1. 先查看当前队列中的元素个数, 是否满足两个
                //    在往队列里添加一个元素后仍然不能进行后续匹配操作，
                //    因此使用 while 循环检测是否有两个元素添加到队列中更合理
                while (matchQueue.size() < 2) {
                    // 玩家数 < 2 的时候, 就进行等待
                    System.out.println("当前匹配队列中只有"+matchQueue.size()+"人, 等待中...");
                    if (isBot() && matchQueue.size() == 1){
                        System.out.println("匹配到Bot");
                        User player = matchQueue.poll();
                        handlerBot(player,matchQueue);
                        return;
                    }
                    else{
                        matchQueue.wait();
                    }
                }
                // 2. 尝试从队列中取出两个玩家
                User player1 = matchQueue.poll();
                if (isBot()){
                    System.out.println("匹配到Bot");
                    handlerBot(player1,matchQueue);
                    return;
                }
                User player2 = matchQueue.poll();
                System.out.println("匹配到的两个玩家: " + player1.getUsername()+ " , " + player2.getUsername());
 
                // 3. 获取到玩家的 websocket 的会话.
                WebSocketSession session1 = onlineUserManager.getFromGameHall(player1.getUid());
                WebSocketSession session2 = onlineUserManager.getFromGameHall(player2.getUid());
                // 再次判断是否为空
                if (session1 == null && session2 != null) {
                    // 如果玩家1 掉线了，就把玩家2 重新放到匹配队列中
                    matchQueue.offer(player2);
                    return;
                }
                if (session1 != null && session2 == null) {
                    // 如果玩家2 掉线了，就把玩家1 重新放到匹配队列中
                    matchQueue.offer(player1);
                    return;
                }
                if (session1 == null && session2 == null) {
                    return;
                }
                if (session1 == session2) {
                    // 如果两个玩家是同一个用户（一个玩家入队了两次,理论上不存在，但还是需要再判定一次）
                    // 就把其中的一个玩家放回到匹配队列
                    matchQueue.offer(player1);
                    return;
                }
 
                // 4. 把两个玩家放入一个游戏房间中
                Room room = new Room();
                roomManager.add(room, player1.getUid(), player2.getUid());
 
                // 5. 给玩家反馈信息, 通知匹配到了对手
               // 给玩家1返回的响应
                Response<String> response1 = new Response<>();
                response1.setData("matchsuccess");
                String json1 = objectMapper.writeValueAsString(response1);
                session1.sendMessage(new TextMessage(json1));
 
                // 给玩家2返回的响应
                Response<String> response2 = new Response<>();
                response2.setData("matchsuccess");
                String json2 = objectMapper.writeValueAsString(response2);
                session2.sendMessage(new TextMessage(json2));
 
            } catch (IOException | InterruptedException e) {// - IOException |
                e.printStackTrace();
            }
        }
    }
    private void handlerBot(User player,Queue<User> matchQueue) {
        try{
            System.out.println("匹配到的两个玩家: " + player.getUsername()+ " , " + "ChatBot");
            WebSocketSession session = onlineUserManager.getFromGameHall(player.getUid());
            if (session == null) {
                // 如果玩家掉线了，就把玩家重新放到匹配队列中
                matchQueue.offer(player);
                return;
            }
            // 4. 把两个玩家放入一个游戏房间中
            Room room = new Room();
            room.setBot(true);
            roomManager.add(room, player.getUid(), Constants.ChatBot.getUid());

            // 5. 给玩家反馈信息, 通知匹配到了对手
           // 给玩家1返回的响应
            Response<String> response1 = new Response<>();
            response1.setData("matchsuccess");
            String json1 = objectMapper.writeValueAsString(response1);
            session.sendMessage(new TextMessage(json1));
        } catch (IOException e) {// - IOException |
            e.printStackTrace();
        }
    }
}