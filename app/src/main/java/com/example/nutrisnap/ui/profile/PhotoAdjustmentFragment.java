package com.example.nutrisnap.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.nutrisnap.R;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class PhotoAdjustmentFragment extends Fragment {

    private static final String ARG_IMAGE_URI = "image_uri";
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_adjustment, container, false);

        if (getArguments() != null) {
            imageUri = getArguments().getParcelable(ARG_IMAGE_URI);
        }

        ImageView imgPreview = view.findViewById(R.id.img_adjust_preview);
        ImageView btnBack = view.findViewById(R.id.btn_back_adjust_photo);
        View btnRetake = view.findViewById(R.id.btn_adjust_camera);
        TextView btnSaveHeader = view.findViewById(R.id.btn_save_adjust_header);

        if (imageUri != null) {
            Glide.with(this).load(imageUri).into(imgPreview);
        }

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        btnRetake.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        btnSaveHeader.setOnClickListener(v -> {
            processAndReturnImage();
        });

        return view;
    }

    private void processAndReturnImage() {
        if (imageUri == null) return;
        
        try {
            // Đọc và nén ảnh ngay tại đây để tránh lỗi quyền truy cập URI sau này
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            // Resize nếu ảnh quá lớn để tránh lỗi OutOfMemory hoặc upload quá lâu
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > 1024 || height > 1024) {
                float scale = Math.min(1024f / width, 1024f / height);
                bitmap = Bitmap.createScaledBitmap(bitmap, (int)(width * scale), (int)(height * scale), true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageData = baos.toByteArray();

            Bundle result = new Bundle();
            result.putByteArray("selected_avatar_bytes", imageData);
            getParentFragmentManager().setFragmentResult("avatar_request", result);
            
            Navigation.findNavController(requireView()).popBackStack(R.id.nav_edit_profile, false);
            
        } catch (Exception e) {
            Log.e("PhotoAdjust", "Error processing image", e);
            Toast.makeText(getContext(), "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }
}