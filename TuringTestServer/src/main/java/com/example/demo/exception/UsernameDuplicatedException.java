package com.example.demo.exception;

public class UsernameDuplicatedException extends ServiceException{

    public UsernameDuplicatedException() {
        super();
    }

    //返回异常信息(常用)
    public UsernameDuplicatedException(String message) {
        super(message);
    }

    //返回异常信息和异常对象(常用)
    public UsernameDuplicatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsernameDuplicatedException(Throwable cause) {
        super(cause);
    }

    protected UsernameDuplicatedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
