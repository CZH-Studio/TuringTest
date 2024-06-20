package com.example.demo;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Response<E> implements Serializable {
    //状态码
    private Long uid;
    //描述信息
    // private String message;
    //数据类型不确定,用E表示任何的数据类型,一个类里如果声明的有泛型的数据类型,类也要声明为泛型
    private E data;
	
    //无参构造
    public Response() {
    }

    //将状态码传给构造方法初始化对象
    public Response(Long uid) {
        this.uid = uid;
    }

    //将状态码和数据传给构造方法初始化对象
    public Response(Long uid, E data) {
        this.uid = uid;
        this.data = data;
    }

    public Response(E data) {
        this.data = data;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    
}

