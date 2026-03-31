package com.example.nutrisnap;

import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// Import các Fragment
import com.example.nutrisnap.ui.home.HomeFragment;
import com.example.nutrisnap.ui.home.BreakfastFragment;
import com.example.nutrisnap.ui.home.LunchFragment;
import com.example.nutrisnap.ui.home.DinnerFragment;
import com.example.nutrisnap.ui.home.SnacksFragment;
import com.example.nutrisnap.ui.home.AnalysisFragment;
import com.example.nutrisnap.ui.chatbot.ChatbotFragment;
import com.example.nutrisnap.ui.report.InsightsFragment;
import com.example.nutrisnap.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_chatbot) {
                selectedFragment = new ChatbotFragment();
            } else if (id == R.id.nav_insights) {
                selectedFragment = new InsightsFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                updateBottomNavVisibility(selectedFragment);
                return true;
            }
            return false;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            updateBottomNavVisibility(currentFragment);
        });
    }

    public void navigateToHome() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void updateBottomNavVisibility(Fragment fragment) {
        if (fragment instanceof BreakfastFragment || 
            fragment instanceof LunchFragment || 
            fragment instanceof DinnerFragment || 
            fragment instanceof SnacksFragment ||
            fragment instanceof AnalysisFragment ||
            fragment instanceof ChatbotFragment) {
            bottomNav.setVisibility(View.GONE);
        } else {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }
}