package com.example.moviemate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.adapters.DayAdapter;
import com.example.moviemate.adapters.TimeAdapter;
import com.example.moviemate.models.Movie;
import com.example.moviemate.utils.CustomDialog;
import com.example.moviemate.utils.MoneyFormatter;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SelectSeatActivity extends AppCompatActivity {
    private int totalTimeLeft = 1200000; // Tổng thời gian đặt ghế, thanh toán là 20 phút
    private Timer timer;
    private ImageButton backBtn;
    private RecyclerView dateRecyclerView, timeRecyclerView;
    private GridLayout seatGridLayout;
    private TextView totalPriceTextView;
    private TextView timeLeftTextView;
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
    private Movie movie;
    private String cinemaID;
    private String cinemaName;

    private ActivityResultLauncher<Intent> launcher; // Launcher để gọi PaymentActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_seat);

        Intent data = getIntent();
        // Nhận cinemaID, cinemaName và movie từ Intent
        cinemaID = "Cinema" + data.getIntExtra("cinema_id", -1);
        cinemaName = data.getStringExtra("cinema_name");
        movie = (Movie) data.getSerializableExtra("movie");

        // Ánh xạ các view
        backBtn = findViewById(R.id.BackBtn);
        dateRecyclerView = findViewById(R.id.date_recycler_view);
        timeRecyclerView = findViewById(R.id.time_recycler_view);
        seatGridLayout = findViewById(R.id.seat_grid_layout);
        totalPriceTextView = findViewById(R.id.total_price);
        timeLeftTextView = findViewById(R.id.time_left);
        buyTicketButton = findViewById(R.id.buy_ticket_button);

        dateRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        timeRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        userTicketsRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Tickets");

        fetchShowTimesFromFirebase();

        // Khởi tạo launcher để gọi PaymentActivity
        initLauncher();

        // Bắt đầu đếm ngược thời gian
        startTimer();

        // Sự kiện khi nhấn nút "Quay lại"
        setupBackEvent();

        // Sự kiện khi nhấn nút "Mua vé"
        buyTicketButton.setOnClickListener(v -> {
            if (selectedDate == null || selectedTime == null || selectedSeats.isEmpty()) {
                Toast.makeText(this, "Please select at least one date and one seat.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, PaymentActivity.class);
                intent.putExtra("totalPrice", MoneyFormatter.formatMoney(this, totalPrice));
                intent.putExtra("cinemaName", cinemaName);
                intent.putExtra("movie", movie);
                intent.putExtra("selectedDate", selectedDate);
                intent.putExtra("selectedTime", selectedTime);
                String seats = String.join(",", selectedSeats);
                intent.putExtra("selectedSeats", seats);
                intent.putExtra("timeLeft", totalTimeLeft);
                launcher.launch(intent);
            }
        });
    }

    private void setupBackEvent() {
        backBtn.setOnClickListener(v -> {
            cancelHolding();
            finish();
        });

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                cancelHolding();
                finish();
            }
        });
    }

    private void initLauncher() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_CANCELED) return;

                    saveTicketToFirebase();
                    Intent intent = new Intent(this, MainActivity.class);
                    // Xóa các Activity khác và tạo mới MainActivity
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
        );
    }

    private void fetchShowTimesFromFirebase() {
        cinemaRef = FirebaseDatabase.getInstance().getReference("Cinemas")
                .child(cinemaID)
                .child("Movies")
                .child("Movie" + movie.getMovieID())
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
                Toast.makeText(SelectSeatActivity.this, "Failed to fetch showtime from Firebase", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SelectSeatActivity.this, "Failed to fetch date from Firebase", Toast.LENGTH_SHORT).show();
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
        cinemaRef.child(date).child(time).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seatMap = new LinkedHashMap<>(); // Giữ nguyên thứ tự ghế khi trả về từ Firebase
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
                Toast.makeText(SelectSeatActivity.this, "Failed to fetch seats from Firebase", Toast.LENGTH_SHORT).show();
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

                if (selectedSeats.contains(seat)) {
                    seatTextView.setSelected(true);
                    seatTextView.setBackgroundResource(R.color.selected_seat);
                } else {
                    seatTextView.setSelected(false);
                    seatTextView.setBackgroundResource(status.equals("available") ? R.color.available_seat : R.color.reserved_seat);
                }
                seatTextView.setTextColor(getResources().getColor(android.R.color.white));
                seatTextView.setOnClickListener(v -> {
                    if (status.equals("available") || selectedSeats.contains(seat)) {
                        toggleSeatSelection(seatTextView, seat);
                    }
                    else {
                        Toast.makeText(this, "The seat has been reserved.", Toast.LENGTH_SHORT).show();
                    }
                });

                seatGridLayout.addView(seatTextView);
            }
        } else {
            Toast.makeText(this, "No seat available", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleSeatSelection(TextView seatTextView, String seat) {
        DatabaseReference seatRef = FirebaseDatabase.getInstance().getReference("Cinemas")
                .child(cinemaID)
                .child("Movies")
                .child("Movie" + movie.getMovieID())
                .child("ShowTimes")
                .child(selectedDate)
                .child(selectedTime)
                .child("Seats")
                .child(seat);

        if (seatTextView.isSelected()) {
            seatTextView.setSelected(false);
            seatTextView.setBackgroundResource(R.color.available_seat);
            totalPrice -= seatPrice;
            selectedSeats.remove(seat);
            seatMap.put(seat, "available");
            seatRef.setValue("available");
        } else {
            seatTextView.setSelected(true);
            seatTextView.setBackgroundResource(R.color.selected_seat);
            totalPrice += seatPrice;
            selectedSeats.add(seat);
            seatMap.put(seat, "selected");
            seatRef.setValue("booked");
        }

        totalPriceTextView.setText(String.format(Locale.getDefault(), "Total: %s VND", MoneyFormatter.formatMoney(this, totalPrice)));
    }

    private void saveTicketToFirebase() {
        String ticketId = userTicketsRef.push().getKey(); // Sử dụng TicketID làm mã định danh duy nhất cho vé
        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("cinema", cinemaID);
        ticketData.put("movie", "Movie" + movie.getMovieID());
        ticketData.put("date", selectedDate);
        ticketData.put("time", selectedTime);
        ticketData.put("seats", selectedSeats);
        ticketData.put("totalPrice", totalPrice);
        ticketData.put("TicketID", ticketId);  // Lưu TicketID

        Bitmap barcodeBitmap = createBarcode(ticketId);  // Tạo mã vạch từ TicketID
        if (barcodeBitmap != null) {
            uploadBarcodeToFirebase(barcodeBitmap, ticketId, ticketData);
        } else {
            Toast.makeText(this, "Failed to create bar code", Toast.LENGTH_SHORT).show();
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
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save ticket. Please contact support", Toast.LENGTH_SHORT).show());
        })).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load bar code", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateSeatStatus() {
        DatabaseReference seatsRef = FirebaseDatabase.getInstance().getReference("Cinemas")
                .child(cinemaID)
                .child("Movies")
                .child("Movie" + movie.getMovieID())
                .child("ShowTimes")
                .child(selectedDate)
                .child(selectedTime)
                .child("Seats");

        for (String seat : selectedSeats) {
            seatsRef.child(seat).setValue("booked")
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update seat status", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Huỷ giữ chỗ khi bấm back hoặc hết thời gian
    private void cancelHolding() {
        DatabaseReference seatRef = FirebaseDatabase.getInstance().getReference("Cinemas")
                .child(cinemaID)
                .child("Movies")
                .child("Movie" + movie.getMovieID())
                .child("ShowTimes")
                .child(selectedDate)
                .child(selectedTime)
                .child("Seats");

        for (String seat : selectedSeats) {
            seatRef.child(seat).setValue("available")
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to restore seat status", Toast.LENGTH_SHORT).show();
                    });
        }

        timer.cancel();
    }

    // Countdown timer
    private void startTimer() {
        timer = new Timer();
        timer.schedule(new SelectSeatCountdown(), 0, 1000);
    }

    public class SelectSeatCountdown extends TimerTask {
        @Override
        public void run() {
            totalTimeLeft -= 1000; // Giảm thời gian còn lại mỗi giây
            totalTimeLeft = Math.max(totalTimeLeft, 0); // Đảm bảo thời gian còn lại không âm
            final int minutes = Math.max((totalTimeLeft / 1000) / 60, 0);
            final int seconds = Math.max((totalTimeLeft / 1000) % 60, 0);

            runOnUiThread(() -> {
                timeLeftTextView.setText(String.format(Locale.getDefault(), " %02d:%02d", minutes, seconds));
            });

            if (totalTimeLeft <= 0) {
                handleTimeout();
            }
        }
    }

    private void handleTimeout() {
        runOnUiThread(() -> {
            cancelHolding();
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Time's up!", "Your seat reservation has been cancelled.", true);
        });
    }
}
