package com.example.moviemate.activities;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.moviemate.R;
import com.example.moviemate.fragments.AdminMoviesFragment;
import com.example.moviemate.fragments.AdminPanelFragment;
import com.example.moviemate.fragments.AdminStatisticFragment;
import com.example.moviemate.fragments.AdminUsersFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.admin_bottom_nav);

        // Movies fragment is the default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.adminContainer, new AdminMoviesFragment())
                    .commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.navAdminMovies)
                selectedFragment = new AdminMoviesFragment();
            else if (item.getItemId() == R.id.navAdminUsers)
                selectedFragment = new AdminUsersFragment();
            else if (item.getItemId() == R.id.navAdminStatistic)
                selectedFragment = new AdminStatisticFragment();
            else if (item.getItemId() == R.id.navAdminPanel)
                selectedFragment = new AdminPanelFragment();

            if (selectedFragment == null)
                return false;

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.adminContainer, selectedFragment)
                    .commit();

            return true;
        });
    }
}