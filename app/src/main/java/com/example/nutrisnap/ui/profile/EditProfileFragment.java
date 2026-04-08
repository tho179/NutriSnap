package com.example.nutrisnap.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import androidx.navigation.Navigation;
import com.example.nutrisnap.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private EditText edtName, edtEmail, edtPhone, edtWeight, edtHeight;
    private ImageView imgAvatar;
    private Uri currentAvatarUri;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

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

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        btnChangeAvatar.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_photo_selection);
        });

        btnUpdate.setOnClickListener(v -> {
            saveProfile();
        });

        loadCurrentProfile();

        return view;
    }

    private void loadCurrentProfile() {
        if (userId == null) return;

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                edtName.setText(documentSnapshot.getString("username"));
                edtEmail.setText(documentSnapshot.getString("email"));
                edtPhone.setText(documentSnapshot.getString("phone"));
                edtWeight.setText(documentSnapshot.getString("weight"));
                edtHeight.setText(documentSnapshot.getString("height"));
                
                // Email không nên cho phép sửa vì là định danh tài khoản
                edtEmail.setEnabled(false);
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) {
                Toast.makeText(getContext(), R.string.load_data_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        if (userId == null) return;

        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String weight = edtWeight.getText().toString().trim();
        String height = edtHeight.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError(getString(R.string.name_empty_error));
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", name);
        updates.put("phone", phone);
        updates.put("weight", weight);
        updates.put("height", height);

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                if (isAdded()) {
                    Toast.makeText(getContext(), R.string.update_success, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(getView()).popBackStack();
                }
            })
            .addOnFailureListener(e -> {
                Log.e("NutriSnap_Error", "Update failed: " + e.getMessage());
                if (isAdded()) {
                    Toast.makeText(getContext(), R.string.update_data_error, Toast.LENGTH_SHORT).show();
                }
            });
    }
}
