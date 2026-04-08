package com.example.nutrisnap.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.nutrisnap.R;
import com.example.nutrisnap.controller.MealController;
import com.example.nutrisnap.model.AIOnResultListener;
import com.example.nutrisnap.model.FoodItem;
import com.example.nutrisnap.model.MealCallback;
import com.example.nutrisnap.model.MealRecord;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class BaseMealFragment extends Fragment {

    protected MealController mealController;
    protected List<FoodItem> currentFoodList = new ArrayList<>();
    protected LinearLayout layoutFoodList;
    private String currentPhotoPath;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mealController = new MealController();

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (currentPhotoPath != null) {
                            analyzeImage(Uri.fromFile(new File(currentPhotoPath)));
                        }
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        analyzeImage(uri);
                    }
                }
        );
    }

    protected void setupBaseViews(View view) {
        ImageView btnBack = view.findViewById(R.id.btn_back);
        ImageView btnScan = view.findViewById(R.id.btn_scan);
        Button btnSave = view.findViewById(R.id.btn_save_meal);
        layoutFoodList = view.findViewById(R.id.layout_food_list);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        }

        if (btnScan != null) {
            btnScan.setOnClickListener(v -> showImageSourceDialog());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveMeal());
        }

        getParentFragmentManager().setFragmentResultListener("add_food_request", this, (requestKey, bundle) -> {
            String name = bundle.getString("food_name");
            int kcal = bundle.getInt("food_kcal");
            double protein = bundle.getDouble("food_protein", 0);
            double carbs = bundle.getDouble("food_carbs", 0);
            double fat = bundle.getDouble("food_fat", 0);
            Uri imageUri = bundle.getParcelable("food_image");
            
            FoodItem newItem = new FoodItem(name, 1, kcal, protein, carbs, fat);
            addFoodToUI(newItem, imageUri);
        });
    }

    protected abstract String getMealType();

    protected void addFoodToUI(FoodItem foodItem, Uri imageUri) {
        if (layoutFoodList == null) return;
        
        currentFoodList.add(foodItem);
        
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_food_row, layoutFoodList, false);
        
        TextView tvName = itemView.findViewById(R.id.tv_food_name);
        TextView tvKcal = itemView.findViewById(R.id.tv_food_kcal);
        ImageView imgFood = itemView.findViewById(R.id.img_food_icon);
        ImageView btnDelete = itemView.findViewById(R.id.btn_delete_food);

        tvName.setText(foodItem.getName());
        tvKcal.setText(String.format(Locale.getDefault(), "%d kcal", (int)foodItem.getCalories()));
        if (imageUri != null) imgFood.setImageURI(imageUri);

        itemView.setOnClickListener(v -> {
            MealRecord singleMeal = new MealRecord(getMealType(), System.currentTimeMillis(), Collections.singletonList(foodItem));
            layoutFoodList.removeView(itemView);
            currentFoodList.remove(foodItem);
            onImageAnalyzed(v, imageUri, singleMeal);
        });

        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(() -> {
                layoutFoodList.removeView(itemView);
                currentFoodList.remove(foodItem);
            });
        });

        layoutFoodList.addView(itemView);
    }

    protected void saveMeal() {
        if (currentFoodList.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng thêm ít nhất một món ăn", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        MealRecord mealRecord = new MealRecord(getMealType(), System.currentTimeMillis(), currentFoodList);

        mealController.addMeal(userId, date, mealRecord, new MealCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Lưu bữa ăn thành công!", Toast.LENGTH_SHORT).show();
                // We don't have a view here, but we can try getting it from fragment view
                if (getView() != null) {
                    Navigation.findNavController(getView()).popBackStack();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn ảnh thực đơn")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(getContext(), "Lỗi khi tạo file ảnh", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            cameraLauncher.launch(takePictureIntent);
        }
    }

    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "NutriSnap_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void analyzeImage(Uri uri) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang nhận diện món ăn...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            File imageFile = getFileFromUri(uri);
            mealController.recognizeMealFromImage(imageFile, new AIOnResultListener() {
                @Override
                public void onSuccess(MealRecord mealRecord) {
                    progressDialog.dismiss();
                    if (getView() != null) {
                        onImageAnalyzed(getView(), uri, mealRecord);
                    }
                }

                @Override
                public void onError(String message) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Lỗi nhận diện: " + message, Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Lỗi đọc file ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private File getFileFromUri(Uri uri) throws IOException {
        File tempFile = new File(requireContext().getCacheDir(), "temp_image.jpg");
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        }
        return tempFile;
    }

    protected void onImageAnalyzed(View view, Uri imageUri, MealRecord mealRecord) {
        Bundle args = new Bundle();
        args.putParcelable("image_uri", imageUri);
        if (mealRecord.getFoods() != null && !mealRecord.getFoods().isEmpty()) {
            FoodItem firstFood = mealRecord.getFoods().get(0);
            args.putString("food_name", firstFood.getName());
            args.putInt("food_kcal", (int) firstFood.getCalories());
            args.putDouble("food_protein", firstFood.getProtein());
            args.putDouble("food_carbs", firstFood.getCarbs());
            args.putDouble("food_fat", firstFood.getFat());
        }
        Navigation.findNavController(view).navigate(R.id.nav_analysis, args);
    }

    protected void showDeleteConfirmationDialog(Runnable onConfirm) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_confirmation);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView btnClose = dialog.findViewById(R.id.btn_close_dialog);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnYes = dialog.findViewById(R.id.btn_yes);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnYes.setOnClickListener(v -> {
            onConfirm.run();
            dialog.dismiss();
        });

        dialog.show();
    }
}
