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

import com.bumptech.glide.Glide;
import com.example.nutrisnap.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private EditText edtName, edtEmail, edtPhone, edtWeight, edtHeight;
    private ImageView imgAvatar;
    private Uri currentAvatarUri;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private String userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        // Lắng nghe kết quả từ PhotoAdjustmentFragment
        getParentFragmentManager().setFragmentResultListener("avatar_request", this, (requestKey, bundle) -> {
            Uri resultUri = bundle.getParcelable("selected_avatar_uri");
            if (resultUri != null) {
                currentAvatarUri = resultUri;
                uploadAvatarToFirebase(currentAvatarUri);
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
                
                // Lấy weight/height linh hoạt (Double hoặc String)
                Object wObj = documentSnapshot.get("weight");
                Object hObj = documentSnapshot.get("height");
                edtWeight.setText(wObj != null ? String.valueOf(wObj) : "");
                edtHeight.setText(hObj != null ? String.valueOf(hObj) : "");
                
                String avatarUrl = documentSnapshot.getString("avatarUrl");
                if (avatarUrl != null && !avatarUrl.isEmpty() && isAdded()) {
                    Glide.with(this).load(avatarUrl).placeholder(R.drawable.img_avatar_placeholder).into(imgAvatar);
                }
                
                edtEmail.setEnabled(false);
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) {
                Toast.makeText(getContext(), R.string.load_data_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadAvatarToFirebase(Uri uri) {
        if (userId == null || uri == null) return;

        StorageReference avatarRef = storage.getReference().child("avatars/" + userId + ".jpg");
        avatarRef.putFile(uri)
            .addOnSuccessListener(taskSnapshot -> avatarRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                String downloadUrl = downloadUri.toString();
                db.collection("users").document(userId)
                    .update("avatarUrl", downloadUrl)
                    .addOnSuccessListener(aVoid -> {
                        if (isAdded()) {
                            Glide.with(this).load(downloadUrl).into(imgAvatar);
                            Toast.makeText(getContext(), "Avatar updated!", Toast.LENGTH_SHORT).show();
                        }
                    });
            }))
            .addOnFailureListener(e -> {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void saveProfile() {
        if (userId == null) return;

        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String weightStr = edtWeight.getText().toString().trim();
        String heightStr = edtHeight.getText().toString().trim();

        if (name.isEmpty()) {
            edtName.setError(getString(R.string.name_empty_error));
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", name);
        updates.put("phone", phone);
        
        try {
            if (!weightStr.isEmpty()) updates.put("weight", Double.parseDouble(weightStr));
            if (!heightStr.isEmpty()) updates.put("height", Double.parseDouble(heightStr));
        } catch (NumberFormatException e) {
            updates.put("weight", weightStr);
            updates.put("height", heightStr);
        }

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
