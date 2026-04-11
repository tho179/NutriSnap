package com.example.nutrisnap.ui.report;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.nutrisnap.R;
import com.example.nutrisnap.controller.DailyController;
import com.example.nutrisnap.model.DailySummaryData;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InsightsFragment extends Fragment implements OnChartValueSelectedListener {

    private BarChart barChart;
    private PieChart pieChart;
    private LineChart lineChart;
    private int selectedBarIndex = 0;
    
    private TextView tvWeekly, tvMonthly, tvYearly;
    private TextView tvInsightsRange;
    private ImageView btnPrev, btnNext;
    private Button btnUpdateWeight;
    
    private Calendar currentCalendar = Calendar.getInstance();
    private String currentMode = "weekly";
    private float userWeight = 60.0f; // Mặc định nếu không có dữ liệu

    private DailyController dailyController;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<BarEntry> currentBarEntries = new ArrayList<>();
    private float currentTargetKcal = 2500f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insights, container, false);

        dailyController = new DailyController();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        barChart = view.findViewById(R.id.bar_chart_calories);
        pieChart = view.findViewById(R.id.pie_chart_nutrients);
        lineChart = view.findViewById(R.id.line_chart_weight);
        
        tvWeekly = view.findViewById(R.id.tv_weekly);
        tvMonthly = view.findViewById(R.id.tv_monthly);
        tvYearly = view.findViewById(R.id.tv_yearly);
        tvInsightsRange = view.findViewById(R.id.tv_insights_range);
        btnPrev = view.findViewById(R.id.btn_prev_week);
        btnNext = view.findViewById(R.id.btn_next_week);
        btnUpdateWeight = view.findViewById(R.id.btn_update_weight);

        setupTabClickListeners();
        setupNavigationListeners();
        
        btnUpdateWeight.setOnClickListener(v -> {
            showUpdateWeightDialog();
        });

        // Load cân nặng hiện tại từ User Profile trước khi hiển thị chart
        loadCurrentUserWeight();

        return view;
    }

    private void loadCurrentUserWeight() {
        String uid = mAuth.getUid();
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("weight")) {
                        Double w = doc.getDouble("weight");
                        if (w == null) {
                            String wStr = doc.getString("weight");
                            try {
                                userWeight = (wStr != null) ? Float.parseFloat(wStr) : 60.0f;
                            } catch (Exception e) {
                                userWeight = 60.0f;
                            }
                        } else {
                            userWeight = w.floatValue();
                        }
                    }
                    updateViewMode("weekly");
                })
                .addOnFailureListener(e -> updateViewMode("weekly"));
        } else {
            updateViewMode("weekly");
        }
    }

    private void showUpdateWeightDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_weight);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvDate = dialog.findViewById(R.id.tv_dialog_date);
        EditText edtWeight = dialog.findViewById(R.id.edt_weight_input);
        ImageView btnClose = dialog.findViewById(R.id.btn_close_dialog);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel_dialog);
        Button btnUpdateConfirm = dialog.findViewById(R.id.btn_update_confirm);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM dd", Locale.US);
        Calendar now = Calendar.getInstance();
        tvDate.setText("Today, " + sdf.format(now.getTime()));
        edtWeight.setText(String.valueOf(userWeight));

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnUpdateConfirm.setOnClickListener(v -> {
            String weightStr = edtWeight.getText().toString();
            if (!weightStr.isEmpty()) {
                try {
                    float newWeight = Float.parseFloat(weightStr);
                    String userId = mAuth.getUid();
                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now.getTime());
                    
                    if (userId != null) {
                        dailyController.updateWeight(userId, date, newWeight, new DailyController.UpdateCallback() {
                            @Override
                            public void onSuccess() {
                                userWeight = newWeight;
                                Toast.makeText(getContext(), "Weight updated successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                updateRangeTextAndCharts();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid weight", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void setupTabClickListeners() {
        tvWeekly.setOnClickListener(v -> updateViewMode("weekly"));
        tvMonthly.setOnClickListener(v -> updateViewMode("monthly"));
        tvYearly.setOnClickListener(v -> updateViewMode("yearly"));
    }

    private void setupNavigationListeners() {
        btnPrev.setOnClickListener(v -> navigate(-1));
        btnNext.setOnClickListener(v -> navigate(1));
    }

    private void navigate(int delta) {
        if (currentMode.equals("weekly")) {
            currentCalendar.add(Calendar.WEEK_OF_YEAR, delta);
        } else if (currentMode.equals("monthly")) {
            currentCalendar.add(Calendar.MONTH, delta);
        } else {
            currentCalendar.add(Calendar.YEAR, delta);
        }
        updateRangeTextAndCharts();
    }

    private void updateViewMode(String mode) {
        currentMode = mode;
        tvWeekly.setBackground(null);
        tvWeekly.setTextColor(Color.parseColor("#333333"));
        tvMonthly.setBackground(null);
        tvMonthly.setTextColor(Color.parseColor("#333333"));
        tvYearly.setBackground(null);
        tvYearly.setTextColor(Color.parseColor("#333333"));

        TextView selectedTab = mode.equals("weekly") ? tvWeekly : (mode.equals("monthly") ? tvMonthly : tvYearly);
        selectedTab.setBackgroundResource(R.drawable.bg_button_green);
        selectedTab.setTextColor(Color.WHITE);

        updateRangeTextAndCharts();
    }

    private void updateRangeTextAndCharts() {
        String userId = mAuth.getUid();
        if (userId == null) return;

        SimpleDateFormat dbSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        List<String> dateKeys = new ArrayList<>();
        String[] labels = new String[7];

        if (currentMode.equals("weekly")) {
            Calendar start = (Calendar) currentCalendar.clone();
            start.setFirstDayOfWeek(Calendar.MONDAY);
            start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_YEAR, 6);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.US);
            tvInsightsRange.setText(sdf.format(start.getTime()) + " - " + sdf.format(end.getTime()) + ", " + currentCalendar.get(Calendar.YEAR));
            
            Calendar labelCal = (Calendar) start.clone();
            for (int i = 0; i < 7; i++) {
                dateKeys.add(dbSdf.format(labelCal.getTime()));
                labels[i] = String.valueOf(labelCal.get(Calendar.DAY_OF_MONTH));
                labelCal.add(Calendar.DAY_OF_YEAR, 1);
            }
        } else if (currentMode.equals("monthly")) {
            Calendar end = (Calendar) currentCalendar.clone();
            Calendar start = (Calendar) end.clone();
            start.add(Calendar.MONTH, -6);
            
            SimpleDateFormat sdfRange = new SimpleDateFormat("MMM", Locale.US);
            tvInsightsRange.setText(sdfRange.format(start.getTime()) + " - " + sdfRange.format(end.getTime()) + ", " + end.get(Calendar.YEAR));
            
            Calendar labelCal = (Calendar) start.clone();
            for (int i = 0; i < 7; i++) {
                dateKeys.add(dbSdf.format(labelCal.getTime()));
                labels[i] = sdfRange.format(labelCal.getTime());
                labelCal.add(Calendar.MONTH, 1);
            }
        } else {
            int endYear = currentCalendar.get(Calendar.YEAR);
            int startYear = endYear - 6;
            tvInsightsRange.setText(startYear + " - " + endYear);
            
            for (int i = 0; i < 7; i++) {
                int year = startYear + i;
                labels[i] = String.valueOf(year);
                dateKeys.add(year + "-01-01");
            }
        }

        // Bước 1: Tìm cân nặng gần nhất TRƯỚC dải thời gian hiển thị
        dailyController.fetchLastKnownWeightBefore(userId, dateKeys.get(0), new DailyController.LastWeightCallback() {
            @Override
            public void onSuccess(float initialWeight) {
                // Bước 2: Fetch dữ liệu trong dải thời gian
                dailyController.fetchRangeSummary(userId, dateKeys, new DailyController.DailyListCallback() {
                    @Override
                    public void onSuccess(Map<String, DailySummaryData> dataMap) {
                        if (!isAdded()) return;
                        
                        currentBarEntries.clear();
                        List<Entry> weightEntries = new ArrayList<>();
                        
                        float totalProtein = 0, totalCarbs = 0, totalFat = 0;
                        
                        // Sử dụng initialWeight làm mốc bắt đầu nếu ngày đầu tiên trống
                        float lastKnownWeight = (initialWeight > 0) ? initialWeight : userWeight;
                        currentTargetKcal = 2500f;

                        for (int i = 0; i < dateKeys.size(); i++) {
                            DailySummaryData d = dataMap.get(dateKeys.get(i));
                            
                            if (d != null) {
                                currentTargetKcal = d.targetCalories;
                                float cal = d.totalCaloriesIn;
                                currentBarEntries.add(new BarEntry(i, cal));
                                
                                if (d.totalCaloriesIn > 0) {
                                    totalProtein += d.proteinEaten;
                                    totalCarbs += d.carbsEaten;
                                    totalFat += d.fatEaten;
                                }

                                // Cập nhật lastKnownWeight CHỈ khi ngày đó có dữ liệu thực tế
                                if (d.weight > 0) {
                                    lastKnownWeight = d.weight;
                                }
                            } else {
                                currentBarEntries.add(new BarEntry(i, 0));
                            }
                            // Gán giá trị cân nặng (thực tế hoặc lan truyền từ quá khứ) cho ngày i
                            weightEntries.add(new Entry(i, lastKnownWeight));
                        }
                        
                        updateBarChart(currentBarEntries, labels, currentTargetKcal);
                        updatePieChart(totalProtein, totalCarbs, totalFat);
                        updateWeightChart(weightEntries, labels);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (isAdded()) Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Nếu lỗi truy vấn lịch sử, vẫn tiếp tục fetch dải hiện tại
                onSuccess(0f);
            }
        });
    }

    private void configureBarChart(String[] labels, float targetKcal) {
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.setFitBars(true);
        barChart.setExtraOffsets(0, 40, 0, 0);
        barChart.setOnChartValueSelectedListener(this);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextColor(Color.parseColor("#9E9E9E"));
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        
        float maxVal = 0;
        if (barChart.getData() != null) {
            maxVal = barChart.getData().getYMax();
        }
        leftAxis.setAxisMaximum(Math.max(targetKcal + 500f, maxVal + 500f));
        
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F2F2F2"));
        leftAxis.setDrawAxisLine(false);
        
        leftAxis.removeAllLimitLines();
        LimitLine limitLine = new LimitLine(targetKcal, "");
        limitLine.setLineColor(Color.parseColor("#66BB6A"));
        limitLine.setLineWidth(1.5f);
        limitLine.enableDashedLine(10f, 10f, 0f);
        leftAxis.addLimitLine(limitLine);
    }

    private void updateBarChart(List<BarEntry> entries, String[] labels, float targetKcal) {
        BarDataSet dataSet = new BarDataSet(entries, "Calories");
        selectedBarIndex = entries.size() - 1; 
        updateBarColors(dataSet, entries, targetKcal);
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return (int) barEntry.getX() == selectedBarIndex ? String.valueOf((int) barEntry.getY()) : "";
            }
        });
        barChart.setData(new BarData(dataSet));
        barChart.getData().setBarWidth(0.65f);
        configureBarChart(labels, targetKcal);
        barChart.animateY(800);
        barChart.invalidate();
    }

    private void updatePieChart(float protein, float carbs, float fat) {
        List<PieEntry> entries = new ArrayList<>();
        if (protein == 0 && carbs == 0 && fat == 0) {
            entries.add(new PieEntry(1f, "No Data"));
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColor(Color.LTGRAY);
            dataSet.setDrawValues(false);
            pieChart.setData(new PieData(dataSet));
        } else {
            entries.add(new PieEntry(protein, "Protein"));
            entries.add(new PieEntry(carbs, "Carbs"));
            entries.add(new PieEntry(fat, "Fat"));
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(new int[]{Color.parseColor("#FF9800"), Color.parseColor("#FF5252"), Color.parseColor("#03A9F4")});
            dataSet.setSliceSpace(3f);
            dataSet.setDrawValues(false);
            pieChart.setData(new PieData(dataSet));
        }
        pieChart.setHoleRadius(65f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.animateXY(800, 800);
        pieChart.invalidate();
    }

    private void configureLineChart(String[] labels) {
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTextColor(Color.parseColor("#9E9E9E"));
        
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(labels.length - 0.5f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F2F2F2"));
        leftAxis.setDrawAxisLine(false);
        leftAxis.setAxisMinimum(0f); 

        lineChart.setExtraOffsets(10, 10, 10, 10);
    }

    private void updateWeightChart(List<Entry> entries, String[] labels) {
        LineDataSet dataSet = new LineDataSet(entries, "Weight");
        int purple = Color.parseColor("#9575CD");
        dataSet.setColor(purple);
        dataSet.setCircleColor(purple);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setCircleRadius(5f);
        dataSet.setLineWidth(2f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return String.format("%.1f", entry.getY());
            }
        });
        
        lineChart.setData(new LineData(dataSet));
        configureLineChart(labels);
        
        float min = userWeight, max = userWeight;
        for (Entry e : entries) {
            if (e.getY() < min && e.getY() > 0) min = e.getY();
            if (e.getY() > max) max = e.getY();
        }
        lineChart.getAxisLeft().setAxisMinimum(Math.max(0, min - 5f));
        lineChart.getAxisLeft().setAxisMaximum(max + 5f);
        
        lineChart.animateX(800);
        lineChart.invalidate();
    }

    private void updateBarColors(BarDataSet dataSet, List<BarEntry> entries, float targetKcal) {
        int selectedColor = Color.parseColor("#66BB6A"); // Xanh lá
        int defaultColor = Color.parseColor("#C8E6C9");  // Xanh lá nhạt
        int exceededColor = Color.parseColor("#FF5252"); // Đỏ
        int exceededSelectedColor = Color.parseColor("#D32F2F"); // Đỏ đậm
        
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            boolean isExceeded = entries.get(i).getY() > targetKcal;
            if (i == selectedBarIndex) {
                colors.add(isExceeded ? exceededSelectedColor : selectedColor);
            } else {
                colors.add(isExceeded ? exceededColor : defaultColor);
            }
        }
        dataSet.setColors(colors);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        selectedBarIndex = (int) e.getX();
        if (barChart.getData() != null && barChart.getData().getDataSetCount() > 0) {
            BarDataSet dataSet = (BarDataSet) barChart.getData().getDataSetByIndex(0);
            if (dataSet != null) {
                updateBarColors(dataSet, currentBarEntries, currentTargetKcal);
                barChart.invalidate();
            }
        }
    }

    @Override
    public void onNothingSelected() {}
}
