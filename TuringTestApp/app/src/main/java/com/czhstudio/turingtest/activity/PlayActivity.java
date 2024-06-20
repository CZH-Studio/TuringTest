package com.czhstudio.turingtest.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.czhstudio.turingtest.R;
import com.czhstudio.turingtest.activity.dialog.ErrorDialog;
import com.czhstudio.turingtest.game.MyWebSocketClient;
import com.czhstudio.turingtest.game.MyWebSocketThread;
import com.czhstudio.turingtest.user.UidAndData;
import com.czhstudio.turingtest.user.UserInfoManager;
import com.czhstudio.turingtest.utils.Error;
import com.czhstudio.turingtest.utils.Url;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends AppCompatActivity {
    /* components */
    /**
     * 用户发送的问题
     */
    private TextView tvQuestion;
    /**
     * 系统响应的答案
     */
    private TextView tvAnswer;
    /**
     * 用户的输入框
     */
    private EditText etQuestion;
    /**
     * 发送按钮
     */
    private Button btnSend;
    /**
     * 放弃按钮
     */
    private Button btnGiveUp;
    /**
     * 轮数显示框
     */
    private TextView tvRound;
    /**
     * 我方倒计时显示
     */
    private TextView tvCountdownMe;
    /**
     * 对方倒计时显示
     */
    private TextView tvCountdownYou;
    /**
     * 我方倒计时进度条
     */
    private ProgressBar pbCountdownMe;
    /**
     * 对方倒计时进度条
     */
    private ProgressBar pbCountdownYou;

    /* variables */
    /**
     * 总轮数
     */
    private int roundTotal;
    /**
     * 当前轮数
     */
    private int roundCurrent;
    /**
     * 是否先手
     */
    private boolean first;

    /**
     * 游戏状态
     */
    enum GameStatus {
        WAIT,
        START,
        END
    }

    private GameStatus gameStatus = GameStatus.WAIT;
    /**
     * 是否到我的回合
     */
    private boolean myTurn = false;
    /**
     * 决策：是否是AI
     */
    private boolean isRobot;
    /**
     * 用户id
     */
    private int uid;

    /* Thread And Dialog */
    /**
     * 游戏连接线程
     */
    private GameThread gameThread;
    /**
     * 自己和对方的倒计时任务
     */
    private Timer myTimer;
    private TimerTask myTimerTask;
    private Timer yourTimer;
    private TimerTask yourTimerTask;
    private int myTime;
    private int yourTime;
    /**
     * 结束的对话框
     */
    private AlertDialog resultDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        // 获取组件
        tvQuestion = findViewById(R.id.play_bubble_question);
        tvAnswer = findViewById(R.id.play_bubble_answer);
        etQuestion = findViewById(R.id.play_question);
        btnSend = findViewById(R.id.play_send);
        btnGiveUp = findViewById(R.id.play_give_up);
        tvRound = findViewById(R.id.play_round);
        tvCountdownMe = findViewById(R.id.play_countdown_text_me);
        tvCountdownYou = findViewById(R.id.play_countdown_text_you);
        pbCountdownMe = findViewById(R.id.play_countdown_progressbar_me);
        pbCountdownYou = findViewById(R.id.play_countdown_progressbar_you);

        // 设置字体
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/write.ttf");
        tvQuestion.setTypeface(face);
        tvAnswer.setTypeface(face);
        etQuestion.setTypeface(face);
        tvRound.setTypeface(face);
        tvCountdownMe.setTypeface(face);
        tvCountdownYou.setTypeface(face);
        btnSend.setTypeface(face);
        btnGiveUp.setTypeface(face);

        // 初始化变量
        int difficulty = UserInfoManager.getDifficulty(this);
        roundTotal = UserInfoManager.getRound(difficulty);
        roundCurrent = 0;
        uid = UserInfoManager.getUid(this);

        // 绑定发送按钮事件
        btnSend.setOnClickListener(v -> {
            if (gameThread.client == null) {
                onErrorToast(Error.ERR_CONNECTION);
                return;
            }
            if (!gameThread.client.isOpen()) {
                onErrorToast(Error.ERR_CONNECTION);
                return;
            }
            if (gameStatus == GameStatus.WAIT) {
                onErrorToast(Error.ERR_NOT_START);
                return;
            }
            if (!myTurn) {
                onErrorToast(Error.ERR_NOT_MY_TURN);
                return;
            }
            String question = etQuestion.getText().toString();
            if (question.isEmpty()) {
                onErrorToast(Error.ERR_EMPTY);
                return;
            }
            gameThread.send(new UidAndData(uid, question).toPost(0), true, question);
        });

        // 绑定放弃按钮
        btnGiveUp.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.string_give_up_title);
            builder.setMessage(R.string.string_give_up_message);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.string_confirm, (dialog, which) -> {
                PlayActivity.this.gameStatus = GameStatus.END;
                dialog.dismiss();
                PlayActivity.this.gameThread.interrupt();
                Intent intent = new Intent(PlayActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                PlayActivity.this.startActivity(intent);
                overridePendingTransition(R.anim.slide_right_new, R.anim.slide_right_old);
            });
            builder.setNegativeButton(R.string.string_cancel, (dialog, which) -> {
                dialog.dismiss();
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        // 设置轮数显示
        updateRound(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 为了防止界面没有加载，游戏线程在界面加载完成后启动
        System.out.println("start");
        this.gameThread = new GameThread();
        this.gameThread.start();
    }

    @Override
    public void onBackPressed() {
    }

    /**
     * 点击空白区域关闭输入法
     *
     * @param ev 点击事件
     * @return 未知
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);

            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            // 点击的是输入框区域，保留点击EditText的事件
            return !(event.getX() > left) || !(event.getX() < right)
                    || !(event.getY() > top) || !(event.getY() < bottom);
        }
        return false;
    }

    /**
     * 游戏线程
     */
    public class GameThread extends MyWebSocketThread {
        /**
         * websocket客户端
         */
        private MyWebSocketClient client;

        /**
         * 启动线程，初始化websocket客户端
         */
        @Override
        public void run() {
            try {
                client = new MyWebSocketClient(Url.URL_GAME, GameThread.this, UserInfoManager.getUid(PlayActivity.this));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 线程中断
         */
        @Override
        public void interrupt() {
            if (client != null) client.close();
            stopMyCountdown();
            stopYourCountdown();
            super.interrupt();
        }

        @Override
        public void onReceive(UidAndData data) {
            int statusCode = data.getUid();
            String message = data.getData();
            System.out.println("On receive, code:" + statusCode);
            System.out.println("On receive, message:" + message);
            if (statusCode == -100) {
                if (gameStatus == GameStatus.WAIT) {
                    first = true;
                    gameStatus = GameStatus.START;
                    eventStartGame();
                }
            } else if (statusCode == -101) {
                if (gameStatus == GameStatus.WAIT) {
                    first = false;
                    gameStatus = GameStatus.START;
                    eventStartGame();
                }
            } else if (statusCode == -200) {
                if (gameStatus == GameStatus.END) {
                    eventAfterResult("你赢了！");
                } else if (gameStatus == GameStatus.START) {
                    onErrorDialog(Error.ERR_OPPOSITE_OFFLINE);
                    this.interrupt();
                }
            } else if (statusCode == -201) {
                eventAfterResult("你输了！");
            } else {
                // 处理发送过来的内容
                if (gameStatus == GameStatus.START) {
                    PlayActivity.this.onReceive(message);
                }
            }
        }

        public void send(String s, boolean updateUI, String s1) {
            // 向服务器发送内容，如果是文字的话那么需要更新UI
            this.client.send(s);
            if (updateUI) {
                onSend(s1);
            }
        }

        @Override
        public void onOpen() {
        }

        @Override
        public void onClose() {
            if (gameStatus != GameStatus.END) onErrorDialog(Error.ERR_CONNECTION);
        }

        @Override
        public void onTimeout() {
            onErrorDialog(Error.ERR_CONNECTION);
            this.interrupt();
        }
    }

    /**
     * 选择对话框
     */
    private class ChoiceDialog extends AlertDialog.Builder {

        public ChoiceDialog(@NonNull @NotNull Context context) {
            super(context);
            super.setCancelable(false);
            super.setTitle(R.string.string_decision);
            super.setPositiveButton(R.string.string_human, (dialog, which) -> {
                PlayActivity.this.isRobot = false;
                dialog.dismiss();
                eventAfterChoice();
            });
            super.setNegativeButton(R.string.string_robot, (dialog, which) -> {
                PlayActivity.this.isRobot = true;
                dialog.dismiss();
                eventAfterChoice();
            });
        }
    }

    private class ResultDialog extends AlertDialog.Builder {

        public ResultDialog(@NonNull @NotNull Context context) {
            super(context);
            super.setTitle(R.string.string_result);
            super.setMessage(R.string.string_waiting);
            super.setPositiveButton(R.string.string_confirm, (dialog, which) -> {
                PlayActivity.this.gameThread.interrupt();
                Intent intent = new Intent(PlayActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                PlayActivity.this.startActivity(intent);
                overridePendingTransition(R.anim.slide_right_new, R.anim.slide_right_old);
            });
        }
    }

    /**
     * 所有轮数完成后，结束游戏的操作：启动双方倒计时进度条、启动等待5秒线程
     */
    private void eventFinishGame() {
        gameStatus = GameStatus.END;
        this.gameThread.send(new UidAndData(-300, "finish").toPost(0), false, "");
        startMyCountdown(5);
        startYourCountdown(5);
        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    PlayActivity.this.pbCountdownMe.setVisibility(View.INVISIBLE);
                    PlayActivity.this.pbCountdownYou.setVisibility(View.INVISIBLE);
                    PlayActivity.this.tvCountdownMe.setVisibility(View.INVISIBLE);
                    PlayActivity.this.tvCountdownYou.setVisibility(View.INVISIBLE);
                    new ChoiceDialog(PlayActivity.this).create().show();
                });
            }
        };
        timer.schedule(timerTask, 5100);
    }

    /**
     * 选择之后
     */
    private void eventAfterChoice() {
        runOnUiThread(() -> {
            this.resultDialog = new ResultDialog(this).create();
            this.resultDialog.show();
        });
        this.gameThread.send(new UidAndData(uid, String.valueOf(isRobot)).toPost(0), false, null);
    }

    /**
     * 知道了结果之后
     */
    private void eventAfterResult(String s) {
        if (this.resultDialog == null) {
            this.resultDialog = new ResultDialog(this).create();
            this.resultDialog.show();
            this.resultDialog.setMessage("由于对方断线，你赢了！");
        } else {
            this.resultDialog.setMessage(s);
        }
        // 断开游戏线程
        this.gameThread.interrupt();
    }

    /**
     * 接收到开始游戏的消息时，UI的操作
     */
    private void eventStartGame() {
        runOnUiThread(() -> {
            if (first) {
                startMyCountdown(60);
            } else {
                startYourCountdown(60);
            }
        });
    }

    /**
     * 弹出错误消息的Toast
     *
     * @param errno 错误号
     */
    private void onErrorToast(int errno) {
        runOnUiThread(() -> Toast.makeText(this, Error.getErrMsg(errno), Toast.LENGTH_SHORT).show());
    }

    /**
     * 弹出错误消息的对话框，并返回主页
     *
     * @param errno 错误号
     */
    private void onErrorDialog(int errno) {
        runOnUiThread(() -> new ErrorDialog(this, errno, true).create().show());
    }

    /**
     * 接收消息时，UI的操作
     *
     * @param s 消息字符串
     */
    private void onReceive(String s) {
        runOnUiThread(() -> {
            tvAnswer.setVisibility(View.VISIBLE);
            tvAnswer.setText(s);
            if (first) {
                // 如果我方是先手
                if (roundTotal - roundCurrent == 1) {
                    // 如果对话已经完成，那么关闭双方的倒计时任务
                    stopMyCountdown();
                    stopYourCountdown();
                } else {
                    // 没完成，那么对方倒计时停止，我方倒计时启动
                    stopYourCountdown();
                    startMyCountdown(60);
                }
                updateRound(true);
            } else {
                // 如果我方是后手，那么对方倒计时停止，我方启动
                stopYourCountdown();
                startMyCountdown(60);
            }
        });
    }

    /**
     * 发送消息时，UI的操作
     *
     * @param s 消息字符串
     */
    private void onSend(String s) {
        runOnUiThread(() -> {
            tvQuestion.setVisibility(View.VISIBLE);
            tvQuestion.setText(s);
            etQuestion.setText("");
            if (!first) {
                // 如果我方是后手
                if (roundTotal - roundCurrent == 1) {
                    // 如果对话已经完成，那么只需要关闭双方的倒计时任务
                    System.out.println("finished!");
                    stopMyCountdown();
                    stopYourCountdown();
                } else {
                    // 如果对话没有完成，那么该启动对方倒计时
                    stopMyCountdown();
                    startYourCountdown(60);
                }
                updateRound(true);
            } else {
                // 如果是我方先手，那我方倒计时停止，对方倒计时启动
                stopMyCountdown();
                startYourCountdown(60);
            }
        });
    }

    /**
     * 更新轮数显示
     *
     * @param increase 当前轮数是否+1
     */
    private void updateRound(boolean increase) {
        runOnUiThread(() -> {
            if (increase) roundCurrent++;
            String round = this.getString(R.string.label_round) + roundCurrent + " / " + roundTotal;
            this.tvRound.setText(round);
            if (gameStatus == GameStatus.START && roundCurrent == roundTotal) {
                // 如果游戏开始了，并且当前轮数已经达到上限，那么执行完成游戏的动作
                eventFinishGame();
            }
        });
    }

    /**
     * 更新我方倒计时的UI操作
     */
    private void updateMyCountdown() {
        runOnUiThread(() -> {
            String time = this.getString(R.string.label_countdown) + myTime + "秒";
            this.tvCountdownMe.setText(time);
            this.pbCountdownMe.setProgress(myTime);
        });
    }

    /**
     * 停止我方倒计时
     */
    private void stopMyCountdown() {
        runOnUiThread(() -> {
            if (this.myTimerTask != null) this.myTimerTask.cancel();
            if (this.myTimer != null) this.myTimer.cancel();
            this.pbCountdownMe.setVisibility(View.INVISIBLE);
            this.tvCountdownMe.setVisibility(View.INVISIBLE);
        });
    }

    /**
     * 更新我方倒计时的线程
     */
    private void startMyCountdown(int time) {
        runOnUiThread(() -> {
            this.myTurn = true;
            this.myTime = time;
            this.btnSend.setEnabled(true);
            this.pbCountdownMe.setProgress(time);
            this.pbCountdownMe.setVisibility(View.VISIBLE);
            this.tvCountdownMe.setVisibility(View.VISIBLE);
        });
        myTimer = new Timer(true);
        myTimerTask = new TimerTask() {
            @Override
            public void run() {
                PlayActivity.this.myTime--;
                if (PlayActivity.this.myTime < 0) {
                    stopMyCountdown();
                    if (gameStatus != GameStatus.END)
                        gameThread.send(new UidAndData(uid, "你好").toPost(0), true, "你好");
                } else {
                    updateMyCountdown();
                }
            }
        };
        myTimer.scheduleAtFixedRate(myTimerTask, 1000, 1000);
    }

    /**
     * 更新对方倒计时的UI操作
     */
    private void updateYourCountdown() {
        runOnUiThread(() -> {
            String time = this.getString(R.string.label_countdown) + yourTime + "秒";
            this.tvCountdownYou.setText(time);
            this.pbCountdownYou.setProgress(yourTime);
        });
    }

    /**
     * 停止对方倒计时
     */
    private void stopYourCountdown() {
        runOnUiThread(() -> {
            if (this.yourTimerTask != null) this.yourTimerTask.cancel();
            if (this.yourTimer != null) this.yourTimer.cancel();
            this.pbCountdownYou.setVisibility(View.INVISIBLE);
            this.tvCountdownYou.setVisibility(View.INVISIBLE);
        });
    }

    /**
     * 更新对方倒计时的线程
     */
    private void startYourCountdown(int time) {
        runOnUiThread(() -> {
            this.myTurn = false;
            this.yourTime = time;
            this.pbCountdownYou.setProgress(time);
            this.pbCountdownYou.setVisibility(View.VISIBLE);
            this.tvCountdownYou.setVisibility(View.VISIBLE);
            this.btnSend.setEnabled(false);
        });
        yourTimer = new Timer(true);
        yourTimerTask = new TimerTask() {
            @Override
            public void run() {
                PlayActivity.this.yourTime--;
                if (PlayActivity.this.yourTime < 0) {
                    stopYourCountdown();
                } else {
                    updateYourCountdown();
                }
            }
        };
        yourTimer.scheduleAtFixedRate(yourTimerTask, 1000, 1000);
    }
}
