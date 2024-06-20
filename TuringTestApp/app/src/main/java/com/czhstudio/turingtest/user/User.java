package com.czhstudio.turingtest.user;

import com.czhstudio.turingtest.utils.Entity;

public class User extends Entity {
    /* 不同的字符串解析方式 */
    public static final int MODE_LOGIN = 0;
    public static final int MODE_REGISTER = 1;
    public static final int MODE_GET_RANKING = 2;
    public static final int MODE_LOGOUT = 3;
    public static final int MODE_GET_USER_INFO = 4;

    /* 用户信息 */
    private final String username;  // 用户名
    private final String password;  // 密码
    private final int uid;          // uid，在登录和注册时，如果大于0，标识uid；小于0标识错误代码
    private final int score;        // 得分

    @Override
    public String toPost(int mode){
        switch (mode) {
            case MODE_LOGIN:
            case MODE_REGISTER:
                return "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";
            case MODE_GET_RANKING:
            case MODE_LOGOUT:
            case MODE_GET_USER_INFO:
                return "{\"uid\":" + uid + "}";
            default:
                return "";
        }
    }

    @Override
    public String toGet(int mode){
        switch (mode) {
            case MODE_LOGIN:
            case MODE_REGISTER:
                return "?username=" + username + "&password=" + password;
            case MODE_GET_RANKING:
                return "?uid=" + uid;
            default:
                return "";
        }
    }

    public User(String username, String password){
        /* 用于登录和注册的构造方法 */
        this.username = username;
        this.password = String.valueOf(password.hashCode());
        this.uid = 0;
        this.score = 0;
    }

    public User(String username, String password, int uid, int score){
        /* 用于登录完成后获取用户信息的构造方法 */
        this.username = username;
        this.password = password;
        this.uid = uid;
        this.score = score;
    }

    public User(int errCode){
        /* 用于标识错误代码的构造方法 */
        this.username = "";
        this.password = "";
        this.uid = errCode;
        this.score = 0;
    }

    public int getUid() {
        return uid;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }
}
