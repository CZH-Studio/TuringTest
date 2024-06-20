package com.czhstudio.turingtest.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.czhstudio.turingtest.R;
import com.czhstudio.turingtest.user.LoginAndRegister;
import com.czhstudio.turingtest.user.User;
import com.czhstudio.turingtest.user.UserInfoManager;
import com.czhstudio.turingtest.utils.Error;
import com.czhstudio.turingtest.utils.Url;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private String username;
    private String password;
    private EditText etUsername;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.etUsername = findViewById(R.id.login_username);
        this.etPassword = findViewById(R.id.login_password);
        Button btnLogin = findViewById(R.id.login_login);
        Button btnRegister = findViewById(R.id.login_goto_register);
        Button btnChangeIP = findViewById(R.id.login_change_ip);

        btnLogin.setOnClickListener(v -> {
            // 执行登录过程，登录成功则跳转到主页面
            // 获取用户输入的用户名和密码
            username = etUsername.getText().toString();
            password = etPassword.getText().toString();
            loginThread(username, password);
        });

        // 跳转到注册页
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_right_new, R.anim.slide_right_old);
        });

        // 修改IP
        btnChangeIP.setOnClickListener(v -> {
            new ChangeIPDialog(this);
        });

        // 修改字体
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/write.ttf");
        btnLogin.setTypeface(face);
        btnRegister.setTypeface(face);
        btnChangeIP.setTypeface(face);
        etUsername.setTypeface(face);
        etPassword.setTypeface(face);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.etUsername.setText(UserInfoManager.getUsername(this));
        this.etPassword.setText(UserInfoManager.getPassword(this));
        // 初始化IP
        Url.setIP(UserInfoManager.getIP(this));
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    private void loginThread (String username, String password){
        new Thread(() -> {
            User user = LoginAndRegister.login(username, password);    // 执行登录过程
            showToast(user);
        }
        ).start();
    }

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

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v instanceof EditText) {
            int[] leftTop = { 0, 0 };
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

    private void showToast(User user){
        runOnUiThread(() -> {
            int returnCode = user.getUid();
            if (returnCode > 0){
                /* 登录成功 */
                Toast.makeText(this, R.string.suc_login, Toast.LENGTH_SHORT).show();
                // 首先保存用户登录信息
                UserInfoManager.setUsername(this, user.getUsername());
                UserInfoManager.setPassword(this, user.getPassword());
                UserInfoManager.setUid(this, user.getUid());
                UserInfoManager.setScore(this, user.getScore());
                // 然后执行页面跳转
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_left_new, R.anim.slide_left_old);
            } else {
                Toast.makeText(this, Error.getErrMsg(returnCode), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class ChangeIPDialog {
        ChangeIPDialog(Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final IPView ip = new IPView(context);
            builder.setView(ip);
            builder.setTitle(R.string.string_change_ip);
            builder.setCancelable(false);
            builder.setNegativeButton(R.string.string_cancel, (dialog, which) -> dialog.dismiss());
            builder.setPositiveButton(R.string.string_confirm, (dialog, which) -> {
                dialog.dismiss();
                try {
                    int ip1 = Integer.parseInt(Objects.requireNonNull(ip.ip1.getText()).toString());
                    int ip2 = Integer.parseInt(Objects.requireNonNull(ip.ip2.getText()).toString());
                    int ip3 = Integer.parseInt(Objects.requireNonNull(ip.ip3.getText()).toString());
                    int ip4 = Integer.parseInt(Objects.requireNonNull(ip.ip4.getText()).toString());
                    if (ip1 < 0 || ip1 > 255 || ip2 < 0 || ip2 > 255 || ip3 < 0 || ip3 > 255 || ip4 < 0 || ip4 > 255){
                        Toast.makeText(context, R.string.err_invalid_ip, Toast.LENGTH_SHORT).show();
                    } else {
                        Url.setIP(new int[]{ip1, ip2, ip3, ip4});
                        UserInfoManager.setIP(LoginActivity.this, Url.IP);
                    }
                } catch (NumberFormatException e){
                    Toast.makeText(context, R.string.err_invalid_ip, Toast.LENGTH_SHORT).show();
                }
            });
            builder.create().show();
        }

        class IPView extends LinearLayout{
            public IPEditText ip1;
            public IPEditText ip2;
            public IPEditText ip3;
            public IPEditText ip4;
            public IPView(Context context) {
                super(context);
                super.setOrientation(LinearLayout.HORIZONTAL);
                int[] ips = Url.getIP();
                ip1 = new IPEditText(context, ips[0]);
                super.addView(ip1);
                super.addView(new IPDots(context));
                ip2 = new IPEditText(context, ips[1]);
                super.addView(ip2);
                super.addView(new IPDots(context));
                ip3 = new IPEditText(context, ips[2]);
                super.addView(ip3);
                super.addView(new IPDots(context));
                ip4 = new IPEditText(context, ips[3]);
                super.addView(ip4);
                super.setGravity(Gravity.CENTER);
            }
        }

        class IPEditText extends androidx.appcompat.widget.AppCompatEditText{

            public IPEditText(Context context, int value) {
                super(context);
                super.setText(String.valueOf(value));
                super.setWidth(180);
                super.setInputType(InputType.TYPE_CLASS_PHONE);
                super.setGravity(Gravity.CENTER);
            }
        }

        class IPDots extends androidx.appcompat.widget.AppCompatTextView {

            public IPDots(Context context) {
                super(context);
                super.setText(".");
                super.setWidth(10);
            }
        }
    }
}

