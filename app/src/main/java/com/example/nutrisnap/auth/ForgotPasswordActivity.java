package com.example.nutrisnap.auth;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.example.nutrisnap.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "ForgotPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        EditText etEmail = findViewById(R.id.et_email_forgot);
        AppCompatButton btnSendLink = findViewById(R.id.btn_send_otp);

        btnSendLink.setText("Send Reset Link");

        btnSendLink.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hiển thị trạng thái đang xử lý
            btnSendLink.setEnabled(false);
            btnSendLink.setText("Sending...");

            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnSendLink.setEnabled(true);
                    btnSendLink.setText("Send Reset Link");

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email sent successfully to: " + email);
                        Toast.makeText(this, "Success! Check your email (and Spam folder) for the reset link.", Toast.LENGTH_LONG).show();
                        // Chờ một chút rồi mới đóng để user kịp đọc Toast
                        btnSendLink.postDelayed(this::finish, 2000);
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "Failed to send reset email: " + error);
                        
                        // Thông báo lỗi cụ thể cho user
                        if (error.contains("no user record")) {
                            Toast.makeText(this, "This email is not registered in our system.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
        });
    }
}
