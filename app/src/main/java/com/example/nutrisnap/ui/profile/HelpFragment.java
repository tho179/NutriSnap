package com.example.nutrisnap.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
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

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        tvUpdateProfile.setOnClickListener(v -> {
            navigateToDetail(v, getString(R.string.help_update_profile_desc));
        });

        tvChangePassword.setOnClickListener(v -> {
            navigateToDetail(v, getString(R.string.help_change_password_desc));
        });

        tvContactSupport.setOnClickListener(v -> {
            navigateToDetail(v, getString(R.string.help_contact_support_desc));
        });

        return view;
    }

    private void navigateToDetail(View view, String content) {
        Bundle bundle = new Bundle();
        bundle.putString(HelpDetailFragment.ARG_CONTENT, content);

        Navigation.findNavController(view).navigate(R.id.nav_help_detail, bundle);
    }
}