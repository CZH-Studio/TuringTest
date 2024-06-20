package com.czhstudio.turingtest.game;

import com.czhstudio.turingtest.user.UidAndData;
import com.czhstudio.turingtest.utils.Connection;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class MyWebSocketClient extends WebSocketClient {

    private final MyWebSocketThread callerThread;        // 调用者线程
    private final Timer timer;                // 心跳定时器
    private final TimerTask timerTask;        // 心跳任务
    private final int uid;                    // uid
    private int clientSeq = 0;                // 客户端心跳序列号
    private int serverSeq = 0;                // 服务器发来的最新的心跳序列号

    public MyWebSocketClient(String url, MyWebSocketThread callerThread, int uid) throws URISyntaxException {
        super(new URI(url));
        System.out.println("init websocket client");
        this.callerThread = callerThread;
        this.uid = uid;
        this.timer = new Timer(true);
        this.timerTask = new TimerTask() {
            int fail = 0;
            @Override
            public void run() {
//                System.out.println("execute heartbeat task -fail:" + fail);
                // 获取连接状态
                if (MyWebSocketClient.this.isOpen()) {
                    fail = 0;
                    if (clientSeq - serverSeq >= 5){
                        // 如果超过5次没有收到心跳包，则自动断开连接
                        MyWebSocketClient.this.callerThread.onTimeout();
                    } else {
                        String message = "heartbeat " + clientSeq;
                        MyWebSocketClient.this.send(new UidAndData(MyWebSocketClient.this.uid, message).toPost(0));
                        clientSeq++;
                    }
                } else if (MyWebSocketClient.this.getReadyState() == ReadyState.NOT_YET_CONNECTED){
                    // 如果连接尚未建立，则失败次数+1
                    fail++;
                } else {
                    // 如果连接已经断开，则直接返回超时错误
                    timer.cancel();
                    timerTask.cancel();
                    MyWebSocketClient.this.callerThread.onTimeout();
                }
                if (fail >= 5) {
                    // 如果失败次数超过5次，则自动断开连接
                    timer.cancel();
                    timerTask.cancel();
                    MyWebSocketClient.this.callerThread.onTimeout();
                }
            }
        };
        this.addHeader("uid", String.valueOf(uid));
        this.connect();
        this.timer.scheduleAtFixedRate(this.timerTask, 1000, 1000);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("GameWebSocketClient onOpen");
        callerThread.onOpen();
    }

    @Override
    public void onMessage(String s) {
//        System.out.printf("GameWebSocketClient onMessage: %s%n", s);
        UidAndData message = Connection.parse(s, UidAndData.class);
        if (message == null) return;
        if (message.getData().startsWith("heartbeat")) {
            // 如果是心跳消息，则只需要更新serverSeq
            serverSeq = Integer.max(Integer.parseInt(message.getData().split(" ")[1]), serverSeq);
//            System.out.println("serverSeq update to " + serverSeq);
        } else {
            // 否则向调用者线程发送消息
            callerThread.onReceive(message);
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("GameWebSocketClient onClose");
        this.callerThread.onClose();
    }

    @Override
    public void onError(Exception e) {
        System.out.printf("GameWebSocketClient onError: %s", e.getMessage());
        callerThread.interrupt();
    }

    @Override
    public void close(){
        this.timer.cancel();
        this.timerTask.cancel();
        super.close();
    }

    @Override
    public void send(String s) {
//        System.out.println(s);
        super.send(s);
    }
}
