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
import java.util.ArrayList;
import java.util.List;

public class GoalFragment extends Fragment {

    private List<LinearLayout> options = new ArrayList<>();
    private List<View> checks = new ArrayList<>();
    private int selectedIndex = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal, container, false);

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
                // Chuyển đến MainActivity (Home)
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            } else {
                Toast.makeText(getContext(), "Please select a goal first", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void selectGoal(int index) {
        // Reset previous selection
        if (selectedIndex != -1) {
            options.get(selectedIndex).setBackgroundResource(R.drawable.bg_goal_item_normal);
            checks.get(selectedIndex).setVisibility(View.GONE);
        }

        // Set new selection
        selectedIndex = index;
        options.get(index).setBackgroundResource(R.drawable.bg_goal_item_selected);
        checks.get(index).setVisibility(View.VISIBLE);
    }
}