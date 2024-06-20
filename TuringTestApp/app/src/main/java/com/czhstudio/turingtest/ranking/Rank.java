package com.czhstudio.turingtest.ranking;

public class Rank {
    private final String username;
    private final String score;
    private final String rank;

    public Rank(String username, int score, int rank) {
        this.username = username;
        this.score = String.valueOf(score);
        this.rank = String.valueOf(rank);
    }

    public Rank(String username, String score, String rank) {
        this.username = username;
        this.score = score;
        this.rank = rank;
    }

    public String getUsername() { return username; }
    public String getScore() { return score; }
    public String getRank() { return rank; }

}
