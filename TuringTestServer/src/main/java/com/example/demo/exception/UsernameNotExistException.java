package com.example.demo.exception;

public class UsernameNotExistException extends ServiceException{

    //什么也不返回
    public UsernameNotExistException() {
        super();
    }

    //返回异常信息(常用)
    public UsernameNotExistException(String message) {
        super(message);
    }

    //返回异常信息和异常对象(常用)
    public UsernameNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsernameNotExistException(Throwable cause) {
        super(cause);
    }

    protected UsernameNotExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
