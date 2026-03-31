package com.example.nutrisnap.ui.setup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.nutrisnap.MainActivity;
import com.example.nutrisnap.R;

public class SetupProfileActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private AppCompatButton btnContinue;
    private int currentStep = 1;
    private final int TOTAL_STEPS = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        progressBar = findViewById(R.id.setup_progress_bar);
        btnContinue = findViewById(R.id.btn_setup_continue);
        ImageButton btnBack = findViewById(R.id.btn_back_setup);

        if (savedInstanceState == null) {
            updateStep(1);
        }

        btnContinue.setOnClickListener(v -> {
            if (currentStep < TOTAL_STEPS) {
                updateStep(currentStep + 1);
            } else {
                startActivity(new Intent(SetupProfileActivity.this, MainActivity.class));
                finish();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (currentStep > 1) {
                updateStep(currentStep - 1);
            } else {
                finish();
            }
        });
    }

    private void updateStep(int step) {
        currentStep = step;
        progressBar.setProgress((currentStep * 100) / TOTAL_STEPS);

        // Ẩn nút Continue ở Activity khi đến GoalFragment (Step 6)
        // Vì GoalFragment đã có nút Continue riêng hoặc sẽ xử lý logic chuyển màn hình
        if (currentStep == TOTAL_STEPS) {
            btnContinue.setVisibility(View.GONE);
        } else {
            btnContinue.setVisibility(View.VISIBLE);
        }

        Fragment fragment;
        switch (step) {
            case 1: fragment = new InputNameFragment(); break;
            case 2: fragment = new GenderFragment(); break;
            case 3: fragment = new AgeFragment(); break;
            case 4: fragment = new HeightFragment(); break;
            case 5: fragment = new WeightFragment(); break;
            case 6: fragment = new GoalFragment(); break;
            default: fragment = new InputNameFragment();
        }
        loadFragment(fragment);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        transaction.replace(R.id.setup_fragment_container, fragment);
        transaction.commit();
    }
}