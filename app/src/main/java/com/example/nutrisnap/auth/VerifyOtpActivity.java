package com.example.nutrisnap.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nutrisnap.R;

public class VerifyOtpActivity extends AppCompatActivity {

    private TextView[] otpBoxes;
    private EditText hiddenEditText;
    private final int OTP_LENGTH = 4;
    private String email;
    private String receivedOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        email = getIntent().getStringExtra("email");
        receivedOtp = getIntent().getStringExtra("otp_code");

        otpBoxes = new TextView[OTP_LENGTH];
        otpBoxes[0] = findViewById(R.id.tv_otp_1);
        otpBoxes[1] = findViewById(R.id.tv_otp_2);
        otpBoxes[2] = findViewById(R.id.tv_otp_3);
        otpBoxes[3] = findViewById(R.id.tv_otp_4);

        // Tạo một EditText ẩn để nhận sự kiện bàn phím
        setupHiddenEditText();

        // Vẫn giữ logic cho bàn phím số trên giao diện (nếu bạn muốn click chuột)
        setupNumericKeyboard();

        ImageView btnVerify = findViewById(R.id.btn_verify);
        btnVerify.setOnClickListener(v -> verifyOtp());

        ImageView btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            String currentText = hiddenEditText.getText().toString();
            if (currentText.length() > 0) {
                hiddenEditText.setText(currentText.substring(0, currentText.length() - 1));
            }
        });

        // Tự động mở bàn phím khi vào màn hình
        showKeyboard();
    }

    private void setupHiddenEditText() {
        hiddenEditText = new EditText(this);
        hiddenEditText.setLayoutParams(new ViewGroup.LayoutParams(0, 0)); // Ẩn đi
        hiddenEditText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        hiddenEditText.setFocusable(true);
        hiddenEditText.setFocusableInTouchMode(true);
        
        // Thêm vào root layout
        ViewGroup rootView = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        rootView.addView(hiddenEditText);

        hiddenEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if (text.length() > OTP_LENGTH) {
                    hiddenEditText.setText(text.substring(0, OTP_LENGTH));
                    hiddenEditText.setSelection(OTP_LENGTH);
                    return;
                }
                updateOtpUI(text);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Khi người dùng click vào các ô OTP thì hiện bàn phím
        View.OnClickListener focusListener = v -> showKeyboard();
        for (TextView box : otpBoxes) {
            box.setOnClickListener(focusListener);
        }
    }

    private void showKeyboard() {
        hiddenEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(hiddenEditText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void verifyOtp() {
        String enteredOtp = hiddenEditText.getText().toString();
        if (enteredOtp.length() == OTP_LENGTH) {
            if (enteredOtp.equals(receivedOtp)) {
                Toast.makeText(this, "Xác thực thành công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(VerifyOtpActivity.this, ResetPasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Mã OTP không chính xác", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Vui lòng nhập đủ 4 chữ số", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateOtpUI(String otp) {
        for (int i = 0; i < OTP_LENGTH; i++) {
            if (i < otp.length()) {
                otpBoxes[i].setText(String.valueOf(otp.charAt(i)));
            } else {
                otpBoxes[i].setText("");
            }
        }
    }

    private void setupNumericKeyboard() {
        View view = findViewById(R.id.tv_resend);
        if (view != null) {
            ViewParent parent = view.getParent();
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
                                    tvKey.setOnClickListener(v -> {
                                        if (hiddenEditText.getText().length() < OTP_LENGTH) {
                                            hiddenEditText.append(text);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
