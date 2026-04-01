package com.example.nutrisnap.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nutrisnap.R;

public class PhotoAdjustmentFragment extends Fragment {

    private static final String ARG_IMAGE_URI = "image_uri";
    private Uri imageUri;

    public static PhotoAdjustmentFragment newInstance(Uri uri) {
        PhotoAdjustmentFragment fragment = new PhotoAdjustmentFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMAGE_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

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
            imgPreview.setImageURI(imageUri);
        }

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnRetake.setOnClickListener(v -> {
            // Quay lại màn hình chọn ảnh/chụp ảnh
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        btnSaveHeader.setOnClickListener(v -> {
            // Gửi kết quả về EditProfileFragment
            Bundle result = new Bundle();
            result.putParcelable("selected_avatar_uri", imageUri);
            getParentFragmentManager().setFragmentResult("avatar_request", result);
            
            // Quay về EditProfileFragment (bỏ qua PhotoSelectionFragment)
            requireActivity().getSupportFragmentManager().popBackStack();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }
}