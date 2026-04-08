package com.example.nutrisnap.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.example.nutrisnap.MainActivity;
import com.example.nutrisnap.R;
import com.example.nutrisnap.utils.LocaleHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText etUsername = findViewById(R.id.et_username); 
        EditText etPassword = findViewById(R.id.et_password);
        TextView tvGoToSignup = findViewById(R.id.tv_go_to_signup);
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        AppCompatButton btnLogin = findViewById(R.id.btn_login);

        etPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (etPassword.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - 50)) {
                        togglePasswordVisibility(etPassword);
                        return true;
                    }
                }
            }
            return false;
        });

        btnLogin.setOnClickListener(v -> {
            String usernameInput = etUsername.getText().toString().trim();
            String passwordInput = etPassword.getText().toString().trim();

            if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập Username và Mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tìm user theo username hoặc email
            db.collection("users")
                .whereEqualTo("username", usernameInput)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        db.collection("users")
                            .whereEqualTo("email", usernameInput)
                            .get()
                            .addOnSuccessListener(snapshots -> {
                                if (snapshots.isEmpty()) {
                                    Toast.makeText(this, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                                } else {
                                    verifyAndLoginFlow(snapshots.getDocuments().get(0), passwordInput);
                                }
                            });
                    } else {
                        verifyAndLoginFlow(queryDocumentSnapshots.getDocuments().get(0), passwordInput);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        tvGoToSignup.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
    }

    private void verifyAndLoginFlow(com.google.firebase.firestore.DocumentSnapshot document, String passwordInput) {
        String email = document.getString("email");
        String storedPassword = document.getString("password");

        // Thử đăng nhập trực tiếp với Firebase Auth trước (Mật khẩu chuẩn)
        mAuth.signInWithEmailAndPassword(email, passwordInput)
            .addOnSuccessListener(authResult -> {
                // Đăng nhập thành công -> Chuyển màn hình
                onLoginSuccess();
            })
            .addOnFailureListener(e -> {
                // Nếu thất bại, kiểm tra xem có phải do người dùng vừa đổi mật khẩu qua Firestore không
                if (storedPassword != null && storedPassword.equals(passwordInput)) {
                    // Mật khẩu nhập vào khớp với Firestore nhưng sai ở Auth 
                    // -> Đây là trường hợp vừa đổi mật khẩu qua luồng Quên mật khẩu.
                    // Thông thường ở đây bạn nên dùng sendPasswordResetEmail, 
                    // nhưng để fix nhanh theo logic của bạn: 
                    Toast.makeText(this, "Hệ thống đang đồng bộ mật khẩu mới...", Toast.LENGTH_SHORT).show();
                    
                    // Vì không biết mật khẩu cũ của Auth để update, ta chỉ có thể báo người dùng 
                    // sử dụng tính năng "Quên mật khẩu" chuẩn của Firebase hoặc bạn phải Reset mật khẩu qua Admin.
                    // Tuy nhiên, để tiện nhất, tôi sẽ thông báo rõ lỗi này:
                    Toast.makeText(this, "Vui lòng sử dụng mật khẩu cũ để đăng nhập lần cuối hoặc dùng tính năng Quên mật khẩu", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void onLoginSuccess() {
        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void togglePasswordVisibility(EditText editText) {
        if (isPasswordVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_password, 0, R.drawable.ic_eye_hidden, 0);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_password, 0, R.drawable.ic_eye_visible, 0);
        }
        isPasswordVisible = !isPasswordVisible;
        editText.setSelection(editText.getText().length());
    }
}
