package com.example.nutrisnap.ui.home;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.nutrisnap.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvHomeDate;
    private TextView tvWaterProgress;
    private ProgressBar progressWater;
    private final Calendar calendar = Calendar.getInstance();
    
    private int currentWater = 1200;
    private final int goalWater = 2000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvHomeDate = view.findViewById(R.id.tv_home_date);
        tvWaterProgress = view.findViewById(R.id.tv_water_progress);
        progressWater = view.findViewById(R.id.progress_water);
        
        ImageView imgCalendarPicker = view.findViewById(R.id.img_calendar_picker);
        ImageView imgPrevDate = view.findViewById(R.id.img_prev_date);
        ImageView imgNextDate = view.findViewById(R.id.img_next_date);
        ImageView btnAddWater = view.findViewById(R.id.btn_add_water);
        
        RelativeLayout layoutBreakfast = view.findViewById(R.id.layout_breakfast);
        RelativeLayout layoutLunch = view.findViewById(R.id.layout_lunch);
        RelativeLayout layoutDinner = view.findViewById(R.id.layout_dinner);
        RelativeLayout layoutSnacks = view.findViewById(R.id.layout_snacks);

        // Hiển thị dữ liệu mặc định
        updateDateLabel();
        updateWaterUI();

        imgCalendarPicker.setOnClickListener(v -> showDatePicker());

        imgPrevDate.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            updateDateLabel();
        });

        imgNextDate.setOnClickListener(v -> {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            updateDateLabel();
        });

        btnAddWater.setOnClickListener(v -> showDrinkWaterDialog());

        // Chuyển sang các Fragment chi tiết
        layoutBreakfast.setOnClickListener(v -> navigateToFragment(new BreakfastFragment()));
        layoutLunch.setOnClickListener(v -> navigateToFragment(new LunchFragment()));
        layoutDinner.setOnClickListener(v -> navigateToFragment(new DinnerFragment()));
        layoutSnacks.setOnClickListener(v -> navigateToFragment(new SnacksFragment()));

        return view;
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right, 
                R.anim.slide_out_left, 
                R.anim.slide_in_left, 
                R.anim.slide_out_right
        );
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void updateWaterUI() {
        tvWaterProgress.setText(currentWater + "\n/" + goalWater + "ml");
        int progress = (currentWater * 100) / goalWater;
        progressWater.setProgress(progress);
    }

    private void showDrinkWaterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_drink_water, null);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText edtAmount = dialogView.findViewById(R.id.edt_water_amount);
        Button btnDrink = dialogView.findViewById(R.id.btn_drink_dialog);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_dialog);
        ImageView btnClose = dialogView.findViewById(R.id.btn_close_dialog);

        btnDrink.setOnClickListener(v -> {
            String amountStr = edtAmount.getText().toString();
            if (!amountStr.isEmpty()) {
                int amount = Integer.parseInt(amountStr);
                currentWater += amount;
                updateWaterUI();
                Toast.makeText(getContext(), "Added " + amount + "ml", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please enter amount", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateLabel();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateLabel() {
        String myFormat = "EEE, MMM dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        Calendar today = Calendar.getInstance();
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            tvHomeDate.setText("Today, " + new SimpleDateFormat("MMM dd", Locale.US).format(calendar.getTime()));
        } else {
            tvHomeDate.setText(sdf.format(calendar.getTime()));
        }
    }
}