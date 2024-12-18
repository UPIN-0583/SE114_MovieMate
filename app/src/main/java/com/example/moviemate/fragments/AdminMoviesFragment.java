package com.example.moviemate.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.moviemate.R;
import com.example.moviemate.activities.SearchActivity;
import com.example.moviemate.adapters.MoviePagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AdminMoviesFragment extends Fragment {
    private Animation rotateOpen;
    private Animation rotateClose;
    private Animation fromBottom;
    private Animation toBottom;

    private boolean isActionMenuOpen = false;
    private FloatingActionButton fab, fabAddMovie, fabSearchMovie;

    public AdminMoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_movies, container, false);

        TabLayout tabLayout = view.findViewById(R.id.adminMoviesTabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.adminMoviesViewPager);

        MoviePagerAdapter adapter = new MoviePagerAdapter(this, true);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
           if (position == 0) {
               tab.setText("Now Playing");
           }
           else if (position == 1) {
               tab.setText("Coming Soon");
           }
        }).attach();

        // FAB menu
        rotateOpen = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(getContext(), R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(getContext(), R.anim.to_bottom_anim);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            toggleFabMenu();
        });
        fabAddMovie = view.findViewById(R.id.fabAddMovie);
        fabAddMovie.setOnClickListener(v -> {
            addMovie();
        });
        fabSearchMovie = view.findViewById(R.id.fabSearchMovie);
        fabSearchMovie.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });


        return view;
    }

    private void addMovie() {
    }

    private void toggleFabMenu() {
        setFabMenuVisibility(isActionMenuOpen);
        setAnimation(isActionMenuOpen);
        isActionMenuOpen = !isActionMenuOpen;
    }

    private void setFabMenuVisibility(boolean isActionMenuOpen) {
        if (isActionMenuOpen) {
            fabAddMovie.setVisibility(View.INVISIBLE);
            fabSearchMovie.setVisibility(View.INVISIBLE);
        } else {
            fabAddMovie.setVisibility(View.VISIBLE);
            fabSearchMovie.setVisibility(View.VISIBLE);
        }
    }
    private void setAnimation(boolean isActionMenuOpen) {
        if (isActionMenuOpen) {
            fab.startAnimation(rotateClose);
            fabAddMovie.startAnimation(toBottom);
            fabSearchMovie.startAnimation(toBottom);
        } else {
            fab.startAnimation(rotateOpen);
            fabAddMovie.startAnimation(fromBottom);
            fabSearchMovie.startAnimation(fromBottom);
        }
    }
}