package com.example.nutrisnap.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nutrisnap.R;

public class VerifyOtpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        ImageView btnVerify = findViewById(R.id.btn_verify);
        btnVerify.setOnClickListener(v -> {
            // Sau khi verify thành công (giả lập), chuyển sang ChangePassword
            Intent intent = new Intent(VerifyOtpActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
        
        ImageView btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            // Logic xóa số OTP
        });
    }
}