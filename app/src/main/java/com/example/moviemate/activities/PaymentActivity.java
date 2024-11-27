package com.example.moviemate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.moviemate.R;
import com.example.moviemate.adapters.PaymentMethodListAdapter;
import com.example.moviemate.api.PayOsApiService;
import com.example.moviemate.api.objects.payos.CreatePayLink;
import com.example.moviemate.api.objects.payos.CreatePayLinkResponse;
import com.example.moviemate.api.objects.payos.SimpleMovieDescription;
import com.example.moviemate.models.Movie;
import com.example.moviemate.models.PaymentMethod;
import com.example.moviemate.utils.CustomDialog;
import com.example.moviemate.utils.OrderIdGenerator;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import io.github.cdimascio.dotenv.Dotenv;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {
    private static final int PAYMENT_TIMEOUT = 600000; // 10 minutes

    private Timer paymentTimer;
    private int paymentTimeLeft = PAYMENT_TIMEOUT;
    private PaymentMethodListAdapter paymentMethodListAdapter;
    private Integer basePrice = null;
    private ActivityResultLauncher<Intent> launcher;

    // UI Components
    private ImageView backButton;
    private TextView movieTitleTextView;
    private TextView movieGenreTextView;
    private TextView cinemaNameTextView;
    private TextView movieTimeTextView;
    private ImageView moviePosterImageView;
    private TextView orderIdTextView;
    private TextView seatTextView;
    private EditText discountCodeEdit;
    private Button applyDiscountBtn;
    private TextView totalMoneyTextView;
    private ListView paymentMethodListView;
    private TextView paymentTimeLeftTextView;
    private Button payBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLayout();
        initializeViews();
        setupListeners();
        initPaymentMethods();
        startPaymentTimer();
        setData();

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_CANCELED) return;

                    Intent data = result.getData();
                    if (data == null) return;

                    String status = data.getStringExtra("status");
                    if (status == null) return;
                    if (status.equals("cancel")) {
                        cancelPayment("Thanh toán huỷ bởi người dùng");
                    } else if (status.equals("success")) {
                        successPayment();
                    }
                }
        );
    }

    private void setData() {
        Intent data = getIntent();

        Movie movie = (Movie) data.getSerializableExtra("movie");
        String cinemaName = data.getStringExtra("cinemaName");
        String date = data.getStringExtra("selectedDate");
        String time = data.getStringExtra("selectedTime");
        String seats = data.getStringExtra("selectedSeats");
        basePrice = data.getIntExtra("totalPrice", -1);
        if (movie == null) {
            finish();
            return;
        }

        Picasso.get().load(movie.getPoster()).into(moviePosterImageView);
        orderIdTextView.setText(String.valueOf(OrderIdGenerator.generateOrderId()));
        movieTitleTextView.setText(movie.getTitle());
        movieGenreTextView.setText(String.join(", ", movie.getGenre()));
        cinemaNameTextView.setText(cinemaName);
        movieTimeTextView.setText(String.format("%s %s", date, time));
        seatTextView.setText(seats);
        totalMoneyTextView.setText(formatMoney(basePrice));
    }

    private void setupLayout(){
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        setupEdgeToEdge();
    }
    private void setupEdgeToEdge(){
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void initializeViews() {
        backButton = findViewById(R.id.paymentBackBtn);
        movieTitleTextView = findViewById(R.id.movieTitleTextView);
        movieGenreTextView = findViewById(R.id.movieGenreTextView);
        cinemaNameTextView = findViewById(R.id.cinemaNameTextView);
        movieTimeTextView = findViewById(R.id.movieTimeTextView);
        moviePosterImageView = findViewById(R.id.moviePosterImageView);
        orderIdTextView = findViewById(R.id.orderIdTextView);
        seatTextView = findViewById(R.id.seatTextView);
        discountCodeEdit = findViewById(R.id.discountCodeEdit);
        applyDiscountBtn = findViewById(R.id.applyDiscountBtn);
        totalMoneyTextView = findViewById(R.id.totalMoneyTextView);
        paymentMethodListView = findViewById(R.id.paymentMethodListView);
        paymentTimeLeftTextView = findViewById(R.id.paymentTimeLeftTextView);
        payBtn = findViewById(R.id.payBtn);
    }
    private void setupListeners() {
        setupBackPress();
        applyDiscountBtn.setOnClickListener(v -> applyDiscountCode());
        payBtn.setOnClickListener(v -> pay());
    }
    private void setupBackPress() {
        backButton.setOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void startPaymentTimer() {
        paymentTimer = new Timer();
        paymentTimer.schedule(new PaymentCountdown(), 0, 1000);
    }
    private class PaymentCountdown extends TimerTask {
        @Override
        public void run() {
            paymentTimeLeft -= 1000;
            final int minutes = Math.max(paymentTimeLeft / 60000, 0);
            final int seconds = Math.max((paymentTimeLeft % 60000) / 1000, 0);

            runOnUiThread(() -> {
                if (paymentTimeLeftTextView != null) {
                    paymentTimeLeftTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                }
            });

            if (paymentTimeLeft <= 0) {
                handlePaymentTimeout();
            }
        }
    }
    private void handlePaymentTimeout() {
        runOnUiThread(() -> cancelPayment("Hết thời gian thanh toán"));
    }

    private void initPaymentMethods() {
        List<PaymentMethod> paymentMethods = List.of(
                new PaymentMethod(PaymentMethod.TYPE.QR_CODE, "QR Code", R.drawable.logo_qrcode)
        );

        paymentMethodListAdapter = new PaymentMethodListAdapter(this, R.layout.payment_method_item, paymentMethods);

        paymentMethodListView = findViewById(R.id.paymentMethodListView);
        paymentMethodListView.setAdapter(paymentMethodListAdapter);
    }
    private void pay() {
        PaymentMethod paymentMethod = paymentMethodListAdapter.getSelectedPaymentMethod();
        if (paymentMethod == null) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Please select a payment method", false);
            return;
        }

        payWithQRCode();
    }

    private void payWithQRCode() {
        final String CANCEL_URL = "http://cancelpayment.com";
        final String RETURN_URL = "http://successpayment.com";
        final String ORDER_ID_EXIST_ERR_CODE = "231";

        payBtn.setEnabled(false); // Disable pay button to prevent multiple payments
        int totalMoney = parseMoney(totalMoneyTextView.getText().toString());
        if (totalMoney == 0) {

            return;
        }

        int quantity = (int) (seatTextView.getText().toString().chars().filter(ch -> ch == ',').count() + 1);
        SimpleMovieDescription movieDescription = new SimpleMovieDescription(movieTitleTextView.getText().toString(), quantity, totalMoney);
        long expiredAt = (System.currentTimeMillis() / 1000) + (PAYMENT_TIMEOUT / 1000);

        int orderId = Integer.parseInt(orderIdTextView.getText().toString());
        CreatePayLink data = new CreatePayLink(orderId, totalMoney, "Movie ticket",
                List.of(movieDescription), CANCEL_URL, RETURN_URL, expiredAt);

        Dotenv dotenv = Dotenv.configure().directory("/assets").filename("env").load();
        PayOsApiService.payOsApiService.createPayLink(dotenv.get("PAYOS_CLIENT_ID"), dotenv.get("PAYOS_API_KEY"), data).enqueue(new Callback<CreatePayLinkResponse>() {
            @Override
            public void onResponse(Call<CreatePayLinkResponse> call, Response<CreatePayLinkResponse> response) {
                payBtn.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    cancelPayment("Tạo link thanh toán thất bại (Lỗi mạng)" );
                    return;
                }

                CreatePayLinkResponse dataResponse = response.body();

                if (dataResponse.getCode().equals(ORDER_ID_EXIST_ERR_CODE)) {
                    cancelPayment("Tạo link thanh toán thất bại (Order ID đã tồn tại)");
                    return;
                }
                else if (!dataResponse.getCode().equals("00")) {
                    cancelPayment("Tạo link thanh toán thất bại (Lỗi PayOS API)");
                    return;
                }

                String paymentLink = dataResponse.getData().getCheckoutUrl();
                Intent intent = new Intent(PaymentActivity.this, PayOsActivity.class);
                intent.putExtra("checkoutUrl", paymentLink);
                intent.putExtra("cancelUrl", CANCEL_URL);
                intent.putExtra("successUrl", RETURN_URL);
                launcher.launch(intent);
            }

            @Override
            public void onFailure(Call<CreatePayLinkResponse> call, Throwable throwable) {
                cancelPayment("Tạo link thanh toán thất bại (Lỗi mạng)");
            }
        });

    }

    private void cancelPayment(String reason) {
        if (paymentTimer != null) {
            paymentTimer.cancel();
        }

        CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Thông báo", "Huỷ thanh toán do: " + reason, true);
    }

    private void successPayment() {
        if (paymentTimer != null) {
            paymentTimer.cancel();
        }

        CustomDialog.showAlertDialog(this, R.drawable.ic_success, "Thành công", "Thanh toán thành công", true);
    }

    private void applyDiscountCode() {
        String discountCode = discountCodeEdit.getText().toString().trim();
        if (!validateDiscountCode(discountCode)) {
            return;
        }

        int totalMoney = calculateDiscountedAmount();
        totalMoneyTextView.setText(formatMoney(totalMoney));

        CustomDialog.showAlertDialog(this, R.drawable.ic_success, "Success", "Discount code applied successfully", false);
    }
    private boolean validateDiscountCode(String discountCode) {
        if (discountCode.isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Please enter discount code", false);
            return false;
        }

        return true;
    }
    private int calculateDiscountedAmount() {
        int totalMoney = parseMoney(totalMoneyTextView.getText().toString());

        // Reset to base price if discount already applied
        if (basePrice != null && totalMoney != basePrice) {
            totalMoney = basePrice;
        }

        int discountAmount = 1;
        // Calculate discount amount here

        totalMoney -= discountAmount;
        return totalMoney = Math.max(totalMoney, 0); // Never show negative money
    }
    private int parseMoney(String moneyString) {
        NumberFormat formatter = NumberFormat.getInstance();

        try {
            Number number = formatter.parse(moneyString);
            return Objects.requireNonNull(number).intValue();
        } catch (Exception e) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Invalid money format", false);
            return basePrice;
        }
    }
    private String formatMoney(int money) {
        NumberFormat formatter = NumberFormat.getInstance();

        try {
            return formatter.format(money);
        } catch (Exception e) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Invalid money format", false);
            return formatter.format(basePrice);
        }
    }
}