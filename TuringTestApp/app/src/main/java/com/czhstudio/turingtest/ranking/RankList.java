package com.czhstudio.turingtest.ranking;

import java.util.ArrayList;
import java.util.List;

public class RankList {
    private final List<Rank> data;

    public List<Rank> getData() {
        return data;
    }

    public RankList(List<Rank> rankList) {
        this.data = rankList;
    }

    public RankList(){
        this.data = new ArrayList<>();
    }
}
