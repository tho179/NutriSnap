package com.example.nutrisnap.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.example.nutrisnap.MainActivity;
import com.example.nutrisnap.R;
import com.example.nutrisnap.model.NotificationItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.UUID;

public class NotificationHelper extends BroadcastReceiver {

    public static final String CHANNEL_ID = "NutriSnap_Notifications_High";
    public static final String CHANNEL_NAME = "NutriSnap Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        String type = intent.getStringExtra("type");
        if (type == null) type = "SYSTEM";
        int notificationId = intent.getIntExtra("id", (int) System.currentTimeMillis());

        Log.d("NotificationHelper", "Received broadcast: " + title);
        
        // Lưu vào lịch sử khi thông báo xuất hiện
        saveToFirestore(title, message, type);
        
        showNotification(context, title, message, notificationId);
    }

    private void saveToFirestore(String title, String message, String type) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String id = UUID.randomUUID().toString();
        NotificationItem item = new NotificationItem(id, title, message, System.currentTimeMillis(), type);

        db.collection("users").document(userId)
                .collection("notifications")
                .document(id)
                .set(item);
    }

    public static void showNotification(Context context, String title, String message, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(id, builder.build());
    }

    // Version 1: Dùng cho đặt lịch cụ thể từ User (REMINDER)
    public static void scheduleNotification(Context context, String title, String message, long targetTimestamp) {
        int id = (int) (targetTimestamp / 1000);
        scheduleInternal(context, title, message, id, targetTimestamp, "REMINDER");
    }

    // Version 2: Dùng cho nhắc nhở hàng ngày (SYSTEM)
    public static void scheduleNotification(Context context, String title, String message, int id, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        scheduleInternal(context, title, message, id, calendar.getTimeInMillis(), "SYSTEM");
    }

    private static void scheduleInternal(Context context, String title, String message, int id, long timestamp, String type) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationHelper.class);
        intent.putExtra("title", title);
        intent.putExtra("message", message);
        intent.putExtra("id", id);
        intent.putExtra("type", type);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timestamp, pendingIntent);
            }
        }
    }
}