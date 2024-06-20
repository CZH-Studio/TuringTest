package com.example.demo.dto;

public class GameMessege<T> {

    private T data;
    private long uid;

    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
    public long getUid() {
        return uid;
    }
    public void setUid(long uid) {
        this.uid = uid;
    }

    public GameMessege() {
    }

    //将状态码传给构造方法初始化对象
    public GameMessege(Long uid) {
        this.uid = uid;
    }

    //将状态码和数据传给构造方法初始化对象
    public GameMessege(Long uid, T data) {
        this.uid = uid;
        this.data = data;
    }

}
