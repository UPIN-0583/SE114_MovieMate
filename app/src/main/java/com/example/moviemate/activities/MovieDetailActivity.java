package com.example.moviemate.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.adapters.CinemaAdapter;
import com.example.moviemate.adapters.PersonAdapter;
import com.example.moviemate.models.Cinema;
import com.example.moviemate.models.Movie;
import com.example.moviemate.models.Person;
import com.example.moviemate.models.ShowTime;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView moviePoster;
    private TextView movieTitle, movieGenre, movieRating, movieLanguage, movieTime, movieDescription, cinemaSectionTitle;
    private RecyclerView cinemaRecyclerView, directorRecyclerView, actorRecyclerView;
    private Button watchTrailerButton, continueButton;
    private Movie movie;
    private Cinema selectedCinema; // Rạp được chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // Khởi tạo các view
        ImageButton backBtn = findViewById(R.id.BackBtn);
        moviePoster = findViewById(R.id.movie_poster);
        movieTitle = findViewById(R.id.movie_title);
        movieGenre = findViewById(R.id.movie_genre);
        movieRating = findViewById(R.id.movie_rating);
        movieLanguage = findViewById(R.id.movie_language);
        movieTime = findViewById(R.id.movie_time);
        movieDescription = findViewById(R.id.movie_description);
        cinemaRecyclerView = findViewById(R.id.cinema_recycler_view);
        directorRecyclerView = findViewById(R.id.director_recycler_view);
        actorRecyclerView = findViewById(R.id.actor_recycler_view);
        watchTrailerButton = findViewById(R.id.watch_trailer_button);
        cinemaSectionTitle = findViewById(R.id.cinema_section_title);
        continueButton = findViewById(R.id.continue_button);

        // Nhận movie từ Intent
        Intent intent = getIntent();
        int movieID = intent.getIntExtra("movieId", -1);
        if (movieID == -1) {
            finish();
            return;
        }

        fetchMovie(movieID);

        backBtn.setOnClickListener(v -> finish());

        // Xử lý khi nhấn nút "Watch Trailer"
        watchTrailerButton.setOnClickListener(v -> {
            String trailerUrl = (String) watchTrailerButton.getTag();
            if (trailerUrl != null && !trailerUrl.isEmpty()) {
                Intent watchTrailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                startActivity(watchTrailerIntent);
            } else {
                Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
            }

        });

        // Xử lý khi nhấn nút "Continue"
        continueButton.setOnClickListener(v -> {
            if (selectedCinema != null) {
                Intent selectSeatIntent = new Intent(MovieDetailActivity.this, SelectSeatActivity.class);
                selectSeatIntent.putExtra("cinema_id", selectedCinema.getCinemaID());
                selectSeatIntent.putExtra("cinema_name", selectedCinema.getCinemaName());
                selectSeatIntent.putExtra("movie", movie);
                startActivity(selectSeatIntent);
            } else {
                Toast.makeText(MovieDetailActivity.this, "Please select a cinema first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMovie(int movieID) {
        DatabaseReference movieRef = FirebaseDatabase.getInstance().getReference("Movies").child("Movie" + movieID);

        movieRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movie = snapshot.getValue(Movie.class);

                if (movie == null) {
                    Toast.makeText(MovieDetailActivity.this, "Movie not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                displayMovieDetails();
                fetchCinemasFromFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MovieDetailActivity.this, "Error fetching movie", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hiển thị chi tiết phim
    private void displayMovieDetails() {
        movieTitle.setText(movie.getTitle());
        movieGenre.setText(String.join(", ", movie.getGenre()));
        movieRating.setText(movie.getRating());
        movieLanguage.setText(movie.getLanguage());
        movieTime.setText(movie.getTime());
        movieDescription.setText(movie.getDescription());
        Picasso.get().load(movie.getPoster()).into(moviePoster);
        watchTrailerButton.setTag(movie.getTrailer());

        List<Person> directors = movie.getDirector();
        if (directors != null && !directors.isEmpty()) {
            directorRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            PersonAdapter directorAdapter = new PersonAdapter(this, directors, false);
            directorRecyclerView.setAdapter(directorAdapter);
            directorRecyclerView.setVisibility(View.VISIBLE);
        }

        List<Person> actors = movie.getActor();
        if (actors != null && !actors.isEmpty()) {
            actorRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            PersonAdapter actorAdapter = new PersonAdapter(this, actors, false);
            actorRecyclerView.setAdapter(actorAdapter);
            actorRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Lấy dữ liệu rạp chiếu từ Firebase Realtime Database
    private void fetchCinemasFromFirebase() {
        DatabaseReference cinemaRef = FirebaseDatabase.getInstance().getReference("Cinemas");

        cinemaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Cinema> cinemaList = new ArrayList<>();

                for (DataSnapshot cinemaSnapshot : snapshot.getChildren()) {
                    Cinema cinema = cinemaSnapshot.getValue(Cinema.class);
                    if (cinema != null) {
                        DataSnapshot moviesSnapshot = cinemaSnapshot.child("Movies");
                        boolean hasValidShowTime = false;

                        // Kiểm tra xem có Movie khớp với movie đang chọn hay không
                        for (DataSnapshot movieSnapshot : moviesSnapshot.getChildren()) {
                            String movieIdKey = "Movie" + movie.getMovieID(); // Khớp theo định dạng Movie1, Movie2
                            if (movieSnapshot.getKey().equals(movieIdKey)) {
                                DataSnapshot showTimesSnapshot = movieSnapshot.child("ShowTimes");

                                // Kiểm tra nếu có bất kỳ suất chiếu nào trong `ShowTimes`
                                if (showTimesSnapshot.exists()) {
                                    for (DataSnapshot dateSnapshot : showTimesSnapshot.getChildren()) {
                                        for (DataSnapshot timeSnapshot : dateSnapshot.getChildren()) {
                                            if (timeSnapshot.child("Seats").exists()) {
                                                hasValidShowTime = true;
                                                break;
                                            }
                                        }
                                        if (hasValidShowTime) break;
                                    }
                                }
                            }
                            if (hasValidShowTime) break;
                        }

                        // Chỉ thêm Cinema vào danh sách nếu có suất chiếu hợp lệ
                        if (hasValidShowTime) {
                            cinemaList.add(cinema);
                        }
                    }
                }

                displayCinemas(cinemaList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MovieDetailActivity.this, "Error fetching cinemas", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Hiển thị danh sách rạp chiếu
    private void displayCinemas(List<Cinema> cinemaList) {
        if (cinemaList != null && !cinemaList.isEmpty()) {
            cinemaRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            // Lưu rạp được chọn
            CinemaAdapter cinemaAdapter = new CinemaAdapter(this, cinemaList, cinema -> {
                selectedCinema = cinema; // Lưu rạp được chọn
            });
            cinemaRecyclerView.setAdapter(cinemaAdapter);
            cinemaRecyclerView.setVisibility(View.VISIBLE);
            cinemaSectionTitle.setVisibility(View.VISIBLE);
            continueButton.setVisibility(View.VISIBLE);
        } else {
            Log.d("MovieDetailActivity", "No cinemas found for this movie.");
            cinemaSectionTitle.setVisibility(View.GONE);
            cinemaRecyclerView.setVisibility(View.GONE);
            continueButton.setVisibility(View.GONE);
        }
    }
}
