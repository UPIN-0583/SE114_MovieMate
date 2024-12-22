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
import com.example.moviemate.activities.AdminMovieDetailActivity;
import com.example.moviemate.activities.MovieDetailActivity;
import com.example.moviemate.models.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Movie2Adapter extends RecyclerView.Adapter<Movie2Adapter.MovieViewHolder> {

    private Context context;
    private boolean isAdmin;
    private List<Movie> movieList;

    public Movie2Adapter(Context context, List<Movie> movieList, boolean isAdmin) {
        this.context = context;
        this.movieList = movieList;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.movie2_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.title.setText(movie.getTitle());
        holder.time.setText(movie.getTime());

        // Hiển thị thể loại phim
        List<String> genres = movie.getGenre();
        holder.genre.setText(genres != null && !genres.isEmpty()
                ? String.join(", ", genres)
                : "Không có thể loại"); // Chuỗi mặc định nếu thể loại trống

        // Sử dụng Picasso để tải hình ảnh poster
        Picasso.get().load(movie.getPoster()).into(holder.posterImage);

        // Thiết lập sự kiện nhấp vào item
        holder.itemView.setOnClickListener(v -> {
            Intent intent;

            if (isAdmin) {
                intent = new Intent(context, AdminMovieDetailActivity.class);
            }
            else {
                intent = new Intent(context, MovieDetailActivity.class);
            }

            // Truyền movieId qua Intent
            intent.putExtra("movieId", movie.getMovieID());

            // Khởi chạy MovieDetailActivity
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
