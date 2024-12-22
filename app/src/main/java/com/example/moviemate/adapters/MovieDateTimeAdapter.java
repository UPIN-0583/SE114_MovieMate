package com.example.moviemate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.models.MovieDateTime;

import java.util.List;

public class MovieDateTimeAdapter extends RecyclerView.Adapter<MovieDateTimeAdapter.MovieDateTimeViewHolder> {
    private Context context;
    private List<MovieDateTime> movieDateTimeList;

    public MovieDateTimeAdapter(Context context, List<MovieDateTime> movieDateTimeList) {
        this.context = context;
        this.movieDateTimeList = movieDateTimeList;
    }

    @NonNull
    @Override
    public MovieDateTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.day_time_item, parent, false);

        return new MovieDateTimeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieDateTimeViewHolder holder, int position) {
        MovieDateTime dateTime = movieDateTimeList.get(position);

        holder.date.setText(dateTime.date);
        holder.time.setText(dateTime.time);

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movieDateTimeList.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieDateTimeList == null ? 0 : movieDateTimeList.size();
    }

    public class MovieDateTimeViewHolder extends RecyclerView.ViewHolder {
        TextView date, time;
        ImageButton removeButton;

        public MovieDateTimeViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.dateTextView);
            time = itemView.findViewById(R.id.timeTextView);
            removeButton = itemView.findViewById(R.id.removeImageButton);
        }
    }
}
