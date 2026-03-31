package com.example.nutrisnap.ui.home;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.nutrisnap.R;

public class SnacksFragment extends BaseMealFragment {

    private LinearLayout layoutFoodList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_snacks_detail, container, false);
        
        layoutFoodList = view.findViewById(R.id.layout_food_list);
        setupBaseViews(view);

        // Lắng nghe kết quả từ AnalysisFragment
        getParentFragmentManager().setFragmentResultListener("add_food_request", this, (requestKey, bundle) -> {
            String name = bundle.getString("food_name");
            int kcal = bundle.getInt("food_kcal");
            Uri imageUri = bundle.getParcelable("food_image");
            
            addNewFoodItem(name, kcal, imageUri);
        });

        return view;
    }

    private void addNewFoodItem(String name, int kcal, Uri imageUri) {
        if (layoutFoodList == null) return;

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_food_row, layoutFoodList, false);
        
        TextView tvName = itemView.findViewById(R.id.tv_food_name);
        TextView tvKcal = itemView.findViewById(R.id.tv_food_kcal);
        ImageView imgFood = itemView.findViewById(R.id.img_food_icon);
        ImageView btnDelete = itemView.findViewById(R.id.btn_delete_food);

        tvName.setText(name);
        tvKcal.setText(kcal + " kcal");
        if (imageUri != null) {
            imgFood.setImageURI(imageUri);
        }

        btnDelete.setOnClickListener(v -> {
            layoutFoodList.removeView(itemView);
        });

        layoutFoodList.addView(itemView);
        Toast.makeText(getContext(), "Added to Snacks", Toast.LENGTH_SHORT).show();
    }
}