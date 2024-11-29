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

import java.util.List;

public class MovieSearchAdapter extends RecyclerView.Adapter<MovieSearchAdapter.MovieViewHolder> {

    private Context context;
    private List<Movie> movieList;

    public MovieSearchAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.movie_item_search, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.title.setText(movie.getTitle());
        List<String> genres = movie.getGenre();

        if (genres != null && !genres.isEmpty()) {
            holder.genre.setText(TextUtils.join(", ", genres));
        } else {
            holder.genre.setText("Không có thể loại"); // Chuỗi mặc định nếu thể loại trống
        }

        holder.time.setText(movie.getTime());

        // Sử dụng Picasso để tải hình ảnh phim
        Picasso.get().load(movie.getPoster()).into(holder.image);

        // Xử lý khi người dùng nhấp vào một bộ phim
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);

            // Truyền movie qua Intent thay vì truyền movie_id
            intent.putExtra("movie", movie);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, genre, time;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.movie_image);
            title = itemView.findViewById(R.id.movie_title);
            genre = itemView.findViewById(R.id.movie_genre);
            time = itemView.findViewById(R.id.movie_time);
        }
    }
}
