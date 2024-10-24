package com.example.moviemate.fragments;

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

import com.example.moviemate.R;
import com.example.moviemate.adapters.BannerAdapter;
import com.example.moviemate.adapters.MovieAdapter;
import com.example.moviemate.adapters.MovieSearchAdapter;
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

    private RecyclerView bannerRecycler, nowPlayingRecycler, comingSoonRecycler, searchResultRecycler;
    private BannerAdapter bannerAdapter;
    private MovieAdapter nowPlayingAdapter, comingSoonAdapter;
    private MovieSearchAdapter searchResultAdapter;
    private List<Banner> bannerList;
    private List<Movie> nowPlayingList, comingSoonList, allMoviesList, filteredMovies;
    private EditText searchMovies;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerViews
        bannerRecycler = view.findViewById(R.id.banner_recycler);
        nowPlayingRecycler = view.findViewById(R.id.now_playing_recycler);
        comingSoonRecycler = view.findViewById(R.id.coming_soon_recycler);
        searchResultRecycler = view.findViewById(R.id.search_result_recycler);
        searchMovies = view.findViewById(R.id.search_movies);

        // Set layout managers for RecyclerViews
        bannerRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        nowPlayingRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        comingSoonRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        searchResultRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize lists and adapters
        bannerList = new ArrayList<>();
        nowPlayingList = new ArrayList<>();
        comingSoonList = new ArrayList<>();
        filteredMovies = new ArrayList<>();
        allMoviesList = new ArrayList<>();

        bannerAdapter = new BannerAdapter(getContext(), bannerList);
        nowPlayingAdapter = new MovieAdapter(getContext(), nowPlayingList);
        comingSoonAdapter = new MovieAdapter(getContext(), comingSoonList);
        searchResultAdapter = new MovieSearchAdapter(getContext(), filteredMovies);

        // Set adapters for RecyclerViews
        bannerRecycler.setAdapter(bannerAdapter);
        nowPlayingRecycler.setAdapter(nowPlayingAdapter);
        comingSoonRecycler.setAdapter(comingSoonAdapter);
        searchResultRecycler.setAdapter(searchResultAdapter);

        // Load data from Firebase
        loadBanners();
        loadMovies();  // Tải cả phim Now Playing và Coming Soon

        // Lọc phim khi tìm kiếm
        searchMovies.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMovies(s.toString()); // Lọc danh sách phim khi nhập từ khóa
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
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

    // Lọc danh sách phim theo từ khóa tìm kiếm
    private void filterMovies(String query) {
        filteredMovies.clear();
        if (query.isEmpty()) {
            searchResultRecycler.setVisibility(View.GONE); // Ẩn RecyclerView khi không có từ khóa
        } else {
            for (Movie movie : allMoviesList) {
                if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredMovies.add(movie);
                }
            }
            searchResultAdapter.notifyDataSetChanged();
            searchResultRecycler.setVisibility(View.VISIBLE); // Hiển thị RecyclerView khi có kết quả
        }
    }
}
