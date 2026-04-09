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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.nutrisnap.R;
import com.example.nutrisnap.auth.ChangePasswordActivity;
import com.example.nutrisnap.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    private TextView tvName, tvEmail, tvWeight, tvHeight;
    private ImageView imgAvatar;
    private String userEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ View
        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvWeight = view.findViewById(R.id.tv_profile_weight);
        tvHeight = view.findViewById(R.id.tv_profile_height);
        imgAvatar = view.findViewById(R.id.img_avatar);

        CardView cardProfileInfo = view.findViewById(R.id.card_profile_info);
        CardView btnLanguage = view.findViewById(R.id.btn_menu_language);
        CardView btnHelp = view.findViewById(R.id.btn_menu_help);
        CardView btnPassword = view.findViewById(R.id.btn_menu_password);
        CardView btnTarget = view.findViewById(R.id.btn_menu_target);
        CardView btnPlan = view.findViewById(R.id.btn_menu_plan);
        Button btnLogout = view.findViewById(R.id.btn_log_out);

        // Lấy dữ liệu từ Firebase
        loadUserData();

        cardProfileInfo.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_edit_profile));
        btnLanguage.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_language));
        btnHelp.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_help));
        btnTarget.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_target));
        btnPlan.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_plan));

        btnPassword.setOnClickListener(v -> {
            if (userEmail != null && !userEmail.isEmpty()) {
                Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
                intent.putExtra("email", userEmail);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Đang tải thông tin người dùng, vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void loadUserData() {
        String userId = mAuth.getUid();
        if (userId == null) return;

        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("username");
                    String email = documentSnapshot.getString("email");
                    Double weight = documentSnapshot.getDouble("weight");
                    Double height = documentSnapshot.getDouble("height");
                    String avatarUrl = documentSnapshot.getString("avatarUrl");

                    userEmail = email;
                    tvName.setText(name != null ? name : "N/A");
                    tvEmail.setText(email != null ? email : "N/A");
                    
                    String weightVal = (weight != null) ? String.valueOf(weight) : "--";
                    String heightVal = (height != null) ? String.valueOf(height) : "--";
                    
                    tvWeight.setText(getString(R.string.weight_display, weightVal));
                    tvHeight.setText(getString(R.string.height_display, heightVal));

                    if (avatarUrl != null && !avatarUrl.isEmpty() && isAdded()) {
                        Glide.with(this).load(avatarUrl).placeholder(R.drawable.img_avatar_placeholder).into(imgAvatar);
                    }
                }
            })
            .addOnFailureListener(e -> {
                if (isAdded()) {
                    Toast.makeText(getContext(), getString(R.string.load_data_error) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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
            mAuth.signOut();
            dialog.dismiss();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        dialog.show();
    }
}
