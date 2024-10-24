package com.example.moviemate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.models.Cinema;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CinemaAdapter extends RecyclerView.Adapter<CinemaAdapter.CinemaViewHolder> {

    private Context context;
    private List<Cinema> cinemaList;

    public CinemaAdapter(Context context, List<Cinema> cinemaList) {
        this.context = context;
        this.cinemaList = cinemaList;
    }

    @NonNull
    @Override
    public CinemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cinema_item, parent, false);
        return new CinemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CinemaViewHolder holder, int position) {
        Cinema cinema = cinemaList.get(position);
        holder.cinemaName.setText(cinema.getCinemaName());
        holder.cinemaAddress.setText(cinema.getAddress());
        //holder.cinemaDistance.setText(cinema.getDistance());
        Picasso.get().load(cinema.getBrandLogo()).into(holder.cinemaLogo);  // Load cinema logo
    }

    @Override
    public int getItemCount() {
        return (cinemaList != null) ? cinemaList.size() : 0;
    }

    public static class CinemaViewHolder extends RecyclerView.ViewHolder {
        ImageView cinemaLogo;
        TextView cinemaName, cinemaAddress, cinemaDistance;

        public CinemaViewHolder(@NonNull View itemView) {
            super(itemView);
            cinemaLogo = itemView.findViewById(R.id.cinema_logo);
            cinemaName = itemView.findViewById(R.id.cinema_name);
            cinemaAddress = itemView.findViewById(R.id.cinema_address);
            cinemaDistance = itemView.findViewById(R.id.cinema_distance);
        }
    }
}
