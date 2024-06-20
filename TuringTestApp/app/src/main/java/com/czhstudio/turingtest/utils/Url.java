package com.czhstudio.turingtest.utils;

public class Url {
    public static String IP;
    public static String URL_LOGIN;
    public static String URL_REGISTER;
    public static String URL_RANKING;
    public static String URL_INFO;
    public static String URL_LOGOUT;
    public static String URL_MATCH;
    public static String URL_GAME;
    public static int[] getIP() {
        String[] split = IP.split("\\.");
        int[] ip = new int[4];
        for (int i = 0; i < split.length; i++) {
            ip[i] = Integer.parseInt(split[i]);
        }
        return ip;
    }
    public static void setIP(int[] ips) {
        assert ips.length == 4;
        IP = ips[0] + "." + ips[1] + "." + ips[2] + "." + ips[3];
        updateUrl();
    }
    public static void setIP(String ip) {
        IP = ip;
        updateUrl();
    }
    private static void updateUrl(){
        URL_LOGIN = "http://" + IP + ":8080/login";
        URL_REGISTER = "http://" + IP + ":8080/register";
        URL_RANKING = "http://" + IP + ":8080/ranking";
        URL_INFO = "http://" + IP + ":8080/info";
        URL_LOGOUT = "logout";
        URL_MATCH = "ws://" + IP + ":8080/match";
        URL_GAME = "ws://" + IP + ":8080/game";
    }
}
