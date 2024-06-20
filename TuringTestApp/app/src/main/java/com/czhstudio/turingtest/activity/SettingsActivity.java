package com.czhstudio.turingtest.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.czhstudio.turingtest.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnBack = findViewById(R.id.settings_back);
        TextView tvTitle = findViewById(R.id.settings_title);

        // 设置字体
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/write.ttf");
        btnBack.setTypeface(face);
        tvTitle.setTypeface(face);

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_new, R.anim.slide_right_old);
    }
}
