package com.czhstudio.turingtest.user;

import com.czhstudio.turingtest.utils.Connection;
import com.czhstudio.turingtest.utils.Error;
import com.czhstudio.turingtest.utils.Url;

public class LoginAndRegister {
    /* 层次结构
     * _login 和 _register 以错误码形式返回
     * handleError 函数将错误码转换为错误信息并显示，返回是否有错误
     * 调用时只需要调用login 和 register 即可
     * */
    public static User login(String username, String password) {
        User user = new User(username, password);
        // 检查用户名
        if (isInvalidUsername(user)) return new User(Error.ERR_USERNAME);
        // 检查密码
        if (isInvalidPassword(user)) return new User(Error.ERR_PASSWORD);
        // 登录
        String body = Connection.post(Url.URL_LOGIN, user, User.MODE_LOGIN);
        if (body == null) return new User(Error.ERR_CONNECTION);  // 连接服务器失败错误
        UidAndScore info = Connection.parse(body, UidAndScore.class);
        if (info == null) return new User(Error.ERR_CONNECTION);  // 或者服务器返回的内容有误
        int uid = info.getUid();
        int score = info.getScore();
        try {
            if (uid < 0) return new User(uid);  // 登录失败
            return new User(username, password, uid, score);
        } catch (NumberFormatException e) {
            return new User(Error.ERR_CONNECTION);  // 或者服务器返回的内容有误
        }
    }

    public static User register(String username, String password, String passwordConfirm) {
        // 检查重复输入的密码是否相同
        if (!isPasswordSame(password, passwordConfirm)) return new User(Error.ERR_PASSWORD_DIFF);
        User user = new User(username, password);
        // 检查用户名
        if (isInvalidUsername(user)) return new User(Error.ERR_USERNAME);
        // 检查密码
        if (isInvalidPassword(user)) return new User(Error.ERR_PASSWORD);
        // 注册
        String body = Connection.post(Url.URL_REGISTER, user, User.MODE_REGISTER);
        if (body == null) return new User(Error.ERR_CONNECTION);    // 连接服务器失败错误
        UidAndScore info = Connection.parse(body, UidAndScore.class);
        if (info == null) return new User(Error.ERR_CONNECTION);  // 或者服务器返回的内容有误
        int uid = info.getUid();
        int score = info.getScore();
        try {
            if (uid < 0) return new User(uid);
            return new User(username, password, uid, score);
        } catch (NumberFormatException e) {
            return new User(Error.ERR_CONNECTION);  // 或者服务器返回的内容有误
        }
    }

    private static boolean isInvalidUsername(User user) {
        // 检查用户名是否符合规范
        return user.getUsername() == null || user.getUsername().length() > 10 || user.getUsername().isEmpty();
    }

    private static boolean isInvalidPassword(User user) {
        // 检查密码是否符合规范
        return user.getPassword().length() <= 5 || user.getPassword() == null;
    }

    private static boolean isPasswordSame(String pwd1, String pwd2) {
        return pwd1.equals(pwd2);
    }

}
