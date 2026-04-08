package com.example.nutrisnap.ui.setup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nutrisnap.R;

public class AgeFragment extends Fragment implements SetupProfileActivity.SetupDataInterface {

    private EditText etAge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_age, container, false);
        etAge = view.findViewById(R.id.et_setup_age);
        return view;
    }

    @Override
    public void saveData() {
        if (etAge != null) {
            String ageStr = etAge.getText().toString().trim();
            if (!ageStr.isEmpty()) {
                try {
                    int age = Integer.parseInt(ageStr);
                    ((SetupProfileActivity) requireActivity()).updateUserField("age", age);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
    }
}