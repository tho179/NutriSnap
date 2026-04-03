package com.example.nutrisnap.ui.setup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import com.example.nutrisnap.MainActivity;
import com.example.nutrisnap.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalFragment extends Fragment implements SetupProfileActivity.SetupDataInterface {

    private List<LinearLayout> options = new ArrayList<>();
    private List<View> checks = new ArrayList<>();
    private int selectedIndex = -1;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        options.add(view.findViewById(R.id.btn_lose_weight));
        options.add(view.findViewById(R.id.btn_gain_muscle));
        options.add(view.findViewById(R.id.btn_improve_health));
        options.add(view.findViewById(R.id.btn_clear_skin));

        checks.add(view.findViewById(R.id.iv_check_lose_weight));
        checks.add(view.findViewById(R.id.iv_check_gain_muscle));
        checks.add(view.findViewById(R.id.iv_check_improve_health));
        checks.add(view.findViewById(R.id.iv_check_clear_skin));

        for (int i = 0; i < options.size(); i++) {
            final int index = i;
            options.get(i).setOnClickListener(v -> selectGoal(index));
        }

        AppCompatButton btnContinue = view.findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(v -> {
            if (selectedIndex != -1) {
                calculateAndSaveTargets();
            } else {
                Toast.makeText(getContext(), "Please select a goal first", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void selectGoal(int index) {
        if (selectedIndex != -1) {
            options.get(selectedIndex).setBackgroundResource(R.drawable.bg_goal_item_normal);
            checks.get(selectedIndex).setVisibility(View.GONE);
        }
        selectedIndex = index;
        options.get(index).setBackgroundResource(R.drawable.bg_goal_item_selected);
        checks.get(index).setVisibility(View.VISIBLE);
    }

    private void calculateAndSaveTargets() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        // Lấy targetWeight đã lưu từ bước trước
        db.collection("users").document(uid).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Double targetWeight = documentSnapshot.getDouble("targetWeight");
                    if (targetWeight == null) targetWeight = 60.0; // Fallback

                    // Áp dụng công thức
                    double targetCalories = 22 * targetWeight;
                    double targetProtein = 1.8 * targetWeight;
                    double targetCarbs = (0.45 * targetCalories) / 4; // 1g carb = 4 kcal
                    double targetFat = (0.25 * targetCalories) / 9;   // 1g fat = 9 kcal

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("targetCalories", targetCalories);
                    updates.put("targetProtein", targetProtein);
                    updates.put("targetCarbs", targetCarbs);
                    updates.put("targetFat", targetFat);
                    
                    String goalText = "";
                    switch(selectedIndex) {
                        case 0: goalText = "Lose weight"; break;
                        case 1: goalText = "Gain muscle"; break;
                        case 2: goalText = "Improve health"; break;
                        case 3: goalText = "Clear skin"; break;
                    }
                    updates.put("goal", goalText);

                    db.collection("users").document(uid).set(updates, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                            if (getActivity() != null) getActivity().finish();
                        });
                }
            });
    }

    @Override
    public void saveData() {
        // Data is saved in calculateAndSaveTargets() when clicking continue
    }
}
