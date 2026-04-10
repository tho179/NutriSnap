package com.example.nutrisnap.ui.notification;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nutrisnap.R;
import com.example.nutrisnap.model.NotificationItem;
import com.example.nutrisnap.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NotificationFragment extends Fragment {

    private RecyclerView rvHistoryReminders, rvHistorySystem;
    private NotificationAdapter adapterReminders, adapterSystem;
    private List<NotificationItem> listReminders, listSystem;
    private FirebaseFirestore db;
    private TextView tvEmpty;
    
    private EditText edtContent;
    private TextView tvDate, tvTime;
    private Button btnSave;
    private Calendar reminderCalendar;

    private boolean isRemindersVisible = false;
    private boolean isSystemVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        db = FirebaseFirestore.getInstance();
        listReminders = new ArrayList<>();
        listSystem = new ArrayList<>();
        reminderCalendar = Calendar.getInstance();

        initViews(view);
        setupReminderInputs();
        setupRecyclerViews();
        loadNotifications();

        return view;
    }

    private void initViews(View view) {
        view.findViewById(R.id.btn_back_notification).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        
        edtContent = view.findViewById(R.id.edt_reminder_content_main);
        tvDate = view.findViewById(R.id.tv_reminder_date_main);
        tvTime = view.findViewById(R.id.tv_reminder_time_main);
        btnSave = view.findViewById(R.id.btn_save_reminder_main);
        
        rvHistoryReminders = view.findViewById(R.id.rv_history_reminders);
        rvHistorySystem = view.findViewById(R.id.rv_history_system);
        tvEmpty = view.findViewById(R.id.tv_empty_notifications);

        View headerReminders = view.findViewById(R.id.layout_history_reminder_header);
        ImageView arrowReminders = view.findViewById(R.id.iv_history_reminder_arrow);
        headerReminders.setOnClickListener(v -> {
            isRemindersVisible = !isRemindersVisible;
            rvHistoryReminders.setVisibility(isRemindersVisible ? View.VISIBLE : View.GONE);
            arrowReminders.setRotation(isRemindersVisible ? 90 : -90);
        });

        View headerSystem = view.findViewById(R.id.layout_history_system_header);
        ImageView arrowSystem = view.findViewById(R.id.iv_history_system_arrow);
        headerSystem.setOnClickListener(v -> {
            isSystemVisible = !isSystemVisible;
            rvHistorySystem.setVisibility(isSystemVisible ? View.VISIBLE : View.GONE);
            arrowSystem.setRotation(isSystemVisible ? 90 : -90);
        });
    }

    private void setupReminderInputs() {
        tvDate.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                reminderCalendar.set(Calendar.YEAR, year);
                reminderCalendar.set(Calendar.MONTH, month);
                reminderCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                tvDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year));
            }, reminderCalendar.get(Calendar.YEAR), reminderCalendar.get(Calendar.MONTH), reminderCalendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        tvTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                reminderCalendar.set(Calendar.MINUTE, minute);
                reminderCalendar.set(Calendar.SECOND, 0);
                tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
            }, reminderCalendar.get(Calendar.HOUR_OF_DAY), reminderCalendar.get(Calendar.MINUTE), true);
            timePicker.show();
        });

        btnSave.setOnClickListener(v -> {
            String content = edtContent.getText().toString().trim();
            if (content.isEmpty()) {
                edtContent.setError("Nhập nội dung nhắc nhở");
                return;
            }
            if (reminderCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                Toast.makeText(getContext(), "Vui lòng chọn thời gian tương lai", Toast.LENGTH_SHORT).show();
                return;
            }
            saveAndScheduleReminder(content, reminderCalendar.getTimeInMillis());
        });
    }

    private void setupRecyclerViews() {
        adapterReminders = new NotificationAdapter(listReminders);
        rvHistoryReminders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistoryReminders.setAdapter(adapterReminders);

        adapterSystem = new NotificationAdapter(listSystem);
        rvHistorySystem.setLayoutManager(new LinearLayoutManager(getContext()));
        rvHistorySystem.setAdapter(adapterSystem);
    }

    private void loadNotifications() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        db.collection("users").document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listReminders.clear();
                    listSystem.clear();
                    
                    List<NotificationItem> all = queryDocumentSnapshots.toObjects(NotificationItem.class);
                    for (NotificationItem item : all) {
                        if ("REMINDER".equals(item.getType())) {
                            listReminders.add(item);
                        } else {
                            listSystem.add(item);
                        }
                    }
                    
                    adapterReminders.notifyDataSetChanged();
                    adapterSystem.notifyDataSetChanged();
                    
                    tvEmpty.setVisibility(all.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void saveAndScheduleReminder(String message, long timestamp) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        String id = db.collection("users").document(userId).collection("notifications").document().getId();
        NotificationItem item = new NotificationItem(id, "Nhắc nhở cá nhân", message, timestamp, "REMINDER");

        db.collection("users").document(userId)
                .collection("notifications")
                .document(id)
                .set(item)
                .addOnSuccessListener(aVoid -> {
                    NotificationHelper.scheduleNotification(requireContext(), "Nhắc nhở từ NutriSnap", message, timestamp);
                    Toast.makeText(getContext(), "Đã đặt lịch!", Toast.LENGTH_SHORT).show();
                    edtContent.setText("");
                    loadNotifications();
                });
    }
}