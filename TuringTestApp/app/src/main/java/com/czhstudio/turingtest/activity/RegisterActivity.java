package com.czhstudio.turingtest.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.czhstudio.turingtest.R;
import com.czhstudio.turingtest.user.LoginAndRegister;
import com.czhstudio.turingtest.user.User;
import com.czhstudio.turingtest.user.UserInfoManager;
import com.czhstudio.turingtest.utils.Error;

public class RegisterActivity extends AppCompatActivity {
    private String username;
    private String password;
    private String passwordConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etUsername = findViewById(R.id.register_username);
        EditText etPassword = findViewById(R.id.register_password);
        EditText etPasswordConfirm = findViewById(R.id.register_password_confirm);
        Button btnRegister = findViewById(R.id.register_register);
        Button btnLogin = findViewById(R.id.register_goto_login);

        // 设置字体
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/write.ttf");
        etUsername.setTypeface(face);
        etPassword.setTypeface(face);
        etPasswordConfirm.setTypeface(face);
        btnRegister.setTypeface(face);
        btnLogin.setTypeface(face);

        btnRegister.setOnClickListener(v -> {
            // 处理注册逻辑
            username = etUsername.getText().toString();
            password = etPassword.getText().toString();
            passwordConfirm = etPasswordConfirm.getText().toString();
            registerThread(username, password, passwordConfirm);
        });

        btnLogin.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_new, R.anim.slide_left_old);
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

    public  boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);

            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void registerThread(String username, String password1, String password2){
        new Thread(() -> {
            User user = LoginAndRegister.register(username, password1, password2);
            showToast(user);
        }).start();
    }

    private void showToast(User user) {
        runOnUiThread(() -> {
            int returnCode = user.getUid();
            if (returnCode > 0){
                /* 注册成功 */
                // 弹出提示
                Toast.makeText(this, R.string.suc_register, Toast.LENGTH_SHORT).show();
                // 保存用户登录信息
                UserInfoManager.setUsername(this, user.getUsername());
                UserInfoManager.setPassword(this, user.getPassword());
                UserInfoManager.setUid(this, user.getUid());
                UserInfoManager.setScore(this, user.getScore());
                // 然后执行页面跳转到登录页
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_left_new, R.anim.slide_left_old);
            } else {
                /* 注册失败 */
                Toast.makeText(this, Error.getErrMsg(returnCode), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
