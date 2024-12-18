package com.example.moviemate.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.moviemate.R;
import com.example.moviemate.activities.SearchActivity;
import com.example.moviemate.adapters.BannerAdapter;
import com.example.moviemate.adapters.MovieAdapter;
import com.example.moviemate.adapters.MovieSearchAdapter;
import com.example.moviemate.models.Banner;
import com.example.moviemate.models.Movie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private TextView userNameTextView;
    private RecyclerView bannerRecycler, nowPlayingRecycler, comingSoonRecycler;
    private BannerAdapter bannerAdapter;
    private MovieAdapter nowPlayingAdapter, comingSoonAdapter;
    private List<Banner> bannerList;
    private List<Movie> nowPlayingList, comingSoonList, allMoviesList, filteredMovies;
    private EditText searchMovies;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);



        // Initialize
        userNameTextView = view.findViewById(R.id.user_name);
        bannerRecycler = view.findViewById(R.id.banner_recycler);
        nowPlayingRecycler = view.findViewById(R.id.now_playing_recycler);
        comingSoonRecycler = view.findViewById(R.id.coming_soon_recycler);
        searchMovies = view.findViewById(R.id.search_movies);

        // Set layout managers for RecyclerViews
        bannerRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        nowPlayingRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        comingSoonRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));


        // Initialize lists and adapters
        bannerList = new ArrayList<>();
        nowPlayingList = new ArrayList<>();
        comingSoonList = new ArrayList<>();
        filteredMovies = new ArrayList<>();
        allMoviesList = new ArrayList<>();

        bannerAdapter = new BannerAdapter(getContext(), bannerList);
        nowPlayingAdapter = new MovieAdapter(getContext(), nowPlayingList);
        comingSoonAdapter = new MovieAdapter(getContext(), comingSoonList);


        // Set adapters for RecyclerViews
        bannerRecycler.setAdapter(bannerAdapter);
        nowPlayingRecycler.setAdapter(nowPlayingAdapter);
        comingSoonRecycler.setAdapter(comingSoonAdapter);


        // Load data from Firebase
        loadBanners();
        loadMovies();  // Tải cả phim Now Playing và Coming Soon


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userID = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userID);

            // Lấy tên người dùng từ Firebase Database
            usersRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userName = snapshot.getValue(String.class);
                    if (userName != null) {
                        userNameTextView.setText(String.format("Hi, %s", userName));
                    } else {
                        userNameTextView.setText(R.string.hi_user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("HomeFragment", "Failed to read user name.", error.toException());
                }
            });
        }

        searchMovies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để mở SearchActivity
                Intent intent = new Intent(getActivity(), SearchActivity.class);

                // Khởi chạy SearchActivity
                startActivity(intent);
            }
        });


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

    // Tải tất cả các phim và phân loại dựa trên trường status
    private void loadMovies() {
        DatabaseReference moviesRef = FirebaseDatabase.getInstance().getReference("Movies");
        moviesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nowPlayingList.clear();
                comingSoonList.clear();
                allMoviesList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Movie movie = snapshot.getValue(Movie.class);
                    if (movie != null) {
                        // Phân loại phim dựa trên status
                        if ("Now Playing".equals(movie.getStatus())) {
                            nowPlayingList.add(movie);
                        } else if ("Coming Soon".equals(movie.getStatus())) {
                            comingSoonList.add(movie);
                        }
                        allMoviesList.add(movie);  // Lưu tất cả phim để hỗ trợ tìm kiếm
                    }
                }

                nowPlayingAdapter.notifyDataSetChanged();
                comingSoonAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DatabaseError", "loadMovies:onCancelled", databaseError.toException());
            }
        });
    }


}
