package com.example.nutrisnap.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.example.nutrisnap.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        EditText etNewPassword = findViewById(R.id.et_new_password);
        EditText etConfirmNewPassword = findViewById(R.id.et_confirm_new_password);
        AppCompatButton btnSave = findViewById(R.id.btn_save_password);

        // Toggle New Password Visibility
        etNewPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etNewPassword.getRight() - etNewPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - 50)) {
                    isNewPasswordVisible = !isNewPasswordVisible;
                    togglePasswordVisibility(etNewPassword, isNewPasswordVisible);
                    return true;
                }
            }
            return false;
        });

        // Toggle Confirm Password Visibility
        etConfirmNewPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etConfirmNewPassword.getRight() - etConfirmNewPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - 50)) {
                    isConfirmPasswordVisible = !isConfirmPasswordVisible;
                    togglePasswordVisibility(etConfirmNewPassword, isConfirmPasswordVisible);
                    return true;
                }
            }
            return false;
        });

        btnSave.setOnClickListener(v -> {
            // Chuyển sang màn hình NotificationSuccess thay vì về Login trực tiếp
            Intent intent = new Intent(ChangePasswordActivity.this, NotificationSuccessActivity.class);
            startActivity(intent);
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