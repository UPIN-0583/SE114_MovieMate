package com.example.moviemate.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.adapters.DayAdapter;
import com.example.moviemate.adapters.TimeAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.zxing.BarcodeFormat;

import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectSeatActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private RecyclerView dateRecyclerView, timeRecyclerView;
    private GridLayout seatGridLayout;
    private TextView totalPriceTextView;
    private Button buyTicketButton;

    private DayAdapter dayAdapter;
    private TimeAdapter timeAdapter;
    private List<String> dateList = new ArrayList<>();
    private List<String> timeList = new ArrayList<>();
    private List<String> selectedSeats = new ArrayList<>(); // Danh sách ghế đã chọn

    private String selectedDate;
    private String selectedTime;
    private int totalPrice = 0;
    private int seatPrice = 0; // Giá vé

    private Map<String, String> seatMap;
    private DatabaseReference cinemaRef;
    private DatabaseReference userTicketsRef;
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
        backBtn = findViewById(R.id.BackBtn);
        dateRecyclerView = findViewById(R.id.date_recycler_view);
        timeRecyclerView = findViewById(R.id.time_recycler_view);
        seatGridLayout = findViewById(R.id.seat_grid_layout);
        totalPriceTextView = findViewById(R.id.total_price);
        buyTicketButton = findViewById(R.id.buy_ticket_button);

        dateRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        timeRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        userTicketsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Tickets");

        fetchShowTimesFromFirebase();

        backBtn.setOnClickListener(v -> finish());
        // Sự kiện khi nhấn nút "Mua vé"
        buyTicketButton.setOnClickListener(v -> {
            if (selectedDate == null || selectedTime == null || selectedSeats.isEmpty()) {
                Toast.makeText(this, "Hãy chọn ngày, giờ và ít nhất một ghế", Toast.LENGTH_SHORT).show();
            } else {
                saveTicketToFirebase();
            }
        });
    }

    private void fetchShowTimesFromFirebase() {
        cinemaRef = FirebaseDatabase.getInstance().getReference("Cinemas")
                .child(cinemaID)
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
                setupDateAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectSeatActivity.this, "Lỗi khi lấy giờ chiếu từ Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDateAdapter() {
        dayAdapter = new DayAdapter(this, dateList, date -> {
            selectedDate = date;
            fetchTimesForSelectedDate(date);
        });
        dateRecyclerView.setAdapter(dayAdapter);
        dayAdapter.notifyDataSetChanged();
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
        timeAdapter = new TimeAdapter(this, timeList, time -> {
            selectedTime = time;
            fetchSeatsForSelectedTime(selectedDate, time);
        });
        timeRecyclerView.setAdapter(timeAdapter);
    }

    private void fetchSeatsForSelectedTime(String date, String time) {
        cinemaRef.child(date).child(time).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seatMap = new HashMap<>();
                Integer priceFromDb = snapshot.child("Price").getValue(Integer.class);
                if (priceFromDb != null) {
                    seatPrice = priceFromDb;
                }

                for (DataSnapshot seatSnapshot : snapshot.child("Seats").getChildren()) {
                    String seatKey = seatSnapshot.getKey();
                    String seatStatus = seatSnapshot.getValue(String.class);
                    if (seatKey != null && seatStatus != null) {
                        seatMap.put(seatKey, seatStatus);
                    }
                }
                setupSeatGrid();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectSeatActivity.this, "Lỗi khi lấy thông tin ghế", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSeatGrid() {
        if (seatMap != null && !seatMap.isEmpty()) {
            seatGridLayout.removeAllViews();
            for (Map.Entry<String, String> entry : seatMap.entrySet()) {
                String seat = entry.getKey();
                String status = entry.getValue();

                TextView seatTextView = new TextView(this);
                seatTextView.setText(seat);
                seatTextView.setPadding(16, 16, 16, 16);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setMargins(16, 16, 16, 16);
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
            Toast.makeText(this, "Không có thông tin ghế khả dụng", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleSeatSelection(TextView seatTextView, String seat) {
        if (seatTextView.isSelected()) {
            seatTextView.setSelected(false);
            seatTextView.setBackgroundResource(R.color.available_seat);
            totalPrice -= seatPrice;
            selectedSeats.remove(seat);
            seatMap.put(seat, "available");
        } else {
            seatTextView.setSelected(true);
            seatTextView.setBackgroundResource(R.color.selected_seat);
            totalPrice += seatPrice;
            selectedSeats.add(seat);
            seatMap.put(seat, "selected");
        }
        totalPriceTextView.setText("Total: " + totalPrice + " VND");
    }


    private void saveTicketToFirebase() {
        String ticketId = userTicketsRef.push().getKey(); // Sử dụng TicketID làm mã định danh duy nhất cho vé
        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("cinema", cinemaID);
        ticketData.put("movie", "Movie" + movieID);
        ticketData.put("date", selectedDate);
        ticketData.put("time", selectedTime);
        ticketData.put("seats", selectedSeats);
        ticketData.put("totalPrice", totalPrice);
        ticketData.put("TicketID", ticketId);  // Lưu TicketID

        Bitmap barcodeBitmap = createBarcode(ticketId);  // Tạo mã vạch từ TicketID
        if (barcodeBitmap != null) {
            uploadBarcodeToFirebase(barcodeBitmap, ticketId, ticketData);
        } else {
            Toast.makeText(this, "Không thể tạo mã vạch", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm tạo mã vạch từ TicketID
    private Bitmap createBarcode(String ticketId) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.encodeBitmap(ticketId, BarcodeFormat.CODE_128, 600, 200);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Hàm tải mã vạch lên Firebase Storage và lưu URL vào Database
    private void uploadBarcodeToFirebase(Bitmap barcodeBitmap, String ticketId, Map<String, Object> ticketData) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("barcodes/" + ticketId + ".png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        barcodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            ticketData.put("BarcodeImage", uri.toString());  // Lưu URL mã vạch vào dữ liệu vé

            // Lưu dữ liệu vé vào Firebase Realtime Database
            userTicketsRef.child(ticketId).setValue(ticketData)
                    .addOnSuccessListener(aVoid -> {
                        updateSeatStatus();
                        Toast.makeText(this, "Đặt vé thành công! Tổng cộng: " + totalPrice + " VND", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi lưu vé", Toast.LENGTH_SHORT).show());
        })).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi khi tải mã vạch", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateSeatStatus() {
        DatabaseReference seatsRef = FirebaseDatabase.getInstance().getReference("Cinemas")
                .child(cinemaID)
                .child("Movies")
                .child("Movie" + movieID)
                .child("ShowTimes")
                .child(selectedDate)
                .child(selectedTime)
                .child("Seats");

        for (String seat : selectedSeats) {
            seatsRef.child(seat).setValue("booked")
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi cập nhật trạng thái ghế", Toast.LENGTH_SHORT).show();
                    });
        }
    }

}
