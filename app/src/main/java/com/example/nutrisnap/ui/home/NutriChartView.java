package com.example.nutrisnap.ui.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class NutriChartView extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rectF = new RectF();
    private float proteinPercent = 50f;
    private float carbsPercent = 30f;
    private float fatPercent = 20f;

    public NutriChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setData(float protein, float carbs, float fat) {
        float total = protein + carbs + fat;
        if (total == 0) return;
        this.proteinPercent = (protein / total) * 100;
        this.carbsPercent = (carbs / total) * 100;
        this.fatPercent = (fat / total) * 100;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        float radius = Math.min(width, height) / 2f - 40;
        rectF.set(width / 2f - radius, height / 2f - radius, width / 2f + radius, height / 2f + radius);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(50f);
        paint.setStrokeCap(Paint.Cap.BUTT);

        float startAngle = -90f;

        // Vẽ Protein (Orange)
        paint.setColor(Color.parseColor("#FF9800"));
        float proteinAngle = (proteinPercent / 100f) * 360f;
        canvas.drawArc(rectF, startAngle, proteinAngle, false, paint);
        drawTextOnArc(canvas, String.format("%.0f%%", proteinPercent), startAngle + proteinAngle / 2f, radius);

        // Vẽ Carbs (Red)
        startAngle += proteinAngle;
        paint.setColor(Color.parseColor("#FF5252"));
        float carbsAngle = (carbsPercent / 100f) * 360f;
        canvas.drawArc(rectF, startAngle, carbsAngle, false, paint);
        drawTextOnArc(canvas, String.format("%.0f%%", carbsPercent), startAngle + carbsAngle / 2f, radius);

        // Vẽ Fat (Blue)
        startAngle += carbsAngle;
        paint.setColor(Color.parseColor("#03A9F4"));
        float fatAngle = (fatPercent / 100f) * 360f;
        canvas.drawArc(rectF, startAngle, fatAngle, false, paint);
        drawTextOnArc(canvas, String.format("%.0f%%", fatPercent), startAngle + fatAngle / 2f, radius);
    }

    private void drawTextOnArc(Canvas canvas, String text, float angle, float radius) {
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);

        double x = getWidth() / 2f + radius * Math.cos(Math.toRadians(angle));
        double y = getHeight() / 2f + radius * Math.sin(Math.toRadians(angle)) + 8;
        canvas.drawText(text, (float) x, (float) y, textPaint);
    }
}