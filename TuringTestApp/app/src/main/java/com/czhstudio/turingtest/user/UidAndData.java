package com.czhstudio.turingtest.user;

import com.czhstudio.turingtest.utils.Entity;

public class UidAndData extends Entity {

    private final int uid;
    private final String data;

    public UidAndData(int uid, String data) {
        this.uid = uid;
        this.data = data;
    }

    @Override
    public String toPost(int mode) {
        return "{\"uid\": " + uid + ", \"data\": \"" + data + "\"}";
    }

    @Override
    public String toGet(int mode) {
        return "?uid=" + uid + "&data=" + data;
    }

    public String getData(){
        return data;
    }

    public int getUid(){ return uid; }
}
