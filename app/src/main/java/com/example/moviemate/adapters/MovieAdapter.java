package com.example.moviemate.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
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

import java.io.Serializable;
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
        if (genres != null && !genres.isEmpty()) {
            holder.genre.setText(TextUtils.join(", ", genres));
        } else {
            holder.genre.setText("Không có thể loại"); // Chuỗi mặc định nếu thể loại trống
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra("title", movie.getTitle());
            intent.putExtra("genre", genres != null ? String.join(", ", genres) : "Không có thể loại");
            intent.putExtra("rating", movie.getRating());
            intent.putExtra("language", movie.getLanguage());
            intent.putExtra("time", movie.getTime());
            intent.putExtra("description", movie.getDescription());
            intent.putExtra("poster", movie.getPoster());

            // Truyền danh sách actors và directors qua Intent
            intent.putExtra("directors", (Serializable) movie.getDirector());
            intent.putExtra("actors", (Serializable) movie.getActor());

            intent.putExtra("trailerUrl", movie.getTrailer());

            if (movie.getCinemas() != null && !movie.getCinemas().isEmpty()) {
                intent.putExtra("cinemas", (Serializable) movie.getCinemas());
                intent.putExtra("isNowPlaying", true);
            } else {
                intent.putExtra("isNowPlaying", false);
            }

            context.startActivity(intent);
        });
    }

        @Override
    public int getItemCount() {
        return movieList.size();
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
