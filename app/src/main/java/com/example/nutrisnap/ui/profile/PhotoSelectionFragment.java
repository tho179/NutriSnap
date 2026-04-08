package com.example.nutrisnap.ui.profile;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nutrisnap.R;
import java.util.ArrayList;
import java.util.List;

public class PhotoSelectionFragment extends Fragment {

    private Uri imageUri;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (getView() != null) {
                        navigateToAdjustment(getView(), imageUri);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_selection, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_back_select_photo);
        ImageView btnTakePhoto = view.findViewById(R.id.btn_take_photo);
        RecyclerView rvPhotos = view.findViewById(R.id.rv_photos);

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        btnTakePhoto.setOnClickListener(v -> {
            openCamera();
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

    private void navigateToAdjustment(View view, Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable("image_uri", uri);
        Navigation.findNavController(view).navigate(R.id.nav_photo_adjustment, args);
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
                navigateToAdjustment(v, photos.get(position));
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