package com.example.demo.exception;


public class ServiceException extends RuntimeException{
    //什么也不返回
    public ServiceException() {
        super();
    }

    //返回异常信息(常用)
    public ServiceException(String message) {
        super(message);
    }

    //返回异常信息和异常对象(常用)
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    protected ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
