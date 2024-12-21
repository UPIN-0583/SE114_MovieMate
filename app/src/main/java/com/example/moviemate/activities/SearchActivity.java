package com.example.moviemate.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.adapters.MovieSearchAdapter;
import com.example.moviemate.adapters.FilterAdapter;
import com.example.moviemate.models.Movie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity implements FilterAdapter.OnFilterSelectedListener {

    private ImageButton backBtn;
    private EditText searchMovies;
    private RecyclerView searchResultRecycler;
    private RecyclerView genreFilterRecycler;
    private RecyclerView yearFilterRecycler;
    private RecyclerView languageFilterRecycler;

    private MovieSearchAdapter searchResultAdapter;
    private FilterAdapter genreAdapter;
    private FilterAdapter yearAdapter;
    private FilterAdapter languageAdapter;

    private List<Movie> filteredMovies;
    private List<Movie> allMoviesList;
    private List<String> selectedGenres;
    private List<String> selectedYears;
    private List<String> selectedLanguages;
    private ImageButton clearButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initializeViews();
        setupRecyclerViews();
        loadMoviesFromDatabase();
        setupListeners();
    }

    private void initializeViews() {
        backBtn = findViewById(R.id.BackBtn);
        searchMovies = findViewById(R.id.search_movies);
        clearButton = findViewById(R.id.clear_button);
        searchResultRecycler = findViewById(R.id.search_result_recycler);
        genreFilterRecycler = findViewById(R.id.genre_filter_recycler);
        yearFilterRecycler = findViewById(R.id.year_filter_recycler);
        languageFilterRecycler = findViewById(R.id.language_filter_recycler);

        allMoviesList = new ArrayList<>();
        filteredMovies = new ArrayList<>();
        selectedGenres = new ArrayList<>();
        selectedYears = new ArrayList<>();
        selectedLanguages = new ArrayList<>();
    }

    private void setupRecyclerViews() {
        // Setup main movies recycler
        searchResultRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchResultAdapter = new MovieSearchAdapter(this, filteredMovies);
        searchResultRecycler.setAdapter(searchResultAdapter);

        // Setup filter recyclers
        LinearLayoutManager genreLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager yearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager languageLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        genreFilterRecycler.setLayoutManager(genreLayoutManager);
        yearFilterRecycler.setLayoutManager(yearLayoutManager);
        languageFilterRecycler.setLayoutManager(languageLayoutManager);
    }

    private void setupListeners() {
        backBtn.setOnClickListener(v -> finish());

        searchMovies.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        clearButton.setOnClickListener(v -> {
            searchMovies.setText("");
            clearAllFilters();
            applyFilters("");
        });
    }

    private void loadMoviesFromDatabase() {
        DatabaseReference moviesRef = FirebaseDatabase.getInstance().getReference("Movies");

        moviesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allMoviesList.clear();
                Set<String> genres = new HashSet<>();
                Set<String> years = new HashSet<>();
                Set<String> languages = new HashSet<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Movie movie = snapshot.getValue(Movie.class);
                    if (movie != null) {
                        allMoviesList.add(movie);

                        // Collect unique filters
                        genres.addAll(movie.getGenre());
                        years.add(String.valueOf(movie.getYear()));
                        String[] movieLanguages = movie.getLanguage().split(",");
                        for (String lang : movieLanguages) {
                            languages.add(lang.trim());
                        }
                    }
                }

                setupFilterAdapters(new ArrayList<>(genres),
                        new ArrayList<>(years),
                        new ArrayList<>(languages));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void setupFilterAdapters(List<String> genres, List<String> years, List<String> languages) {
        genreAdapter = new FilterAdapter(this, genres, selectedGenres, this);
        yearAdapter = new FilterAdapter(this, years, selectedYears, this);
        languageAdapter = new FilterAdapter(this, languages, selectedLanguages, this);

        genreFilterRecycler.setAdapter(genreAdapter);
        yearFilterRecycler.setAdapter(yearAdapter);
        languageFilterRecycler.setAdapter(languageAdapter);
    }

    @Override
    public void onFilterSelected(String filter) {
        if (genreAdapter.getFilterItems().contains(filter)) {
            selectedGenres.add(filter);
        } else if (yearAdapter.getFilterItems().contains(filter)) {
            selectedYears.add(filter);
        } else if (languageAdapter.getFilterItems().contains(filter)) {
            selectedLanguages.add(filter);
        }
        applyFilters(searchMovies.getText().toString());
    }

    @Override
    public void onFilterDeselected(String filter) {
        selectedGenres.remove(filter);
        selectedYears.remove(filter);
        selectedLanguages.remove(filter);
        applyFilters(searchMovies.getText().toString());
    }

    private void applyFilters(String query) {
        filteredMovies.clear();

        for (Movie movie : allMoviesList) {
            boolean matchesSearch = query.isEmpty() ||
                    movie.getTitle().toLowerCase().contains(query.toLowerCase());

            boolean matchesGenre = selectedGenres.isEmpty() ||
                    movie.getGenre().stream().anyMatch(selectedGenres::contains);

            boolean matchesYear = selectedYears.isEmpty() ||
                    selectedYears.contains(String.valueOf(movie.getYear()));

            boolean matchesLanguage = selectedLanguages.isEmpty();
            if (!matchesLanguage) {
                String[] movieLanguages = movie.getLanguage().split(",");
                for (String lang : movieLanguages) {
                    if (selectedLanguages.contains(lang.trim())) {
                        matchesLanguage = true;
                        break;
                    }
                }
            }

            if (matchesSearch && matchesGenre && matchesYear && matchesLanguage) {
                filteredMovies.add(movie);
            }
        }

        searchResultAdapter.notifyDataSetChanged();
    }

    private void clearAllFilters() {
        selectedGenres.clear();
        selectedYears.clear();
        selectedLanguages.clear();

        if (genreAdapter != null) genreAdapter.notifyDataSetChanged();
        if (yearAdapter != null) yearAdapter.notifyDataSetChanged();
        if (languageAdapter != null) languageAdapter.notifyDataSetChanged();
    }
}