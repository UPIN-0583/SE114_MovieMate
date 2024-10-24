package com.example.moviemate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.moviemate.utils.CustomDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailField, passwordField, confirmPasswordField;
    private Button registerButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.register_email);
        passwordField = findViewById(R.id.register_password);
        registerButton = findViewById(R.id.register_button);
        confirmPasswordField = findViewById(R.id.register_confirm_password);
        TextView loginLink = findViewById(R.id.login_link);
        ImageButton backBtn = findViewById(R.id.register_back_btn);

        registerButton.setOnClickListener(view -> registerUser());

        loginLink.setOnClickListener(view -> {
            finish();
        });

        backBtn.setOnClickListener(view -> {
            finish();
        });
    }

    private void registerUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            CustomDialog.showAlertDialog(RegisterActivity.this, R.drawable.ic_error, "Error", "Please enter all details", false);
            return;
        }

        if (password.compareTo(confirmPassword) != 0) {
            CustomDialog.showAlertDialog(RegisterActivity.this, R.drawable.ic_error, "Error", "Passwords do not match", false);
            return;
        }

        registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    registerButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        CustomDialog.showAlertDialog(RegisterActivity.this, R.drawable.ic_success, "Success", "Registration successful", true);
                    } else {
                        CustomDialog.showAlertDialog(RegisterActivity.this, R.drawable.ic_error, "Error", task.getException().getMessage(), false);
                    }
                });
    }
}