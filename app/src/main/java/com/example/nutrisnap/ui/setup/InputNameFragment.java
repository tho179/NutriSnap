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

public class InputNameFragment extends Fragment implements SetupProfileActivity.SetupDataInterface {

    private EditText etName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_name, container, false);
        etName = view.findViewById(R.id.et_setup_user_name);
        return view;
    }

    @Override
    public void saveData() {
        if (etName != null) {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                ((SetupProfileActivity) requireActivity()).updateUserField("fullName", name);
            }
        }
    }
}