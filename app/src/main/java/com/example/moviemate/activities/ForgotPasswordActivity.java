package com.example.moviemate.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.moviemate.R;
import com.example.moviemate.utils.CustomDialog;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView emailInput = findViewById(R.id.forgot_pass_email);

        ImageButton backBtn = findViewById(R.id.forgot_password_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button continueBtn = findViewById(R.id.forgot_pass_continue_btn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emailInput.getText().toString().isEmpty()) {
                    CustomDialog.showAlertDialog(ForgotPasswordActivity.this, R.drawable.ic_error, "Error", "Please enter your email address.", false);
                    return;
                }

                sendResetPasswordEmail(emailInput);
            }
        });
    }

    private void sendResetPasswordEmail(TextView emailInput) {
        Button continueButton = findViewById(R.id.forgot_pass_continue_btn);
        continueButton.setEnabled(false); // Disable button to prevent multiple clicks

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.sendPasswordResetEmail(emailInput.getText().toString())
                .addOnCompleteListener(task -> {
                    continueButton.setEnabled(true); // Enable button

                    if (task.isSuccessful()) {
                        CustomDialog.showAlertDialog(ForgotPasswordActivity.this, R.drawable.ic_success, "Success", "Password reset email sent.", true);
                    } else {
                        CustomDialog.showAlertDialog(ForgotPasswordActivity.this, R.drawable.ic_error, "Error", "Failed to send password reset email.", false);
                    }
                });
    }
}