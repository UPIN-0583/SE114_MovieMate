package com.example.moviemate.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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
import java.util.Objects;

public class PaymentActivity extends AppCompatActivity {
    PaymentMethodListAdapter paymentMethodListAdapter;
    Integer basePrice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initPaymentMethods();
        loadAndDisplayData();

        Button applyDiscountCodeButton = findViewById(R.id.applyDiscountBtn);
        applyDiscountCodeButton.setOnClickListener(v -> applyDiscountCode());

        Button payButton = findViewById(R.id.payBtn);
        payButton.setOnClickListener(v -> pay());
    }

    private void initPaymentMethods() {
        List<PaymentMethod> paymentMethods = List.of(
                new PaymentMethod(PaymentMethod.TYPE.ZALO_PAY, "Zalo Pay", R.drawable.logo_zalopay),
                new PaymentMethod(PaymentMethod.TYPE.MOMO, "MoMo", R.drawable.logo_momo),
                new PaymentMethod(PaymentMethod.TYPE.QR_CODE, "QR Code", R.drawable.logo_qrcode)
        );

        paymentMethodListAdapter = new PaymentMethodListAdapter(this, R.layout.payment_method_item, paymentMethods);

        ListView paymentMethodListView = findViewById(R.id.paymentMethodListView);
        paymentMethodListView.setAdapter(paymentMethodListAdapter);
    }

    private void loadAndDisplayData() {
        // Put code to load data here
    }

    private void applyDiscountCode() {
        EditText discountCodeEditText = findViewById(R.id.discountCodeEdit);
        String discountCode = discountCodeEditText.getText().toString().trim();

        if (discountCode.isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Please enter discount code", false);
            return;
        }

        TextView totalMoneyTextView = findViewById(R.id.totalMoneyTextView);
        String totalMoneyString = totalMoneyTextView.getText().toString();
        int totalMoney = parseMoney(totalMoneyString);

        // Do not stack discount
        if (basePrice != null && totalMoney != basePrice) {
            totalMoney = basePrice;
        }

        int discountAmount = 100000;
        // Calculate discount amount here

        totalMoney += discountAmount;
        totalMoney = Math.max(totalMoney, 0); // Never show negative money

        totalMoneyTextView.setText(formatMoney(totalMoney));

        CustomDialog.showAlertDialog(this, R.drawable.ic_success, "Success", "Discount code applied successfully", false);
    }

    private void pay() {
        // Put code to pay here
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