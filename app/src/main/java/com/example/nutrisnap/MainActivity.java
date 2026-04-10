package com.example.nutrisnap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.cloudinary.android.MediaManager;
import com.example.nutrisnap.databinding.ActivityMainBinding;
import com.example.nutrisnap.utils.LocaleHelper;
import com.example.nutrisnap.utils.NotificationHelper;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Cloudinary
        initCloudinary();

        requestNotificationPermission();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.navView, navController);
            
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                if (id == R.id.nav_breakfast || id == R.id.nav_lunch || id == R.id.nav_dinner || 
                    id == R.id.nav_snacks || id == R.id.nav_chatbot) {
                    binding.navView.setVisibility(View.GONE);
                } else {
                    binding.navView.setVisibility(View.VISIBLE);
                }
            });
        }

        setupDefaultReminders();
    }

    private void initCloudinary() {
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dadim1ace");
            MediaManager.init(this, config);
            Log.d("CLOUDINARY", "Initialized successfully");
        } catch (IllegalStateException e) {
            Log.d("CLOUDINARY", "Already initialized");
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void setupDefaultReminders() {
        NotificationHelper.scheduleNotification(this, "Bữa sáng tới rồi!", "Đừng quên chụp ảnh bữa sáng để theo dõi calo nhé 🥗", 1001, 8, 0);
        NotificationHelper.scheduleNotification(this, "Đã đến giờ ăn trưa", "Một bữa trưa lành mạnh đang chờ bạn ghi lại đó!", 1002, 12, 0);
        NotificationHelper.scheduleNotification(this, "Ghi lại bữa tối thôi", "Kết thúc ngày bằng việc ghi chép bữa tối đầy đủ nào 🌙", 1003, 19, 0);
        NotificationHelper.scheduleNotification(this, "Kiểm tra cân nặng", "Hôm nay bạn cảm thấy thế nào? Hãy cập nhật cân nặng mới nhé ⚖️", 1004, 9, 30);
        NotificationHelper.scheduleNotification(this, "Chào ngày mới!", "Một cơ thể khỏe mạnh bắt đầu từ một bữa ăn lành mạnh. Cố lên! 💪", 1005, 21, 02);
    }

    public void navigateToHome() {
        if (navController != null) {
            navController.navigate(R.id.nav_home);
        }
    }
}
