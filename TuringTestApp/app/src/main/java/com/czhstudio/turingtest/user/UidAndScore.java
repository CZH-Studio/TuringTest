package com.czhstudio.turingtest.user;

public class UidAndScore {
    private final int uid;
    private final int score;

    public UidAndScore(int uid, int score) {
        this.uid = uid;
        this.score = score;
    }

    public int getUid() {
        return uid;

    }

    public int getScore() {
        return score;
    }
}
