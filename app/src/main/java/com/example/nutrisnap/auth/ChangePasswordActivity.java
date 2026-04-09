package com.example.nutrisnap.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.example.nutrisnap.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ChangePasswordActivity extends AppCompatActivity {

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private String email;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        email = getIntent().getStringExtra("email");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (email == null && currentUser != null) {
            email = currentUser.getEmail();
        }

        ImageView btnBack = findViewById(R.id.btn_back_change_password);
        EditText etNewPassword = findViewById(R.id.et_new_password);
        EditText etConfirmNewPassword = findViewById(R.id.et_confirm_new_password);
        AppCompatButton btnSave = findViewById(R.id.btn_save_password);

        btnBack.setOnClickListener(v -> finish());
        setupPasswordToggles(etNewPassword, etConfirmNewPassword);

        btnSave.setOnClickListener(v -> {
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmNewPassword.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            performChangePassword(newPass);
        });
    }

    private void performChangePassword(String newPass) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thử cập nhật mật khẩu trực tiếp
        user.updatePassword(newPass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Thành công -> Cập nhật Database
                updateFirestorePassword(newPass);
            } else {
                if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                    // Nếu Firebase yêu cầu xác thực lại (do đăng nhập đã lâu)
                    handleReauthentication(newPass);
                } else {
                    Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void handleReauthentication(String newPass) {
        // Lấy mật khẩu cũ đang lưu trong Database để tự động xác thực cho user
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                    String oldPass = doc.getString("password");
                    String docId = doc.getId();

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null && oldPass != null) {
                        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
                        user.reauthenticate(credential).addOnCompleteListener(reAuthTask -> {
                            if (reAuthTask.isSuccessful()) {
                                // Xác thực xong, thử đổi lại lần nữa
                                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        updateFirestorePassword(newPass);
                                    }
                                });
                            } else {
                                // Nếu mật khẩu trong Database cũng sai so với Auth
                                Toast.makeText(this, "Để bảo mật, vui lòng Đăng xuất và Đăng nhập lại để đổi mật khẩu.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
    }

    private void updateFirestorePassword(String newPass) {
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    db.collection("users").document(docId).update("password", newPass)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                }
            });
    }

    private void setupPasswordToggles(EditText etNewPassword, EditText etConfirmNewPassword) {
        etNewPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && event.getRawX() >= (etNewPassword.getRight() - 100)) {
                isNewPasswordVisible = !isNewPasswordVisible;
                toggleVisibility(etNewPassword, isNewPasswordVisible);
                return true;
            }
            return false;
        });

        etConfirmNewPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && event.getRawX() >= (etConfirmNewPassword.getRight() - 100)) {
                isConfirmPasswordVisible = !isConfirmPasswordVisible;
                toggleVisibility(etConfirmNewPassword, isConfirmPasswordVisible);
                return true;
            }
            return false;
        });
    }

    private void toggleVisibility(EditText editText, boolean isVisible) {
        editText.setInputType(isVisible ? InputType.TYPE_CLASS_TEXT : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, isVisible ? R.drawable.ic_eye_visible : R.drawable.ic_eye_hidden, 0);
        editText.setSelection(editText.getText().length());
    }
}
