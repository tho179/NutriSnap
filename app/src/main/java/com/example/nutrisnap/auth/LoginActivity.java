package com.example.nutrisnap.auth;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private boolean isPasswordVisible = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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
            String password = etPassword.getText().toString().trim();

            if (usernameInput.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập Username và Mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            // BƯỚC 1: Tìm Email từ Username trong Firestore
            db.collection("users")
                .whereEqualTo("username", usernameInput)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Lấy email từ document đầu tiên tìm thấy
                        String email = "";
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            email = document.getString("email");
                            break; 
                        }

                        if (email != null && !email.isEmpty()) {
                            // BƯỚC 2: Đăng nhập bằng Email tìm được
                            signInWithEmail(email, password);
                        } else {
                            Toast.makeText(this, "Không tìm thấy Email liên kết với Username này", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Nếu không tìm thấy username, có thể người dùng nhập trực tiếp email
                        if (usernameInput.contains("@")) {
                            signInWithEmail(usernameInput, password);
                        } else {
                            Toast.makeText(this, "Username không tồn tại", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi truy vấn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        });

        tvGoToSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            })
            .addOnFailureListener(e -> {
                Log.e("NutriSnap_Error", "Login Failed: " + e.getMessage());
                Toast.makeText(LoginActivity.this, "Mật khẩu không chính xác", Toast.LENGTH_LONG).show();
            });
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
