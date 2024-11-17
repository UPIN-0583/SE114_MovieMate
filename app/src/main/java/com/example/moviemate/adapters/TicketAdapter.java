package com.example.moviemate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.moviemate.R;
import com.example.moviemate.activities.TicketDetailActivity;
import com.example.moviemate.models.Ticket;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private Context context;
    private List<Ticket> ticketList;

    public TicketAdapter(Context context, List<Ticket> ticketList) {
        this.context = context;
        this.ticketList = ticketList;
    }


    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ticket_item, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);

        // Hiển thị tên phim
        holder.movieTitle.setText(ticket.getMovieTitle());

        // Hiển thị thời gian chiếu (giờ và ngày)
        holder.showTime.setText(ticket.getTime() + " • " + ticket.getDate());

        // Hiển thị tên rạp
        holder.cinemaLocation.setText(ticket.getCinemaLocation());

        // Hiển thị poster phim
        Picasso.get().load(ticket.getMoviePoster()).into(holder.moviePoster);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TicketDetailActivity.class);
            intent.putExtra("ticketID", ticket.getTicketID());// Chuyển dữ liệu vé qua Intent
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView movieTitle, showTime, cinemaLocation;
        ImageView moviePoster;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            movieTitle = itemView.findViewById(R.id.movie_title);
            showTime = itemView.findViewById(R.id.show_time);
            cinemaLocation = itemView.findViewById(R.id.cinema_location);
            moviePoster = itemView.findViewById(R.id.movie_poster);
        }
    }
}
