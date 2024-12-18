package com.example.moviemate.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.moviemate.R;
import com.example.moviemate.adapters.TicketAdapter;
import com.example.moviemate.models.Ticket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TicketFragment extends Fragment {

    private RecyclerView ticketRecyclerView;
    private TicketAdapter ticketAdapter;
    private List<Ticket> ticketList;
    private DatabaseReference userTicketsRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ticket, container, false);

        // Initialize RecyclerView
        ticketRecyclerView = view.findViewById(R.id.ticket_recycler_view);
        ticketRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the list and adapter
        ticketList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(getContext(), ticketList);
        ticketRecyclerView.setAdapter(ticketAdapter);

        // Load tickets from Firebase
        loadUserTickets();

        return view;
    }



    private void loadUserTickets() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userTicketsRef = FirebaseDatabase.getInstance().getReference("Tickets").child(userId);

        userTicketsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ticketList.clear();
                for (DataSnapshot ticketSnapshot : snapshot.getChildren()) {
                    Ticket ticket = ticketSnapshot.getValue(Ticket.class);
                    if (ticket != null) {
                        // Tham chiếu thêm thông tin chi tiết của cinema và movie
                        loadCinemaAndMovieDetails(ticket);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải danh sách vé", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCinemaAndMovieDetails(Ticket ticket) {
        // Lấy thông tin cinema từ id_cinema
        DatabaseReference cinemaRef = FirebaseDatabase.getInstance().getReference("Cinemas").child(ticket.getCinema());
        cinemaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String cinemaName = snapshot.child("CinemaName").getValue(String.class);
                    ticket.setCinemaLocation(cinemaName);
                }

                // Sau khi lấy xong cinema, lấy tiếp thông tin movie
                loadMovieDetails(ticket);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải thông tin rạp", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMovieDetails(Ticket ticket) {
        DatabaseReference movieRef = FirebaseDatabase.getInstance().getReference("Movies").child(ticket.getMovie());
        movieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String movieTitle = snapshot.child("Title").getValue(String.class);
                    String moviePoster = snapshot.child("Poster").getValue(String.class);

                    ticket.setMovieTitle(movieTitle);
                    ticket.setMoviePoster(moviePoster);
                }

                // Sau khi lấy đủ thông tin, thêm vào danh sách và cập nhật giao diện
                ticketList.add(ticket);
                ticketAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải thông tin phim", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
