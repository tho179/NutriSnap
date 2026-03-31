package com.example.nutrisnap.ui.profile;

import android.os.Bundle;
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
import com.example.nutrisnap.R;
import java.util.ArrayList;
import java.util.List;

public class TargetFragment extends Fragment {

    private final List<LinearLayout> options = new ArrayList<>();
    private final List<View> checks = new ArrayList<>();
    private int selectedIndex = -1;
    private EditText edtTargetWeight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_target, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_back_target);
        edtTargetWeight = view.findViewById(R.id.edt_target_weight);
        AppCompatButton btnSave = view.findViewById(R.id.btn_save_target);

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

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

        btnSave.setOnClickListener(v -> {
            String weight = edtTargetWeight.getText().toString().trim();
            if (weight.isEmpty()) {
                Toast.makeText(getContext(), "Please enter target weight", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedIndex == -1) {
                Toast.makeText(getContext(), "Please select a target", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Toast.makeText(getContext(), "Target updated: " + weight + " kg", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Default selection for demonstration (as in your image)
        selectGoal(1); // Gain Muscle

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
}