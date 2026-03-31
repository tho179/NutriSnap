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
import com.example.nutrisnap.R;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
    private float userWeight = 52.56f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insights, container, false);

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
            // Luôn cho phép cập nhật cân nặng cho "ngày hôm nay"
            showUpdateWeightDialog();
        });
        
        updateViewMode("weekly");

        return view;
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
        tvDate.setText("Today, " + sdf.format(Calendar.getInstance().getTime()));
        edtWeight.setText(String.valueOf(userWeight));

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnUpdateConfirm.setOnClickListener(v -> {
            String weightStr = edtWeight.getText().toString();
            if (!weightStr.isEmpty()) {
                try {
                    userWeight = Float.parseFloat(weightStr);
                    Toast.makeText(getContext(), "Weight updated: " + weightStr + " kg", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    updateRangeTextAndCharts();
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
        Calendar realToday = Calendar.getInstance();
        if (currentMode.equals("weekly")) {
            Calendar start = (Calendar) currentCalendar.clone();
            start.setFirstDayOfWeek(Calendar.MONDAY);
            start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_YEAR, 6);
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.US);
            tvInsightsRange.setText(sdf.format(start.getTime()) + " - " + sdf.format(end.getTime()) + ", " + currentCalendar.get(Calendar.YEAR));
            
            String[] weekLabels = new String[7];
            int todayIdx = -1;
            Calendar labelCal = (Calendar) start.clone();
            for (int i = 0; i < 7; i++) {
                weekLabels[i] = String.valueOf(labelCal.get(Calendar.DAY_OF_MONTH));
                if (labelCal.get(Calendar.YEAR) == realToday.get(Calendar.YEAR) && 
                    labelCal.get(Calendar.DAY_OF_YEAR) == realToday.get(Calendar.DAY_OF_YEAR)) {
                    todayIdx = i;
                }
                labelCal.add(Calendar.DAY_OF_YEAR, 1);
            }
            setupBarChart(7, weekLabels, todayIdx);
            setupLineChart(7, weekLabels, todayIdx);
        } else if (currentMode.equals("monthly")) {
            Calendar end = (Calendar) currentCalendar.clone();
            Calendar start = (Calendar) end.clone();
            start.add(Calendar.MONTH, -6);
            
            SimpleDateFormat sdfRange = new SimpleDateFormat("MMM", Locale.US);
            tvInsightsRange.setText(sdfRange.format(start.getTime()) + " - " + sdfRange.format(end.getTime()) + ", " + end.get(Calendar.YEAR));
            
            String[] monthLabels = new String[7];
            int todayIdx = -1;
            Calendar labelCal = (Calendar) start.clone();
            for (int i = 0; i < 7; i++) {
                monthLabels[i] = sdfRange.format(labelCal.getTime());
                if (labelCal.get(Calendar.YEAR) == realToday.get(Calendar.YEAR) && 
                    labelCal.get(Calendar.MONTH) == realToday.get(Calendar.MONTH)) {
                    todayIdx = i;
                }
                labelCal.add(Calendar.MONTH, 1);
            }
            setupBarChart(7, monthLabels, todayIdx);
            setupLineChart(7, monthLabels, todayIdx);
        } else {
            int endYear = currentCalendar.get(Calendar.YEAR);
            int startYear = endYear - 6;
            tvInsightsRange.setText(startYear + " - " + endYear);
            
            String[] yearLabels = new String[7];
            int todayIdx = -1;
            for (int i = 0; i < 7; i++) {
                int year = startYear + i;
                yearLabels[i] = String.valueOf(year);
                if (year == realToday.get(Calendar.YEAR)) {
                    todayIdx = i;
                }
            }
            setupBarChart(7, yearLabels, todayIdx);
            setupLineChart(7, yearLabels, todayIdx);
        }
        setupPieChart();
    }

    private void configureBarChart(String[] labels) {
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
        leftAxis.setAxisMaximum(4500f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F2F2F2"));
        leftAxis.setDrawAxisLine(false);
        
        leftAxis.removeAllLimitLines();
        LimitLine limitLine = new LimitLine(2500f, "");
        limitLine.setLineColor(Color.parseColor("#66BB6A"));
        limitLine.setLineWidth(1.5f);
        limitLine.enableDashedLine(10f, 10f, 0f);
        leftAxis.addLimitLine(limitLine);
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
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F2F2F2"));
        leftAxis.setDrawAxisLine(false);
        lineChart.setExtraOffsets(10, 10, 10, 10);
    }

    private void setupBarChart(int count, String[] labels, int todayIdx) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float val = (float) (Math.random() * 2000) + 1000;
            entries.add(new BarEntry(i, val));
        }
        BarDataSet dataSet = new BarDataSet(entries, "Calories");
        selectedBarIndex = (todayIdx != -1) ? todayIdx : count - 1;
        updateBarColors(dataSet, count);
        dataSet.setDrawValues(true);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return (int) barEntry.getX() == selectedBarIndex ? String.valueOf((int) barEntry.getY()) : "";
            }
        });
        barChart.setData(new BarData(dataSet));
        barChart.getData().setBarWidth(0.65f);
        configureBarChart(labels);
        barChart.animateY(800);
    }

    private void setupLineChart(int count, String[] labels, int todayIdx) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float val;
            if (i == todayIdx) {
                val = userWeight;
            } else {
                // Simulated previous data or random for illustration
                val = 52.0f + (float)(Math.random() * 2.0);
            }
            entries.add(new Entry(i, val));
        }

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
                return String.valueOf(entry.getY());
            }
        });
        
        lineChart.setData(new LineData(dataSet));
        configureLineChart(labels);
        lineChart.animateX(800);
    }

    private void updateBarColors(BarDataSet dataSet, int count) {
        int selectedColor = Color.parseColor("#66BB6A");
        int defaultColor = Color.parseColor("#C8E6C9");
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            colors.add(i == selectedBarIndex ? selectedColor : defaultColor);
        }
        dataSet.setColors(colors);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        selectedBarIndex = (int) e.getX();
        if (barChart.getData() != null && barChart.getData().getDataSetCount() > 0) {
            BarDataSet dataSet = (BarDataSet) barChart.getData().getDataSetByIndex(0);
            if (dataSet != null) {
                updateBarColors(dataSet, dataSet.getEntryCount());
                barChart.invalidate();
            }
        }
    }

    @Override
    public void onNothingSelected() {}

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(50f, "Protein"));
        entries.add(new PieEntry(30f, "Carbs"));
        entries.add(new PieEntry(20f, "Fat"));
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#FF9800"), Color.parseColor("#FF5252"), Color.parseColor("#03A9F4")});
        dataSet.setSliceSpace(3f);
        dataSet.setDrawValues(false);
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setHoleRadius(65f);
        pieChart.animateXY(800, 800);
    }
}