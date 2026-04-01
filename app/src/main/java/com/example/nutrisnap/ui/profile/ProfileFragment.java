package com.example.nutrisnap.ui.profile;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.nutrisnap.R;
import com.example.nutrisnap.auth.ChangePasswordActivity;
import com.example.nutrisnap.auth.LoginActivity;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        CardView cardProfileInfo = view.findViewById(R.id.card_profile_info);
        CardView btnLanguage = view.findViewById(R.id.btn_menu_language);
        CardView btnHelp = view.findViewById(R.id.btn_menu_help);
        CardView btnPassword = view.findViewById(R.id.btn_menu_password);
        CardView btnTarget = view.findViewById(R.id.btn_menu_target);
        CardView btnPlan = view.findViewById(R.id.btn_menu_plan);
        Button btnLogout = view.findViewById(R.id.btn_log_out);

        cardProfileInfo.setOnClickListener(v -> navigateToFragment(new EditProfileFragment()));

        btnLanguage.setOnClickListener(v -> navigateToFragment(new LanguageFragment()));
        btnHelp.setOnClickListener(v -> navigateToFragment(new HelpFragment()));
        btnTarget.setOnClickListener(v -> navigateToFragment(new TargetFragment()));
        btnPlan.setOnClickListener(v -> navigateToFragment(new PlanFragment()));

        btnPassword.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });

        return view;
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_logout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnCancel = dialog.findViewById(R.id.btn_cancel_logout);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm_logout);
        ImageView btnClose = dialog.findViewById(R.id.btn_close_logout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        dialog.show();
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}