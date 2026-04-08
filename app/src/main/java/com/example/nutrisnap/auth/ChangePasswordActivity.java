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
                Toast.makeText(this, R.string.enter_new_password_toast, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, R.string.passwords_do_not_match, Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(this, R.string.password_length_error, Toast.LENGTH_SHORT).show();
                return;
            }

            updatePasswordFlow(newPass);
        });
    }

    private void updatePasswordFlow(String newPass) {
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, R.string.email_info_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Lấy mật khẩu cũ từ Firestore để phục vụ việc xác thực lại với Firebase Auth
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                    String oldPassInFirestore = document.getString("password");
                    String docId = document.getId();
                    
                    syncWithFirebaseAuth(docId, newPass, oldPassInFirestore);
                } else {
                    Toast.makeText(this, R.string.account_not_found, Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi kết nối Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void syncWithFirebaseAuth(String docId, String newPass, String oldPass) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (oldPass == null || oldPass.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin mật khẩu cũ để xác thực.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user != null && user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
            // Trường hợp 1: Đã đăng nhập -> Xác thực lại (Re-authenticate) trước khi đổi
            AuthCredential credential = EmailAuthProvider.getCredential(email, oldPass);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            // Cập nhật Firestore CHỈ khi Firebase Auth đã đổi thành công
                            updateFirestorePassword(docId, newPass);
                        } else {
                            String error = updateTask.getException() != null ? updateTask.getException().getMessage() : "Lỗi đổi mật khẩu Auth.";
                            Toast.makeText(this, "Lỗi Firebase Auth: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    String error = task.getException() != null ? task.getException().getMessage() : "Mật khẩu cũ không khớp.";
                    Toast.makeText(this, "Xác thực thất bại: " + error, Toast.LENGTH_LONG).show();
                    // Log ra để debug nếu cần
                    Log.e("ChangePassword", "Re-auth failed for email: " + email + " with pass: " + oldPass);
                }
            });
        } else {
            // Trường hợp 2: Quên mật khẩu hoặc session không khớp (thường là luồng Forgot Password)
            // Đăng nhập tạm thời bằng mật khẩu cũ (từ Firestore) để lấy quyền updatePassword
            mAuth.signInWithEmailAndPassword(email, oldPass)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser newUser = authResult.getUser();
                    if (newUser != null) {
                        newUser.updatePassword(newPass).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                updateFirestorePassword(docId, newPass);
                            } else {
                                String error = task.getException() != null ? task.getException().getMessage() : "Lỗi đổi mật khẩu.";
                                Toast.makeText(this, "Lỗi Firebase Auth: " + error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Xác thực mật khẩu cũ thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("AuthSync", "Login failed: " + e.getMessage());
                });
        }
    }

    private void updateFirestorePassword(String docId, String newPass) {
        db.collection("users").document(docId)
            .update("password", newPass)
            .addOnSuccessListener(aVoid -> proceedToSuccess())
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi cập nhật Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void proceedToSuccess() {
        Toast.makeText(this, R.string.password_updated_success, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ChangePasswordActivity.this, NotificationSuccessActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupPasswordToggles(EditText etNewPassword, EditText etConfirmNewPassword) {
        etNewPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (etNewPassword.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    if (event.getRawX() >= (etNewPassword.getRight() - etNewPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - 50)) {
                        isNewPasswordVisible = !isNewPasswordVisible;
                        togglePasswordVisibility(etNewPassword, isNewPasswordVisible);
                        v.performClick();
                        return true;
                    }
                }
            }
            return false;
        });

        etConfirmNewPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (etConfirmNewPassword.getCompoundDrawables()[DRAWABLE_RIGHT] != null) {
                    if (event.getRawX() >= (etConfirmNewPassword.getRight() - etConfirmNewPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - 50)) {
                        isConfirmPasswordVisible = !isConfirmPasswordVisible;
                        togglePasswordVisibility(etConfirmNewPassword, isConfirmPasswordVisible);
                        v.performClick();
                        return true;
                    }
                }
            }
            return false;
        });
    }

    private void togglePasswordVisibility(EditText editText, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_visible, 0);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_hidden, 0);
        }
        editText.setSelection(editText.getText().length());
    }
}
