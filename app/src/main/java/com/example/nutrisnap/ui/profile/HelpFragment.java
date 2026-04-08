package com.example.nutrisnap.ui.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.nutrisnap.R;

public class HelpFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_back_help);
        TextView tvUpdateProfile = view.findViewById(R.id.tv_help_update_profile);
        TextView tvChangePassword = view.findViewById(R.id.tv_help_change_password);
        TextView tvContactSupport = view.findViewById(R.id.tv_help_contact_support);

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        tvUpdateProfile.setOnClickListener(v -> {
            navigateToDetail(getString(R.string.help_update_profile_desc));
        });

        tvChangePassword.setOnClickListener(v -> {
            navigateToDetail(getString(R.string.help_change_password_desc));
        });

        tvContactSupport.setOnClickListener(v -> {
            navigateToDetail(getString(R.string.help_contact_support_desc));
        });

        return view;
    }

    private void navigateToDetail(String content) {
        HelpDetailFragment detailFragment = HelpDetailFragment.newInstance(content);
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.fragment_container, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}