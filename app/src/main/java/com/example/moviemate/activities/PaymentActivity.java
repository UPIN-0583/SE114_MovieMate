package com.example.moviemate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.moviemate.utils.MoneyFormatter;
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
    private Timer paymentTimer;
    private int paymentTimeLeft;
    private PaymentMethodListAdapter paymentMethodListAdapter;
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
        setData();
        startPaymentTimer();

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
        String totalPrice = data.getStringExtra("totalPrice");
        paymentTimeLeft = data.getIntExtra("timeLeft", 0);
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
        totalMoneyTextView.setText(totalPrice);
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
        totalMoneyTextView = findViewById(R.id.totalMoneyTextView);
        paymentMethodListView = findViewById(R.id.paymentMethodListView);
        paymentTimeLeftTextView = findViewById(R.id.paymentTimeLeftTextView);
        payBtn = findViewById(R.id.payBtn);
    }
    private void setupListeners() {
        setupBackPress();
        payBtn.setOnClickListener(v -> pay());
    }
    private void setupBackPress() {
        backButton.setOnClickListener(v -> {
            CustomDialog.showQuestionDialog(
                    PaymentActivity.this,
                    "Warning",
                    "Are you sure you want to cancel the payment?",
                    isYes -> {
                        if (isYes) {
                            cancelPayment("User cancelled payment");
                        }
                    });
        });

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                CustomDialog.showQuestionDialog(
                        PaymentActivity.this,
                        "Warning",
                        "Are you sure you want to cancel the payment?",
                        isYes -> {
                            if (isYes) {
                                cancelPayment("User cancelled payment");
                            }
                        });
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
        runOnUiThread(() -> cancelPayment("Payment timeout"));
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
        int totalMoney = MoneyFormatter.parseMoney(this, totalMoneyTextView.getText().toString());
        if (totalMoney == 0) {
            successPayment();
            return;
        }

        int quantity = (int) (seatTextView.getText().toString().chars().filter(ch -> ch == ',').count() + 1);
        SimpleMovieDescription movieDescription = new SimpleMovieDescription(movieTitleTextView.getText().toString(), quantity, totalMoney);
        long expiredAt = (System.currentTimeMillis() / 1000) + (paymentTimeLeft / 1000);

        int orderId = Integer.parseInt(orderIdTextView.getText().toString());
        // Change 2000 to totalMoney when in real use
        CreatePayLink data = new CreatePayLink(orderId, 2000, "Movie ticket",
                List.of(movieDescription), CANCEL_URL, RETURN_URL, expiredAt);

        Dotenv dotenv = Dotenv.configure().directory("/assets").filename("env").load();
        PayOsApiService.payOsApiService.createPayLink(dotenv.get("PAYOS_CLIENT_ID"), dotenv.get("PAYOS_API_KEY"), data).enqueue(new Callback<CreatePayLinkResponse>() {
            @Override
            public void onResponse(Call<CreatePayLinkResponse> call, Response<CreatePayLinkResponse> response) {
                payBtn.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    cancelPayment("Failed to create payment link (Network error)");
                    return;
                }

                CreatePayLinkResponse dataResponse = response.body();

                if (dataResponse.getCode().equals(ORDER_ID_EXIST_ERR_CODE)) {
                    cancelPayment("Failed to create payment link (Order ID already exists)");
                    return;
                }
                else if (!dataResponse.getCode().equals("00")) {
                    cancelPayment("Failed to create payment link (PayOS API error)");
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
                cancelPayment("Failed to create payment link (Network error)");
            }
        });

    }

    private void cancelPayment(String reason) {
        if (paymentTimer != null) {
            paymentTimer.cancel();
        }

        Intent data = new Intent();
        setResult(RESULT_CANCELED, data);

        CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Notice", "Payment canceled due to: " + reason, true);
    }

    private void successPayment() {
        if (paymentTimer != null) {
            paymentTimer.cancel();
        }

        Intent data = new Intent();
        setResult(RESULT_OK, data);

        CustomDialog.showAlertDialog(this, R.drawable.ic_success, "Success", "Payment successful", true);
    }
}