package com.example.moviemate.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.adapters.CinemaAdapter;
import com.example.moviemate.adapters.PersonAdapter;
import com.example.moviemate.models.Cinema;
import com.example.moviemate.models.Person;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView moviePoster;
    private TextView movieTitle, movieGenre, movieRating, movieLanguage, movieTime, movieDescription, cinemaSectionTitle;
    private RecyclerView cinemaRecyclerView, directorRecyclerView, actorRecyclerView;
    private CinemaAdapter cinemaAdapter;
    private PersonAdapter directorAdapter, actorAdapter;
    private Button continueButton, watchTrailerButton;

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

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String genre = intent.getStringExtra("genre");
        String rating = intent.getStringExtra("rating");
        String language = intent.getStringExtra("language");
        String time = intent.getStringExtra("time");
        String description = intent.getStringExtra("description");
        String posterUrl = intent.getStringExtra("poster");
        List<Cinema> cinemas = (List<Cinema>) intent.getSerializableExtra("cinemas");
        List<Person> directors = (List<Person>) intent.getSerializableExtra("directors");
        List<Person> actors = (List<Person>) intent.getSerializableExtra("actors");
        String trailerUrl = intent.getStringExtra("trailerUrl");


        // Gán dữ liệu vào các view
        movieTitle.setText(title);
        movieGenre.setText("Movie genre: " + genre);
        movieRating.setText("Rating: " + rating);
        movieLanguage.setText("Language: " + language);
        movieTime.setText("Time: " + time);
        movieDescription.setText("Description: " +description);
        Picasso.get().load(posterUrl).into(moviePoster);

        // Hiển thị danh sách rạp chiếu phim
        if (cinemas != null && !cinemas.isEmpty()) {
            cinemaRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            cinemaAdapter = new CinemaAdapter(this, cinemas);
            cinemaRecyclerView.setAdapter(cinemaAdapter);
            cinemaRecyclerView.setVisibility(View.VISIBLE);
            continueButton.setVisibility(View.VISIBLE);
            cinemaSectionTitle.setVisibility(View.VISIBLE);
        }

        // Hiển thị danh sách đạo diễn
        if (directors != null && !directors.isEmpty()) {
            directorRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            directorAdapter = new PersonAdapter(this, directors);
            directorRecyclerView.setAdapter(directorAdapter);
            directorRecyclerView.setVisibility(View.VISIBLE);
        }

        // Hiển thị danh sách diễn viên
        if (actors != null && !actors.isEmpty()) {
            actorRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            actorAdapter = new PersonAdapter(this, actors);
            actorRecyclerView.setAdapter(actorAdapter);
            actorRecyclerView.setVisibility(View.VISIBLE);
        }

        watchTrailerButton.setOnClickListener(v -> {
            if (trailerUrl != null && !trailerUrl.isEmpty()) {
                // Mở URL trailer trong YouTube hoặc trình duyệt
                Intent watchTrailerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                startActivity(watchTrailerIntent);
            } else {
                Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
