package com.example.moviemate.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.adapters.Movie2Adapter;
import com.example.moviemate.models.Movie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MovieListFragment extends Fragment {

    private static final String ARG_STATUS = "status"; // Dùng status

    private RecyclerView recyclerView;
    private Movie2Adapter movieAdapter;
    private List<Movie> movieList;
    private String status;

    public static MovieListFragment newInstance(String status) {
        MovieListFragment fragment = new MovieListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        // Sử dụng GridLayoutManager với 2 cột
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        movieList = new ArrayList<>();
        movieAdapter = new Movie2Adapter(getContext(), movieList);
        recyclerView.setAdapter(movieAdapter);

        if (getArguments() != null) {
            status = getArguments().getString(ARG_STATUS);
        }

        loadMoviesFromDatabase();  // Tải phim từ cơ sở dữ liệu dựa trên status

        return view;
    }

    private void loadMoviesFromDatabase() {
        DatabaseReference moviesRef = FirebaseDatabase.getInstance().getReference("Movies");

        moviesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                movieList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Movie movie = snapshot.getValue(Movie.class);
                    if (movie != null && status.equals(movie.getStatus())) {  // Lọc phim theo status
                        movieList.add(movie);
                    }
                }
                movieAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }
}
