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

    private RecyclerView bannerRecycler, topMoviesRecycler, upcomingMoviesRecycler;
    private BannerAdapter bannerAdapter;
    private MovieAdapter topMoviesAdapter, upcomingMoviesAdapter;
    private List<Banner> bannerList;
    private List<Movie> topMoviesList, upcomingMoviesList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerViews
        bannerRecycler = view.findViewById(R.id.banner_recycler);
        topMoviesRecycler = view.findViewById(R.id.top_movies_recycler);
        upcomingMoviesRecycler = view.findViewById(R.id.upcoming_movies_recycler);

        // Set layout managers for RecyclerViews
        bannerRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        topMoviesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        upcomingMoviesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Initialize lists and adapters
        bannerList = new ArrayList<>();
        topMoviesList = new ArrayList<>();
        upcomingMoviesList = new ArrayList<>();

        bannerAdapter = new BannerAdapter(getContext(), bannerList);
        topMoviesAdapter = new MovieAdapter(getContext(), topMoviesList);
        upcomingMoviesAdapter = new MovieAdapter(getContext(), upcomingMoviesList);

        // Set adapters for RecyclerViews
        bannerRecycler.setAdapter(bannerAdapter);
        topMoviesRecycler.setAdapter(topMoviesAdapter);
        upcomingMoviesRecycler.setAdapter(upcomingMoviesAdapter);

        // Load data from Firebase
        loadBanners();
        loadTopMovies();
        loadUpcomingMovies();

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
                Log.w("DatabaseError", "loadBanner:onCancelled", databaseError.toException());
            }
        });
    }

    private void loadTopMovies() {
        DatabaseReference topMoviesRef = FirebaseDatabase.getInstance().getReference("Items");
        topMoviesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                topMoviesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Movie movie = snapshot.getValue(Movie.class);
                    topMoviesList.add(movie);
                }
                topMoviesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DatabaseError", "loadTopMovies:onCancelled", databaseError.toException());
            }
        });
    }

    private void loadUpcomingMovies() {
        DatabaseReference upcomingMoviesRef = FirebaseDatabase.getInstance().getReference("Upcomming");
        upcomingMoviesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                upcomingMoviesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Movie movie = snapshot.getValue(Movie.class);
                    upcomingMoviesList.add(movie);
                }
                upcomingMoviesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DatabaseError", "loadUpcomingMovies:onCancelled", databaseError.toException());
            }
        });
    }
}
