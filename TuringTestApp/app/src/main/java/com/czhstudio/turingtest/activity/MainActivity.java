package com.czhstudio.turingtest.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.czhstudio.turingtest.R;
import com.czhstudio.turingtest.activity.dialog.ErrorDialog;
import com.czhstudio.turingtest.game.MyWebSocketClient;
import com.czhstudio.turingtest.game.MyWebSocketThread;
import com.czhstudio.turingtest.user.UidAndData;
import com.czhstudio.turingtest.user.User;
import com.czhstudio.turingtest.user.UserInfoManager;
import com.czhstudio.turingtest.utils.Connection;
import com.czhstudio.turingtest.utils.Error;
import com.czhstudio.turingtest.utils.Url;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPlay = findViewById(R.id.main_play);
        Button btnLogout = findViewById(R.id.main_logout);
        TextView tvTitle = findViewById(R.id.main_title);
        ImageButton btnSettings = findViewById(R.id.main_settings);
        ImageButton btnRanking = findViewById(R.id.main_ranking);
        ImageButton btnMe = findViewById(R.id.main_me);

        // 设置字体
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/write.ttf");
        btnPlay.setTypeface(face);
        btnLogout.setTypeface(face);
        tvTitle.setTypeface(face);

        /* 开始游戏按钮 */
        btnPlay.setOnClickListener(v -> {
            new DifficultyChooseDialog(MainActivity.this).create().show();
        });

        /* 退出登录按钮 */
        btnLogout.setOnClickListener(v -> {
            logoutThread();
        });

        /* 设置按钮 */
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_left_new, R.anim.slide_left_old);
        });

        /* 排行榜按钮 */
        btnRanking.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RankingActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_left_new, R.anim.slide_left_old);
        });

        /* 我的按钮 */
        btnMe.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_left_new, R.anim.slide_left_old);
        });
    }

    @Override
    public void onBackPressed(){
        backToLogin();
    }

    private void logoutThread(){
        new Thread(() -> {
            User user = new User(UserInfoManager.getUid(this));
            backToLogin();
            String body = Connection.get(Url.URL_LOGOUT, user, User.MODE_LOGOUT);
            // TODO: 处理登出逻辑
        }).start();
    }

    private void backToLogin(){
        runOnUiThread(() -> {
            // 退出登录后跳转
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_right_new, R.anim.slide_right_old);
        });
    }

    private class MatchDialog{
        private final AlertDialog dialog;
        private int time = 0;
        private Timer updateTimeTimer;
        private TimerTask updateTimeTimerTask;
        private final int difficulty;
        private final Context context;
        private final MatchThread matchThread;
        private boolean proactivelyClose = false;

        /**
         * 结束匹配对话框，首先关闭时间更新任务，然后关闭对话框。
         * 如果是主动取消匹配，那么要关闭websocket
         */
        public void dismiss(){
            if (updateTimeTimer != null) updateTimeTimer.cancel();
            if (updateTimeTimerTask != null) updateTimeTimerTask.cancel();
            dialog.dismiss();
            matchThread.interrupt();
        }

        private class MatchThread extends MyWebSocketThread {
            private MyWebSocketClient client;

            @Override
            public void run(){
                try {
                    client = new MyWebSocketClient(Url.URL_MATCH, this, UserInfoManager.getUid(MainActivity.this));
                } catch (URISyntaxException e) {
                    this.interrupt();
                }
            }

            @Override
            public void interrupt(){
                if (client != null) client.close();
                super.interrupt();
            }

            @Override
            public void onReceive(UidAndData data) {
                if (data.getData().equals("matchsuccess")) {
                    // 如果匹配成功，先关闭对话框，然后跳转
                    MatchDialog.this.dismiss();
                    Intent intent = new Intent(MatchDialog.this.context, PlayActivity.class);
                    MatchDialog.this.context.startActivity(intent);
                    overridePendingTransition(R.anim.slide_left_new, R.anim.slide_left_old);
                } else {
                    // 如果匹配失败了，就直接关闭对话框
                    MatchDialog.this.dismiss();
                    onError(Error.ERR_MISMATCH);
                }
            }

            @Override
            public void onTimeout(){
                MatchDialog.this.dismiss();
                if (!MatchDialog.this.proactivelyClose) onError(Error.ERR_CONNECTION);
            }

            @Override
            public void onOpen(){
                client.send(new UidAndData(UserInfoManager.getUid(context), "match").toPost(0));
            }

            @Override
            public void onClose() {
                MatchDialog.this.dismiss();
                if (!MatchDialog.this.proactivelyClose) onError(Error.ERR_CONNECTION);
            }
        }

        /**
         * 构建等待对话框
         */
        public MatchDialog(Context context, int difficulty) {
            this.difficulty = difficulty;
            this.context = context;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            String title = context.getString(R.string.string_matching) +
                    "（" + context.getResources().getStringArray(R.array.string_difficulties)[MatchDialog.this.difficulty] + "）";
            builder.setTitle(title);
            builder.setCancelable(false);
            builder.setNegativeButton(R.string.string_cancel, (dialog, which) -> {
                this.proactivelyClose = true;
                this.dismiss();
            });
            builder.setMessage(getTimeString());
            this.dialog = builder.create();
            this.dialog.show();
            updateTime();
            this.matchThread = new MatchThread();
            this.matchThread.start();
        }

        /**
         * 更新等待时间
         */
        private void updateTime(){
            updateTimeTimer = new Timer(true);
            updateTimeTimerTask = new TimerTask() {
                @Override
                public void run() {
                    time++;
                    dialog.setMessage(getTimeString());
                }
            };
            updateTimeTimer.scheduleAtFixedRate(updateTimeTimerTask, 1000, 1000);
        }

        /**
         * 更新时间显示
         * @return 时间字符串
         */
        private String getTimeString(){
            int min = time / 60;
            int sec = time % 60;
            return context.getString(R.string.string_matching_time) + String.format(Locale.getDefault(), "%02d:%02d", min, sec);
        }
    }

    private class DifficultyChooseDialog extends AlertDialog.Builder{
        private int difficulty = 0;

        public DifficultyChooseDialog(Context context) {
            super(context);
            super.setCancelable(false); // 点击外部区域不要关闭对话框
            super.setTitle(R.string.string_difficulty_choose);
            super.setSingleChoiceItems(R.array.string_difficulties, difficulty, (dialog, which) -> difficulty = which);
            super.setPositiveButton(R.string.string_confirm, (dialog, which) -> {
                UserInfoManager.setDifficulty(context, this.difficulty);
                dialog.dismiss();
                new MatchDialog(context, this.difficulty);
            });
            super.setNegativeButton(R.string.string_cancel, (dialog, which) -> dialog.dismiss());
        }

        @NotNull
        @Override
        public AlertDialog create() {
            return super.create();
        }
    }

    private void onError(int errno){
        runOnUiThread(() -> new ErrorDialog(this, errno, false).create().show());
    }
}