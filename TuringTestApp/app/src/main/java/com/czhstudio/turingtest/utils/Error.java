package com.czhstudio.turingtest.utils;

import com.czhstudio.turingtest.R;

import java.util.HashMap;

public class Error {
    /* Error Const Definition */
    public static final int ERR_SERVER = 0;
    public static final int ERR_USERNAME = -1;
    public static final int ERR_PASSWORD = -2;
    public static final int ERR_CONNECTION = -3;
    public static final int ERR_LOGIN_USERNAME = -4;
    public static final int ERR_LOGIN_PASSWORD = -5;
    public static final int ERR_REGISTER = -6;
    public static final int ERR_PASSWORD_DIFF = -7;
    public static final int ERR_EMPTY = -8;
    public static final int ERR_NOT_START = -9;
    public static final int ERR_MISMATCH = -10;
    public static final int ERR_NOT_MY_TURN = -11;
    public static final int ERR_OPPOSITE_OFFLINE = -12;

    /* Error map */
    private static final HashMap<Integer, Integer> errMap = new HashMap<>();
    static{
        errMap.put(ERR_SERVER, R.string.err_server);
        errMap.put(ERR_USERNAME, R.string.err_invalid_username);
        errMap.put(ERR_PASSWORD, R.string.err_invalid_password);
        errMap.put(ERR_CONNECTION, R.string.err_connection);
        errMap.put(ERR_LOGIN_USERNAME, R.string.err_login_username);
        errMap.put(ERR_LOGIN_PASSWORD, R.string.err_login_password);
        errMap.put(ERR_REGISTER, R.string.err_register);
        errMap.put(ERR_PASSWORD_DIFF, R.string.err_pwd_diff);
        errMap.put(ERR_EMPTY, R.string.err_empty);
        errMap.put(ERR_NOT_START, R.string.err_not_start);
        errMap.put(ERR_MISMATCH, R.string.err_mismatch);
        errMap.put(ERR_NOT_MY_TURN, R.string.err_not_my_turn);
        errMap.put(ERR_OPPOSITE_OFFLINE, R.string.err_opposite_offline);
    }

    /**
     * 获取错误信息对应的字符串id
     * @param errno 错误代码
     * @return 字符串id
     */
    public static int getErrMsg(int errno) {
        assert errMap.containsKey(errno);
        Integer returnCode = errMap.get(errno);
        return returnCode == null ? 1 : returnCode;
    }
}

