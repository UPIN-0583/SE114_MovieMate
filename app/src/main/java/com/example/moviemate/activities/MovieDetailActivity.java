package com.example.moviemate.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.example.moviemate.models.MovieShowtime;
import com.example.moviemate.models.Person;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView moviePoster;
    private TextView movieTitle, movieGenre, movieRating, movieLanguage, movieTime, movieDescription, cinemaSectionTitle;
    private RecyclerView cinemaRecyclerView, directorRecyclerView, actorRecyclerView;
    private CinemaAdapter cinemaAdapter;
    private PersonAdapter directorAdapter, actorAdapter;
    private Button continueButton, watchTrailerButton;
    private DatabaseReference movieRef, cinemaRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // Khởi tạo các view
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
        continueButton = findViewById(R.id.continue_button);
        watchTrailerButton = findViewById(R.id.watch_trailer_button);
        cinemaSectionTitle = findViewById(R.id.cinema_section_title);

        // Nhận movie_id từ Intent
        Intent intent = getIntent();
        int movieID = intent.getIntExtra("movie_id", -1);

        // Kiểm tra movie_id hợp lệ
        if (movieID != -1) {
            // Lấy dữ liệu phim từ Firebase dựa trên movieID
            fetchMovieDetailsFromFirebase(movieID);
            // Lấy danh sách rạp chiếu từ Firebase riêng
            fetchCinemasFromFirebase(movieID);
        }

        watchTrailerButton.setOnClickListener(v -> {
            // Xử lý mở trailer
            String trailerUrl = (String) watchTrailerButton.getTag(); // Lưu URL trailer vào tag của nút
            if (trailerUrl != null && !trailerUrl.isEmpty()) {
                Intent watchTrailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                startActivity(watchTrailerIntent);
            } else {
                Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lấy dữ liệu phim từ Firebase Realtime Database
    private void fetchMovieDetailsFromFirebase(int movieID) {
        // Tham chiếu đến Firebase Realtime Database
        movieRef = FirebaseDatabase.getInstance().getReference("Movies").child(String.valueOf(movieID));

        movieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Movie movie = snapshot.getValue(Movie.class);
                    if (movie != null) {
                        displayMovieDetails(movie);
                    }
                } else {
                    Toast.makeText(MovieDetailActivity.this, "Movie not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MovieDetailActivity.this, "Error fetching movie details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hiển thị chi tiết phim sau khi lấy từ Firebase
    private void displayMovieDetails(Movie movie) {
        movieTitle.setText(movie.getTitle());
        movieGenre.setText("Movie genre: " + String.join(", ", movie.getGenre()));
        movieRating.setText("Rating: " + movie.getRating());
        movieLanguage.setText("Language: " + movie.getLanguage());
        movieTime.setText("Time: " + movie.getTime());
        movieDescription.setText("Description: " + movie.getDescription());
        Picasso.get().load(movie.getPoster()).into(moviePoster);

        // Lưu URL trailer vào tag của nút xem trailer
        watchTrailerButton.setTag(movie.getTrailer());

        // Hiển thị danh sách đạo diễn
        List<Person> directors = movie.getDirector();
        if (directors != null && !directors.isEmpty()) {
            directorRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            directorAdapter = new PersonAdapter(this, directors);
            directorRecyclerView.setAdapter(directorAdapter);
            directorRecyclerView.setVisibility(View.VISIBLE);
        }

        // Hiển thị danh sách diễn viên
        List<Person> actors = movie.getActor();
        if (actors != null && !actors.isEmpty()) {
            actorRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            actorAdapter = new PersonAdapter(this, actors);
            actorRecyclerView.setAdapter(actorAdapter);
            actorRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Lấy dữ liệu rạp chiếu từ Firebase Realtime Database
    private void fetchCinemasFromFirebase(int movieID) {
        // Tham chiếu đến danh sách rạp chiếu phim
        DatabaseReference cinemaRef = FirebaseDatabase.getInstance().getReference("Cinemas");

        cinemaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Cinema> cinemaList = new ArrayList<>();

                // Lặp qua tất cả các rạp chiếu
                for (DataSnapshot cinemaSnapshot : snapshot.getChildren()) {
                    Cinema cinema = cinemaSnapshot.getValue(Cinema.class);

                    // Kiểm tra nếu rạp này có phim với movieID
                    if (cinema != null && cinema.getMovies() != null) {
                        for (MovieShowtime movieShowtime : cinema.getMovies()) {
                            if (movieShowtime.getMovieID() == movieID) {
                                cinemaList.add(cinema); // Thêm rạp vào danh sách nếu tìm thấy phim
                                break; // Không cần tiếp tục kiểm tra các phim khác trong rạp này
                            }
                        }
                    }
                }

                // Hiển thị các rạp trong RecyclerView
                displayCinemas(cinemaList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MovieDetailActivity.this, "Error fetching cinemas", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Hiển thị danh sách rạp chiếu phim
    private void displayCinemas(List<Cinema> cinemaList) {
        if (cinemaList != null && !cinemaList.isEmpty()) {
            // Gán layout manager cho RecyclerView
            cinemaRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Gán adapter cho RecyclerView
            cinemaAdapter = new CinemaAdapter(this, cinemaList);
            cinemaRecyclerView.setAdapter(cinemaAdapter);

            // Hiển thị RecyclerView và các thành phần liên quan
            cinemaRecyclerView.setVisibility(View.VISIBLE);
            continueButton.setVisibility(View.VISIBLE);
            cinemaSectionTitle.setVisibility(View.VISIBLE);
        } else {
            // Ẩn RecyclerView nếu không có dữ liệu
            cinemaSectionTitle.setVisibility(View.GONE);
            cinemaRecyclerView.setVisibility(View.GONE);
        }
    }

}
