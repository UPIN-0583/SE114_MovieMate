package com.example.moviemate.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.moviemate.fragments.AdminMovieListFragment;
import com.example.moviemate.fragments.MovieListFragment;

public class MoviePagerAdapter extends FragmentStateAdapter {
    private boolean isAdmin;

    public MoviePagerAdapter(@NonNull Fragment fragment, boolean isAdmin) {
        super(fragment);
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (isAdmin) {
            if (position == 0) {
                return AdminMovieListFragment.newInstance("Now Playing"); // Now Playing
            } else {
                return AdminMovieListFragment.newInstance("Coming Soon"); // Coming Soon
            }
        }

        if (position == 0) {
            return MovieListFragment.newInstance("Now Playing"); // Now Playing
        } else {
            return MovieListFragment.newInstance("Coming Soon"); // Coming Soon
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Có hai tab: Now Playing và Coming Soon
    }
}
