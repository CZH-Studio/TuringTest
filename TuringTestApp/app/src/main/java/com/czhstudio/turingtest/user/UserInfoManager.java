package com.czhstudio.turingtest.user;

import android.content.Context;
import android.content.SharedPreferences;
import com.czhstudio.turingtest.R;

import java.util.Arrays;
import java.util.List;

public class UserInfoManager {
    /* 使用SharedPreferences管理用户信息，在切换activity时信息不会丢失 */
    private static final List<Integer> rounds = Arrays.asList(6, 4, 2);

    public static String getUsername(Context context) {
        /* 获取用户名 */
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.string_userinfo), Context.MODE_PRIVATE);
        return preferences.getString(context.getString(R.string.string_username), "");
    }

    public static void setUsername(Context context, String username) {
        /* 设置用户名 */
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.string_userinfo), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.string_username), username);
        editor.apply();
    }

    public static String getPassword(Context context) {
        /* 获取密码 */
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.string_userinfo), Context.MODE_PRIVATE);
        return preferences.getString(context.getString(R.string.string_password), "");
    }

    public static void setPassword(Context context, String password) {
        /* 设置密码 */
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.string_userinfo), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.string_password), password);
        editor.apply();
    }

    public static int getUid(Context context) {
        /* 获取用户id */
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.string_userinfo), Context.MODE_PRIVATE);
        return Integer.parseInt(preferences.getString(context.getString(R.string.string_uid), "0"));
    }

    public static void setUid(Context context, int uid) {
        /* 设置用户id */
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.string_userinfo), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.string_uid), String.valueOf(uid));
        editor.apply();
    }

    public static int getScore(Context context) {
        /* 获取用户分数 */
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.string_userinfo), Context.MODE_PRIVATE);
        return Integer.parseInt(preferences.getString(context.getString(R.string.string_score), "0"));
    }

    public static void setScore(Context context, int score) {
        /* 设置用户分数 */
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.string_userinfo), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.string_score), String.valueOf(score));
        editor.apply();
    }

    public static int getDifficulty(Context context) {
        /* 获取难度 */
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.string_userinfo), Context.MODE_PRIVATE);
        return Integer.parseInt(preferences.getString(context.getString(R.string.string_difficulty), "0"));
    }

    public static void setDifficulty(Context context, int difficulty) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.string_userinfo), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.string_difficulty), String.valueOf(difficulty));
        editor.apply();
    }

    public static int getRound(int difficulty){
        return rounds.get(difficulty);
    }

    public static void setIP(Context context, String ip){
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.string_userinfo), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.string_ip), ip);
        editor.apply();
    }

    public static String getIP(Context context){
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.string_userinfo), Context.MODE_PRIVATE);
        return preferences.getString(context.getString(R.string.string_ip), "172.20.227.130");
    }
}
