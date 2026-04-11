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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.UploadRequest;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.nutrisnap.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FieldValue;
import com.example.nutrisnap.controller.DailyController;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private EditText edtName, edtEmail, edtPhone, edtWeight, edtHeight;
    private ImageView imgAvatar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private ListenerRegistration profileListener;
    private DailyController dailyController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dailyController = new DailyController();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        // Lắng nghe kết quả từ PhotoAdjustmentFragment (Uri ảnh)
        getParentFragmentManager().setFragmentResultListener("avatar_request", this, (requestKey, bundle) -> {
            Uri resultUri = bundle.getParcelable("selected_avatar_uri");
            if (resultUri == null) {
                // Thử lấy mảng byte nếu Uri bị null do quyền truy cập
                byte[] bytes = bundle.getByteArray("selected_avatar_bytes");
                if (bytes != null) {
                    uploadAvatarToCloudinary(bytes);
                }
            } else {
                uploadAvatarToCloudinary(resultUri);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        imgAvatar = view.findViewById(R.id.img_edit_avatar);
        CardView btnChangeAvatar = view.findViewById(R.id.btn_change_avatar);
        ImageView btnBack = view.findViewById(R.id.btn_back_edit_profile);
        
        edtName = view.findViewById(R.id.edt_edit_name);
        edtEmail = view.findViewById(R.id.edt_edit_email);
        edtPhone = view.findViewById(R.id.edt_edit_phone);
        edtWeight = view.findViewById(R.id.edt_edit_weight);
        edtHeight = view.findViewById(R.id.edt_edit_height);
        
        AppCompatButton btnUpdate = view.findViewById(R.id.btn_save_profile);

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        btnChangeAvatar.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_photo_selection));
        btnUpdate.setOnClickListener(v -> saveProfile());

        startProfileListener();

        return view;
    }

    private void startProfileListener() {
        if (userId == null) return;

        profileListener = db.collection("users").document(userId)
            .addSnapshotListener((documentSnapshot, e) -> {
                if (e != null || documentSnapshot == null || !documentSnapshot.exists() || !isAdded()) return;

                if (!edtName.hasFocus()) edtName.setText(documentSnapshot.getString("username"));
                if (!edtEmail.hasFocus()) edtEmail.setText(documentSnapshot.getString("email"));
                if (!edtPhone.hasFocus()) edtPhone.setText(documentSnapshot.getString("phone"));
                
                Object wObj = documentSnapshot.get("weight");
                Object hObj = documentSnapshot.get("height");
                if (!edtWeight.hasFocus()) edtWeight.setText(wObj != null ? String.valueOf(wObj) : "");
                if (!edtHeight.hasFocus()) edtHeight.setText(hObj != null ? String.valueOf(hObj) : "");
                
                String avatarUrl = documentSnapshot.getString("avatarUrl");
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(this)
                        .load(avatarUrl)
                        .signature(new ObjectKey(avatarUrl)) 
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.img_avatar_placeholder)
                        .into(imgAvatar);
                }
                edtEmail.setEnabled(false);
            });
    }

    private void uploadAvatarToCloudinary(Object source) {
        if (userId == null || source == null) return;

        UploadRequest request;
        if (source instanceof Uri) {
            request = MediaManager.get().upload((Uri) source);
        } else if (source instanceof byte[]) {
            request = MediaManager.get().upload((byte[]) source);
        } else if (source instanceof String) {
            request = MediaManager.get().upload((String) source);
        } else if (source instanceof Integer) {
            request = MediaManager.get().upload((Integer) source);
        } else {
            return;
        }

        Toast.makeText(getContext(), "Đang tải ảnh lên Cloudinary...", Toast.LENGTH_SHORT).show();

        request.unsigned("avatar_upload_preset")
                .option("folder", "avatars")
                .option("public_id", userId) // Ghi đè ảnh cũ của chính user đó
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) { Log.d("CLOUDINARY", "Start"); }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        updateAvatarUrlInFirestore(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Lỗi Cloudinary: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { }
                }).dispatch();
    }

    private void updateAvatarUrlInFirestore(String imageUrl) {
        db.collection("users").document(userId)
                .update("avatarUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show();
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
        
        float weight = 0;
        try {
            if (!weightStr.isEmpty()) {
                weight = Float.parseFloat(weightStr);
                updates.put("weight", weight);
            }
            if (!heightStr.isEmpty()) updates.put("height", Float.parseFloat(heightStr));
        } catch (NumberFormatException e) {
            // Log error
        }

        final float finalWeight = weight;
        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                if (isAdded()) {
                    // Đồng bộ cân nặng vào daily_logs của ngày hiện tại
                    if (finalWeight > 0) {
                        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());
                        dailyController.updateWeight(userId, today, finalWeight, new DailyController.UpdateCallback() {
                            @Override
                            public void onSuccess() {
                                if (isAdded()) {
                                    Toast.makeText(getContext(), R.string.update_success, Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(requireView()).popBackStack();
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                if (isAdded()) {
                                    Toast.makeText(getContext(), R.string.update_success, Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(requireView()).popBackStack();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), R.string.update_success, Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                }
            })
            .addOnFailureListener(e -> {
                if (isAdded()) {
                    Toast.makeText(getContext(), R.string.update_data_error, Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (profileListener != null) profileListener.remove();
    }
}
