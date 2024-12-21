package com.example.moviemate.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moviemate.R;
import com.example.moviemate.models.Ticket;
import com.example.moviemate.utils.MoneyFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TicketDetailActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private ImageView moviePoster, barcodeImage;
    private TextView movieTitle, movieDuration, movieGenre, showTime, seatInfo, price, cinemaLocation, cinemaAddress, orderId;
    private DatabaseReference userTicketsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        // Khởi tạo các view
        backBtn = findViewById(R.id.BackBtn);
        moviePoster = findViewById(R.id.detail_movie_poster);
        movieTitle = findViewById(R.id.detail_movie_title);
        movieDuration = findViewById(R.id.detail_movie_duration);
        movieGenre = findViewById(R.id.detail_movie_genre);
        showTime = findViewById(R.id.detail_show_time);
        seatInfo = findViewById(R.id.detail_seat_info);
        price = findViewById(R.id.detail_price);
        cinemaLocation = findViewById(R.id.detail_cinema_location);
        cinemaAddress = findViewById(R.id.detail_cinema_address);
        barcodeImage = findViewById(R.id.detail_barcode);
        orderId = findViewById(R.id.detail_order_id);

        // Lấy ticketID từ intent
        String ticketID = getIntent().getStringExtra("ticketID");

        if (ticketID != null) {
            loadTicketDetails(ticketID);
        } else {
            Toast.makeText(this, "Ticket ID không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
        }

        backBtn.setOnClickListener(v -> finish());
    }

    private void loadTicketDetails(String ticketID) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userTicketsRef = FirebaseDatabase.getInstance().getReference("Tickets").child(userId).child(ticketID);

        userTicketsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Ticket ticket = snapshot.getValue(Ticket.class);
                if (ticket != null) {
                    // Hiển thị các thông tin chính từ ticket
                    showTime.setText(ticket.getTime() + "\n" + ticket.getDate());

                    // Xử lý ghế ngồi dưới dạng danh sách
                    List<String> seats = ticket.getSeats();
                    if (seats != null && !seats.isEmpty()) {
                        String seatsString = "Seats: " + String.join(", ", ticket.getSeats());
                        seatInfo.setText(seatsString);
                    }
                    else {
                        seatInfo.setText("Seats: N/A");
                    }

                    String priceFormatted = MoneyFormatter.formatMoney(TicketDetailActivity.this, ticket.getTotalPrice());
                    price.setText(String.format("%s VND", priceFormatted));
                    orderId.setText(String.format("Ticket ID: %s", ticket.getTicketID()));

                    // Tải thông tin bổ sung từ rạp và phim
                    loadCinemaDetails(ticket.getCinema());
                    loadMovieDetails(ticket.getMovie());

                    // Tải hình ảnh mã vạch nếu có
                    if (ticket.getBarcodeImage() != null) {
                        Picasso.get().load(ticket.getBarcodeImage()).into(barcodeImage);
                    }
                } else {
                    Toast.makeText(TicketDetailActivity.this, "Không tìm thấy vé", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketDetailActivity.this, "Lỗi khi tải vé", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCinemaDetails(String cinemaID) {
        DatabaseReference cinemaRef = FirebaseDatabase.getInstance().getReference("Cinemas").child(cinemaID);
        cinemaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String cinemaName = snapshot.child("CinemaName").getValue(String.class);
                    String cinemaAddr = snapshot.child("Address").getValue(String.class);

                    cinemaLocation.setText(cinemaName != null ? cinemaName : "N/A");
                    cinemaAddress.setText(cinemaAddr != null ? cinemaAddr : "N/A");
                } else {
                    cinemaLocation.setText("N/A");
                    cinemaAddress.setText("N/A");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketDetailActivity.this, "Lỗi khi tải thông tin rạp", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMovieDetails(String movieID) {
        DatabaseReference movieRef = FirebaseDatabase.getInstance().getReference("Movies").child(movieID);
        movieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String title = snapshot.child("Title").getValue(String.class);
                    String posterUrl = snapshot.child("Poster").getValue(String.class);
                    String duration = snapshot.child("Time").getValue(String.class);
                    List<String> genres = (List<String>) snapshot.child("Genre").getValue();

                    movieTitle.setText(title != null ? title : "N/A");
                    movieDuration.setText(duration != null ? duration : "N/A");

                    // Hiển thị thể loại phim
                    if (genres != null && !genres.isEmpty()) {
                        movieGenre.setText(String.join(", ", genres));
                    } else {
                        movieGenre.setText("N/A");
                    }

                    // Load hình ảnh poster
                    if (posterUrl != null) {
                        Picasso.get().load(posterUrl).into(moviePoster);
                    }
                } else {
                    movieTitle.setText("N/A");
                    movieDuration.setText("N/A");
                    movieGenre.setText("N/A");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketDetailActivity.this, "Lỗi khi tải thông tin phim", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
