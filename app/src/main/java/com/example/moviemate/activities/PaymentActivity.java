package com.example.moviemate.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.moviemate.R;
import com.example.moviemate.adapters.PaymentMethodListAdapter;
import com.example.moviemate.models.PaymentMethod;
import com.example.moviemate.utils.CustomDialog;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class PaymentActivity extends AppCompatActivity {
    private static final int PAYMENT_TIMEOUT = 900000; // 15 minutes

    private Timer paymentTimer;
    private int paymentTimeLeft = PAYMENT_TIMEOUT;
    private PaymentMethodListAdapter paymentMethodListAdapter;
    private Integer basePrice = null;

    // UI Components
    private ImageView backButton;
    private TextView movieTitleTextView;
    private TextView movieGenreTextView;
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
        String reason = "Payment timeout";
        runOnUiThread(() -> cancelPayment(reason));
    }

    private void initPaymentMethods() {
        List<PaymentMethod> paymentMethods = List.of(
                new PaymentMethod(PaymentMethod.TYPE.ZALO_PAY, "Zalo Pay", R.drawable.logo_zalopay),
                new PaymentMethod(PaymentMethod.TYPE.MOMO, "MoMo", R.drawable.logo_momo),
                new PaymentMethod(PaymentMethod.TYPE.QR_CODE, "QR Code", R.drawable.logo_qrcode)
        );

        paymentMethodListAdapter = new PaymentMethodListAdapter(this, R.layout.payment_method_item, paymentMethods);

        paymentMethodListView = findViewById(R.id.paymentMethodListView);
        paymentMethodListView.setAdapter(paymentMethodListAdapter);
    }
    private void pay() {

    }
    private void cancelPayment(String reason) {
        if (paymentTimer != null) {
            paymentTimer.cancel();
        }

        CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Payment cancelled due to: " + reason, false);
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