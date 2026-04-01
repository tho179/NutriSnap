package com.example.nutrisnap.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nutrisnap.R;
import java.util.ArrayList;
import java.util.List;

public class PhotoSelectionFragment extends Fragment {

    private Uri imageUri;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    navigateToAdjustment(imageUri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_selection, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_back_select_photo);
        ImageView btnTakePhoto = view.findViewById(R.id.btn_take_photo);
        RecyclerView rvPhotos = view.findViewById(R.id.rv_photos);

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        rvPhotos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        List<Uri> galleryPhotos = getGalleryPhotos(requireContext());
        PhotoAdapter adapter = new PhotoAdapter(galleryPhotos);
        rvPhotos.setAdapter(adapter);

        return view;
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        imageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(intent);
    }

    private void navigateToAdjustment(Uri uri) {
        PhotoAdjustmentFragment adjustmentFragment = PhotoAdjustmentFragment.newInstance(uri);
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.fragment_container, adjustmentFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private List<Uri> getGalleryPhotos(Context context) {
        List<Uri> photoUris = new ArrayList<>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int count = 0;
            while (cursor.moveToNext() && count < 20) {
                long id = cursor.getLong(idColumn);
                Uri contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                photoUris.add(contentUri);
                count++;
            }
            cursor.close();
        }
        return photoUris;
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
        private final List<Uri> photos;

        public PhotoAdapter(List<Uri> photos) {
            this.photos = photos;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_selection, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.imgPhoto.setImageURI(photos.get(position));
            holder.itemView.setOnClickListener(v -> {
                navigateToAdjustment(photos.get(position));
            });
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgPhoto;
            ViewHolder(View itemView) {
                super(itemView);
                imgPhoto = itemView.findViewById(R.id.img_selection_item);
            }
        }
    }
}