package com.example.nutrisnap.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nutrisnap.R;

public class VerifyOtpActivity extends AppCompatActivity {

    private TextView[] otpBoxes;
    private StringBuilder currentOtp = new StringBuilder();
    private final int OTP_LENGTH = 4;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        email = getIntent().getStringExtra("email");

        // Initialize OTP boxes
        otpBoxes = new TextView[OTP_LENGTH];
        otpBoxes[0] = findViewById(R.id.tv_otp_1);
        otpBoxes[1] = findViewById(R.id.tv_otp_2);
        otpBoxes[2] = findViewById(R.id.tv_otp_3);
        otpBoxes[3] = findViewById(R.id.tv_otp_4);

        // Setup custom keyboard
        setupNumericKeyboard();

        ImageView btnVerify = findViewById(R.id.btn_verify);
        btnVerify.setOnClickListener(v -> {
            if (currentOtp.length() == OTP_LENGTH) {
                // Giả lập verify thành công (Ví dụ mã mặc định là 1234 hoặc bất kỳ mã nào)
                Toast.makeText(this, "Mã xác thực chính xác", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(VerifyOtpActivity.this, ChangePasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Vui lòng nhập đủ 4 chữ số", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            if (currentOtp.length() > 0) {
                currentOtp.deleteCharAt(currentOtp.length() - 1);
                updateOtpUI();
            }
        });
    }

    private void setupNumericKeyboard() {
        // Tìm GridLayout chứa các phím số
        GridLayout gridLayout = null;
        // Duyệt tìm GridLayout trong layout chính (vì nó không có ID trực tiếp)
        View root = findViewById(android.R.id.content);
        if (root instanceof ViewGroup) {
            // Đây là cách đơn giản để gán sự kiện cho các TextView là số
            // Trong layout có 1 GridLayout duy nhất
            // Chúng ta có thể tìm nó bằng cách duyệt qua view hierarchy hoặc gán listener cho từng phím
        }
        
        // Cách thủ công nhưng chắc chắn: gán listener cho các phím số dựa trên style
        // Vì XML không có ID cho phím số, ta sẽ dùng phương pháp duyệt con của GridLayout
        // Tìm GridLayout (con trực tiếp của ConstraintLayout)
        View view = findViewById(R.id.tv_resend);
        if (view != null) {
            ViewParent parent = view.getParent(); // Relative/Constraint layout
            if (parent instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) parent;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    if (child instanceof GridLayout) {
                        GridLayout grid = (GridLayout) child;
                        for (int j = 0; j < grid.getChildCount(); j++) {
                            View key = grid.getChildAt(j);
                            if (key instanceof TextView) {
                                TextView tvKey = (TextView) key;
                                String text = tvKey.getText().toString();
                                if (text.length() == 1 && Character.isDigit(text.charAt(0))) {
                                    tvKey.setOnClickListener(v -> addDigit(text));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addDigit(String digit) {
        if (currentOtp.length() < OTP_LENGTH) {
            currentOtp.append(digit);
            updateOtpUI();
        }
    }

    private void updateOtpUI() {
        for (int i = 0; i < OTP_LENGTH; i++) {
            if (i < currentOtp.length()) {
                otpBoxes[i].setText(String.valueOf(currentOtp.charAt(i)));
                // Có thể thay đổi style khi đã nhập
                otpBoxes[i].setBackgroundResource(R.drawable.bg_otp_box); // Hoặc style active
            } else {
                otpBoxes[i].setText("");
                otpBoxes[i].setBackgroundResource(R.drawable.bg_otp_box);
            }
        }
    }
}
