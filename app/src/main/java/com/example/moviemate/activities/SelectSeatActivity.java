package com.example.moviemate.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.adapters.DayTimeAdapter;
import com.example.moviemate.models.ShowTime;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectSeatActivity extends AppCompatActivity {

    private RecyclerView dateRecyclerView, timeRecyclerView;
    private GridLayout seatGridLayout;
    private TextView totalPriceTextView;
    private Button buyTicketButton;

    private DayTimeAdapter dateAdapter, timeAdapter;
    private List<String> dateList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();

    private String selectedDate;
    private String selectedTime;
    private int totalPrice = 0;
    private Map<String, String> seatMap; // Giữ trạng thái các ghế

    private DatabaseReference cinemaRef;
    private String cinemaID;
    private int movieID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_seat);

        // Nhận cinemaID và movieID từ Intent
        cinemaID = "Cinema" + getIntent().getIntExtra("cinema_id", -1);
        movieID = getIntent().getIntExtra("movie_id", -1);

        // Ánh xạ các view
        dateRecyclerView = findViewById(R.id.date_recycler_view);
        timeRecyclerView = findViewById(R.id.time_recycler_view);
        seatGridLayout = findViewById(R.id.seat_grid_layout);
        totalPriceTextView = findViewById(R.id.total_price);
        buyTicketButton = findViewById(R.id.buy_ticket_button);

        // Thiết lập RecyclerView cho ngày và giờ
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        timeRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        fetchShowTimesFromFirebase();

        // Sự kiện khi nhấn nút "Mua vé"
        buyTicketButton.setOnClickListener(v -> {
            if (selectedDate == null || selectedTime == null) {
                Toast.makeText(this, "Hãy chọn ngày và giờ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đặt vé thành công! Tổng cộng: " + totalPrice + " VND", Toast.LENGTH_SHORT).show();
                // Xử lý thanh toán tại đây
            }
        });
    }

    private void fetchShowTimesFromFirebase() {
        cinemaRef = FirebaseDatabase.getInstance().getReference("Cinemas")
                .child(cinemaID) // Đảm bảo cinemaID được truyền đúng
                .child("Movies")
                .child("Movie" + movieID)
                .child("ShowTimes");

        cinemaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dateList.clear();
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    if (date != null) {
                        dateList.add(date); // Thêm từng ngày vào danh sách
                    }
                }
                setupDateAdapter(); // Gọi cập nhật adapter sau khi lấy dữ liệu xong
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectSeatActivity.this, "Lỗi khi lấy giờ chiếu từ Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDateAdapter() {
        dateAdapter = new DayTimeAdapter(this, dateList, date -> {
            selectedDate = date;
            fetchTimesForSelectedDate(date);
        });
        dateRecyclerView.setAdapter(dateAdapter);
        dateAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
    }


    private void fetchTimesForSelectedDate(String date) {
        cinemaRef.child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                timeList.clear();
                for (DataSnapshot timeSnapshot : snapshot.getChildren()) {
                    String time = timeSnapshot.getKey();
                    if (time != null) {
                        timeList.add(time); // Lưu giờ vào danh sách
                    }
                }
                setupTimeAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectSeatActivity.this, "Lỗi khi lấy giờ chiếu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTimeAdapter() {
        timeAdapter = new DayTimeAdapter(this, timeList, time -> {
            selectedTime = time;
            fetchSeatsForSelectedTime(selectedDate, time);
        });
        timeRecyclerView.setAdapter(timeAdapter);
    }

    private void fetchSeatsForSelectedTime(String date, String time) {
        cinemaRef.child(date).child(time).child("Seats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seatMap = new HashMap<>();  // Khởi tạo lại seatMap để đảm bảo nó không null
                for (DataSnapshot seatSnapshot : snapshot.getChildren()) {
                    String seatKey = seatSnapshot.getKey();
                    String seatStatus = seatSnapshot.getValue(String.class);
                    if (seatKey != null && seatStatus != null) {
                        seatMap.put(seatKey, seatStatus);
                    }
                }
                setupSeatGrid();  // Hiển thị ghế sau khi đã lấy dữ liệu từ Firebase
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectSeatActivity.this, "Lỗi khi lấy thông tin ghế", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSeatGrid() {
        // Kiểm tra nếu seatMap không phải là null
        if (seatMap != null && !seatMap.isEmpty()) {
            seatGridLayout.removeAllViews();
            for (Map.Entry<String, String> entry : seatMap.entrySet()) {
                String seat = entry.getKey();
                String status = entry.getValue();

                TextView seatTextView = new TextView(this);
                seatTextView.setText(seat);
                seatTextView.setPadding(16, 16, 16, 16);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setMargins(16, 16, 16, 16);  // Đặt margin cho TextView
                seatTextView.setLayoutParams(params);

                seatTextView.setBackgroundResource(status.equals("available") ? R.color.available_seat : R.color.reserved_seat);
                seatTextView.setTextColor(getResources().getColor(android.R.color.white));
                seatTextView.setOnClickListener(v -> {
                    if (status.equals("available")) {
                        toggleSeatSelection(seatTextView, seat);
                    } else {
                        Toast.makeText(this, "Ghế này đã được đặt", Toast.LENGTH_SHORT).show();
                    }
                });

                seatGridLayout.addView(seatTextView);
            }
        } else {
            // Xử lý khi seatMap là null hoặc không có ghế nào
            Toast.makeText(this, "Không có thông tin ghế khả dụng", Toast.LENGTH_SHORT).show();
        }
    }


    private void toggleSeatSelection(TextView seatTextView, String seat) {
        if (seatTextView.isSelected()) {
            seatTextView.setSelected(false);
            seatTextView.setBackgroundResource(R.color.available_seat);
            totalPrice -= 210000;
        } else {
            seatTextView.setSelected(true);
            seatTextView.setBackgroundResource(R.color.selected_seat);
            totalPrice += 210000;
        }
        totalPriceTextView.setText("Total: " + totalPrice + " VND");
    }
}
