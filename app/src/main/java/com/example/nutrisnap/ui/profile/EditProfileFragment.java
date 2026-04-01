package com.example.nutrisnap.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.nutrisnap.R;

public class EditProfileFragment extends Fragment {

    private EditText edtName, edtEmail, edtPhone, edtWeight, edtHeight;
    private ImageView imgAvatar;
    private Uri currentAvatarUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lắng nghe kết quả từ PhotoAdjustmentFragment
        getParentFragmentManager().setFragmentResultListener("avatar_request", this, (requestKey, bundle) -> {
            Uri resultUri = bundle.getParcelable("selected_avatar_uri");
            if (resultUri != null) {
                currentAvatarUri = resultUri;
                if (imgAvatar != null) {
                    imgAvatar.setImageURI(currentAvatarUri);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_back_edit_profile);
        imgAvatar = view.findViewById(R.id.img_edit_avatar);
        CardView btnChangeAvatar = view.findViewById(R.id.btn_change_avatar);
        
        edtName = view.findViewById(R.id.edt_edit_name);
        edtEmail = view.findViewById(R.id.edt_edit_email);
        edtPhone = view.findViewById(R.id.edt_edit_phone);
        edtWeight = view.findViewById(R.id.edt_edit_weight);
        edtHeight = view.findViewById(R.id.edt_edit_height);
        
        AppCompatButton btnUpdate = view.findViewById(R.id.btn_save_profile);

        if (currentAvatarUri != null) {
            imgAvatar.setImageURI(currentAvatarUri);
        }

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnChangeAvatar.setOnClickListener(v -> {
            PhotoSelectionFragment selectionFragment = new PhotoSelectionFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );
            transaction.replace(R.id.fragment_container, selectionFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnUpdate.setOnClickListener(v -> {
            saveProfile();
        });

        loadCurrentProfile();

        return view;
    }

    private void loadCurrentProfile() {
        edtName.setText("PNganne");
        edtEmail.setText("pngan@gmail.com");
        edtPhone.setText("0389712462");
        edtWeight.setText("53 kg");
        edtHeight.setText("158 cm");
    }

    private void saveProfile() {
        String name = edtName.getText().toString();
        Toast.makeText(getContext(), "Profile updated for " + name, Toast.LENGTH_SHORT).show();
        requireActivity().getSupportFragmentManager().popBackStack();
    }
}