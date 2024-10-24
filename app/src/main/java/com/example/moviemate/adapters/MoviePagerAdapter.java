package com.example.moviemate.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.moviemate.fragments.MovieListFragment;

public class MoviePagerAdapter extends FragmentStateAdapter {

    public MoviePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
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
