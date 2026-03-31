package com.example.nutrisnap.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.example.nutrisnap.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        AppCompatButton btnSendOtp = findViewById(R.id.btn_send_otp);
        btnSendOtp.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
            startActivity(intent);
        });
    }
}