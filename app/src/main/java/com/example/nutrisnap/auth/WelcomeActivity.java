package com.example.nutrisnap.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.example.nutrisnap.R;
import com.example.nutrisnap.ui.setup.SetupProfileActivity;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        AppCompatButton btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(v -> {
            // Thay đổi từ MainActivity sang SetupProfileActivity
            Intent intent = new Intent(WelcomeActivity.this, SetupProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }
}