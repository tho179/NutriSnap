package com.example.nutrisnap.ui.setup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.nutrisnap.MainActivity;
import com.example.nutrisnap.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SetupProfileActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private AppCompatButton btnContinue;
    private int currentStep = 1;
    private final int TOTAL_STEPS = 7; // Tăng lên 7 bước
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.setup_progress_bar);
        btnContinue = findViewById(R.id.btn_setup_continue);
        ImageButton btnBack = findViewById(R.id.btn_back_setup);

        if (savedInstanceState == null) {
            updateStep(1);
        }

        btnContinue.setOnClickListener(v -> handleContinue());

        btnBack.setOnClickListener(v -> {
            if (currentStep > 1) {
                updateStep(currentStep - 1);
            } else {
                finish();
            }
        });
    }

    private void handleContinue() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.setup_fragment_container);
        if (currentFragment instanceof SetupDataInterface) {
            ((SetupDataInterface) currentFragment).saveData();
        }

        if (currentStep < TOTAL_STEPS) {
            updateStep(currentStep + 1);
        } else {
            startActivity(new Intent(SetupProfileActivity.this, MainActivity.class));
            finish();
        }
    }

    public void updateUserField(String field, Object value) {
        String uid = mAuth.getUid();
        if (uid != null) {
            Map<String, Object> data = new HashMap<>();
            data.put(field, value);
            db.collection("users").document(uid)
                    .set(data, SetOptions.merge())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error saving " + field, Toast.LENGTH_SHORT).show());
        }
    }

    private void updateStep(int step) {
        currentStep = step;
        progressBar.setProgress((currentStep * 100) / TOTAL_STEPS);

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
            case 6: fragment = new TargetWeightFragment(); break; // Thêm TargetWeight ở bước 6
            case 7: fragment = new GoalFragment(); break; // Goal dời sang bước 7
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

    public interface SetupDataInterface {
        void saveData();
    }
}
