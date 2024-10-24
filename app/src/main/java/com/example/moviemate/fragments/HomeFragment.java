package com.example.moviemate.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.moviemate.R;
import com.example.moviemate.adapters.BannerAdapter;
import com.example.moviemate.adapters.MovieAdapter;
import com.example.moviemate.models.Banner;
import com.example.moviemate.models.Movie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView bannerRecycler, nowPlayingRecycler, comingSoonRecycler;
    private BannerAdapter bannerAdapter;
    private MovieAdapter nowPlayingAdapter, comingSoonAdapter;
    private List<Banner> bannerList;
    private List<Movie> nowPlayingList, comingSoonList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerViews
        bannerRecycler = view.findViewById(R.id.banner_recycler);
        nowPlayingRecycler = view.findViewById(R.id.now_playing_recycler);
        comingSoonRecycler = view.findViewById(R.id.coming_soon_recycler);

        // Set layout managers for RecyclerViews
        bannerRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        nowPlayingRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        comingSoonRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Initialize lists and adapters
        bannerList = new ArrayList<>();
        nowPlayingList = new ArrayList<>();
        comingSoonList = new ArrayList<>();

        bannerAdapter = new BannerAdapter(getContext(), bannerList);
        nowPlayingAdapter = new MovieAdapter(getContext(), nowPlayingList);
        comingSoonAdapter = new MovieAdapter(getContext(), comingSoonList);

        // Set adapters for RecyclerViews
        bannerRecycler.setAdapter(bannerAdapter);
        nowPlayingRecycler.setAdapter(nowPlayingAdapter);
        comingSoonRecycler.setAdapter(comingSoonAdapter);

        // Load data from Firebase
        loadBanners();
        loadNowPlayingMovies();
        loadComingSoonMovies();

        return view;
    }

    private void loadBanners() {
        DatabaseReference bannersRef = FirebaseDatabase.getInstance().getReference("Banners");
        bannersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bannerList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Banner banner = snapshot.getValue(Banner.class);
                    bannerList.add(banner);
                }
                bannerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DatabaseError", "loadBanners:onCancelled", databaseError.toException());
            }
        });
    }

    private void loadNowPlayingMovies() {
        DatabaseReference nowPlayingRef = FirebaseDatabase.getInstance().getReference("Items");
        nowPlayingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nowPlayingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Movie movie = snapshot.getValue(Movie.class);
                    nowPlayingList.add(movie);
                }
                nowPlayingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DatabaseError", "loadNowPlayingMovies:onCancelled", databaseError.toException());
            }
        });
    }

    private void loadComingSoonMovies() {
        DatabaseReference comingSoonRef = FirebaseDatabase.getInstance().getReference("Upcomming");
        comingSoonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comingSoonList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Movie movie = snapshot.getValue(Movie.class);
                    comingSoonList.add(movie);
                }
                comingSoonAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DatabaseError", "loadComingSoonMovies:onCancelled", databaseError.toException());
            }
        });
    }
}
