package com.example.moviemate;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.moviemate.fragments.HomeFragment;
import com.example.moviemate.fragments.MovieFragment;
import com.example.moviemate.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set the default fragment to HomeFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new HomeFragment())
                    .commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                }
//// else if bạn muốn thêm các Fragment khác sau này, bạn có thể thêm tiếp các điều kiện:
//                else if (item.getItemId() == R.id.nav_ticket) {
//                    selectedFragment = new TicketFragment();
                else if (item.getItemId() == R.id.nav_movie) {
                    selectedFragment = new MovieFragment();
                }
                else if (item.getItemId() == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                }


                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, selectedFragment)
                        .commit();

                return true;
            }
        });
    }
}
