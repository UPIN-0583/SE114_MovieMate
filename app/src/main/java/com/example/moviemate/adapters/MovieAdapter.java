package com.example.moviemate.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.activities.MovieDetailActivity;
import com.example.moviemate.models.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private Context context;
    private List<Movie> movieList;

    public MovieAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.movie_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.title.setText(movie.getTitle());
        holder.time.setText(movie.getTime());
        Picasso.get().load(movie.getPoster()).into(holder.posterImage);  // Load movie poster image from URL

        // Kiểm tra danh sách thể loại có null hoặc rỗng không
        List<String> genres = movie.getGenre();
        String genreString = "";

        if (genres == null || genres.isEmpty())
            genreString = "No genre";
        else
            genreString = String.join(", ", genres);

        int MAX_GENRE_LENGTH = 20;
        if (genreString.length() > MAX_GENRE_LENGTH) {
            genreString = genreString.substring(0, MAX_GENRE_LENGTH - 3) + "...";
        }

        holder.genre.setText(genreString);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);

            // Chuyển movie object qua Intent để tránh query lại từ database
            intent.putExtra("movieId", movie.getMovieID());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return (movieList != null) ? movieList.size() : 0;
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;
        TextView title, time, genre;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImage = itemView.findViewById(R.id.movie_poster);
            title = itemView.findViewById(R.id.movie_title);
            time = itemView.findViewById(R.id.movie_time);
            genre = itemView.findViewById(R.id.movie_genre);
        }
    }
}
