package com.czhstudio.turingtest.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.czhstudio.turingtest.R;
import com.czhstudio.turingtest.user.User;
import com.czhstudio.turingtest.user.UserInfoManager;
import com.czhstudio.turingtest.utils.Connection;
import com.czhstudio.turingtest.utils.Url;

public class MeActivity extends AppCompatActivity {

    private TextView tvUsername;
    private TextView tvUid;
    private TextView tvScore;

  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        Button btnBack = findViewById(R.id.me_back);
        tvUsername = findViewById(R.id.me_username);
        tvUid = findViewById(R.id.me_uid);
        tvScore = findViewById(R.id.me_score);
        TextView tvTitle = findViewById(R.id.me_title);

        // 设置字体
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/write.ttf");
        tvUsername.setTypeface(face);
        tvUid.setTypeface(face);
        tvScore.setTypeface(face);
        tvTitle.setTypeface(face);
        btnBack.setTypeface(face);

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onStart(){
        super.onStart();
        getInfo();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(MeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_new, R.anim.slide_right_old);
    }

    private void getInfo(){
        new Thread(() -> {
            String body = Connection.post(Url.URL_INFO, new User(UserInfoManager.getUid(this)), User.MODE_GET_USER_INFO);
            User user = Connection.parse(body, User.class);
            if (user == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.err_connection, Toast.LENGTH_SHORT).show();
                });
                return;
            }
            int score = user.getScore();
            UserInfoManager.setScore(this, score);
            runOnUiThread(() -> {
                String name = this.getString(R.string.label_username) + UserInfoManager.getUsername(this);
                tvUsername.setText(name);
                String id = this.getString(R.string.label_uid) + UserInfoManager.getUid(this);
                tvUid.setText(id);
                String sc = this.getString(R.string.label_score) + score;
                tvScore.setText(sc);
            });
        }).start();
    }
}
