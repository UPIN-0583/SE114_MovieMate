package com.example.moviemate.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.moviemate.R;
import com.example.moviemate.utils.CustomDialog;

public class PayOsActivity extends AppCompatActivity {
    private WebView payOsScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pay_os);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        final String CHECKOUT_URL = intent.getStringExtra("checkoutUrl");
        final String CANCEL_URL = intent.getStringExtra("cancelUrl");
        final String SUCCESS_URL = intent.getStringExtra("successUrl");
        if (CHECKOUT_URL == null || CANCEL_URL == null || SUCCESS_URL == null) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Missing data", true);
            return;
        }

        payOsScreen = findViewById(R.id.pay_os_screen);
        payOsScreen.getSettings().setJavaScriptEnabled(true);
        payOsScreen.loadUrl(CHECKOUT_URL);
        payOsScreen.setWebViewClient(new WebViewClient() {

        });
        payOsScreen.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.d("PayOsActivity", "onPageStarted: " + url);
                if (url.contains(SUCCESS_URL)) {
                    success();
                } else if (url.contains(CANCEL_URL)) {
                    cancel();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.d("PayOsActivity", "shouldOverrideUrlLoading: " + url);
                if (url.contains(SUCCESS_URL)) {
                    success();
                    return true;
                }
                else if (url.contains(CANCEL_URL)) {
                    cancel();
                    return true;
                }
                return false;
            }
        });
    }

    private void cancel() {
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        data.putExtra("status", "cancel");
        finish();
    }

    private void success() {
        Intent data = new Intent();
        setResult(RESULT_OK, data);
        data.putExtra("status", "success");
        finish();
    }
}