package com.czhstudio.turingtest.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.czhstudio.turingtest.R;
import com.czhstudio.turingtest.ranking.RankList;
import com.czhstudio.turingtest.ranking.RankListAdapter;
import com.czhstudio.turingtest.user.User;
import com.czhstudio.turingtest.user.UserInfoManager;
import com.czhstudio.turingtest.utils.Connection;
import com.czhstudio.turingtest.utils.Error;
import com.czhstudio.turingtest.utils.Url;

public class RankingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        Button btnBack = findViewById(R.id.ranking_back);
        RecyclerView ranking = this.findViewById(R.id.ranking_list);
        // 设置字体
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/write.ttf");
        TextView tvTitle = findViewById(R.id.ranking_title);
        tvTitle.setTypeface(face);
        btnBack.setTypeface(face);

        ranking.setLayoutManager(new LinearLayoutManager(this));
        ranking.setAdapter(new RankListAdapter(this, new RankList()));
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onStart() {
        super.onStart();
        getRankThread();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(RankingActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_new, R.anim.slide_right_old);
    }

    private void getRankThread(){
        // 获取排行榜的线程
        new Thread(() -> {
            User user = new User(UserInfoManager.getUid(this));
            String body = Connection.post(Url.URL_RANKING, user, User.MODE_GET_RANKING);
            if (body == null){
                onError(Error.ERR_CONNECTION);
                return;
            }
            RankList rankList = Connection.parse(body, RankList.class);
            if (rankList == null || rankList.getData() == null){
                onError(Error.ERR_SERVER);
                return;
            }
            updateRankingList(rankList);
        }).start();
    }

    private void updateRankingList(RankList rankList){
        // 获取到排行榜数据后，更新排行榜的内容
        runOnUiThread(() -> {
            RecyclerView ranking = this.findViewById(R.id.ranking_list);
            ranking.setAdapter(new RankListAdapter(this, rankList));
        });
    }

    private void onError(int errno){
        // 无法获取排行榜时，执行此
        runOnUiThread(() -> Toast.makeText(RankingActivity.this, Error.getErrMsg(errno), Toast.LENGTH_SHORT).show());
    }
}

