package com.example.nutrisnap.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.example.nutrisnap.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ResetPasswordActivity extends AppCompatActivity {

    private String email;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean isVisible1 = false, isVisible2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password); // Dùng chung layout với ChangePassword

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        email = getIntent().getStringExtra("email");

        ImageView btnBack = findViewById(R.id.btn_back_change_password);
        EditText etNewPass = findViewById(R.id.et_new_password);
        EditText etConfirmPass = findViewById(R.id.et_confirm_new_password);
        AppCompatButton btnSave = findViewById(R.id.btn_save_password);

        btnBack.setOnClickListener(v -> finish());
        setupToggles(etNewPass, etConfirmPass);

        btnSave.setOnClickListener(v -> {
            String newPass = etNewPass.getText().toString().trim();
            String confirmPass = etConfirmPass.getText().toString().trim();

            if (newPass.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            resetPasswordLogic(newPass);
        });
    }

    private void resetPasswordLogic(String newPass) {
        // Luồng quên mật khẩu: 
        // 1. Lấy mật khẩu cũ từ Firestore để login (do Firebase Auth cần login mới cho đổi pass)
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    String oldPass = queryDocumentSnapshots.getDocuments().get(0).getString("password");
                    String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    
                    mAuth.signInWithEmailAndPassword(email, oldPass != null ? oldPass : "")
                        .addOnSuccessListener(authResult -> {
                            FirebaseUser user = authResult.getUser();
                            if (user != null) {
                                user.updatePassword(newPass).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        updateFirestore(docId, newPass);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Lỗi xác thực hệ thống", Toast.LENGTH_SHORT).show());
                }
            });
    }

    private void updateFirestore(String docId, String newPass) {
        db.collection("users").document(docId).update("password", newPass)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, NotificationSuccessActivity.class));
                finish();
            });
    }

    private void setupToggles(EditText e1, EditText e2) {
        e1.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && event.getRawX() >= (e1.getRight() - 100)) {
                isVisible1 = !isVisible1;
                toggle(e1, isVisible1);
                return true;
            }
            return false;
        });
        e2.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && event.getRawX() >= (e2.getRight() - 100)) {
                isVisible2 = !isVisible2;
                toggle(e2, isVisible2);
                return true;
            }
            return false;
        });
    }

    private void toggle(EditText e, boolean visible) {
        e.setInputType(visible ? InputType.TYPE_CLASS_TEXT : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
        e.setCompoundDrawablesWithIntrinsicBounds(0, 0, visible ? R.drawable.ic_eye_visible : R.drawable.ic_eye_hidden, 0);
        e.setSelection(e.getText().length());
    }
}
