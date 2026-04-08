package com.example.nutrisnap.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.nutrisnap.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetFragment extends Fragment {

    private final List<LinearLayout> options = new ArrayList<>();
    private final List<View> checks = new ArrayList<>();
    private final String[] goalKeys = {"Lose Weight", "Gain Muscle", "Improve Health", "Clear Skin"};
    private int selectedIndex = -1;
    private EditText edtTargetWeight;
    private FirebaseFirestore db;
    private String userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_target, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_back_target);
        edtTargetWeight = view.findViewById(R.id.edt_target_weight);
        AppCompatButton btnSave = view.findViewById(R.id.btn_save_target);

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        options.clear();
        checks.clear();

        options.add(view.findViewById(R.id.btn_target_lose_weight));
        options.add(view.findViewById(R.id.btn_target_gain_muscle));
        options.add(view.findViewById(R.id.btn_target_improve_health));
        options.add(view.findViewById(R.id.btn_target_clear_skin));

        checks.add(view.findViewById(R.id.iv_check_target_lose_weight));
        checks.add(view.findViewById(R.id.iv_check_target_gain_muscle));
        checks.add(view.findViewById(R.id.iv_check_target_improve_health));
        checks.add(view.findViewById(R.id.iv_check_target_clear_skin));

        for (int i = 0; i < options.size(); i++) {
            final int index = i;
            options.get(i).setOnClickListener(v -> selectGoal(index));
        }

        btnSave.setOnClickListener(v -> saveTarget());

        loadCurrentTarget();

        return view;
    }

    private void loadCurrentTarget() {
        if (userId == null) return;

        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Object targetWeightObj = documentSnapshot.get("targetWeight");
                    String targetWeight = (targetWeightObj != null) ? String.valueOf(targetWeightObj) : "";
                    
                    String goal = documentSnapshot.getString("goal");
                    
                    if (!targetWeight.isEmpty()) {
                        edtTargetWeight.setText(targetWeight);
                    }
                    
                    if (goal != null) {
                        for (int i = 0; i < goalKeys.length; i++) {
                            if (goal.equals(goalKeys[i])) {
                                selectGoal(i);
                                break;
                            }
                        }
                    }
                }
            })
            .addOnFailureListener(e -> {
                if (isAdded()) {
                    Toast.makeText(getContext(), getString(R.string.help), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void saveTarget() {
        if (userId == null) return;

        String weight = edtTargetWeight.getText().toString().trim();
        if (weight.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.enter_target_weight), Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedIndex == -1) {
            Toast.makeText(getContext(), getString(R.string.select_goal), Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("targetWeight", weight);
        updates.put("goal", goalKeys[selectedIndex]);

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                if (isAdded()) {
                    Toast.makeText(getContext(), getString(R.string.target_updated), Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                }
            })
            .addOnFailureListener(e -> {
                if (isAdded()) {
                    Log.e("NutriSnap_Error", "Update target failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void selectGoal(int index) {
        if (selectedIndex != -1 && selectedIndex < options.size()) {
            options.get(selectedIndex).setBackgroundResource(R.drawable.bg_goal_item_normal);
            checks.get(selectedIndex).setVisibility(View.GONE);
        }

        selectedIndex = index;
        if (index >= 0 && index < options.size()) {
            options.get(index).setBackgroundResource(R.drawable.bg_goal_item_selected);
            checks.get(index).setVisibility(View.VISIBLE);
        }
    }
}
