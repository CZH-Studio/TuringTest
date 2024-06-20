package com.czhstudio.turingtest.game;

import com.czhstudio.turingtest.user.UidAndData;

public abstract class MyWebSocketThread extends Thread{
    public abstract void onReceive(UidAndData data);
    public abstract void onTimeout();
    public abstract void onOpen();
    public abstract void onClose();
}
