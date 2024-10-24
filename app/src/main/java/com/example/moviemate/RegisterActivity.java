package com.example.moviemate;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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

    private boolean passwordVisible = false;
    private ImageButton passwordVisibilityBtn;
    private ImageButton confirmPasswordVisibilityBtn;

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

        passwordVisibilityBtn = findViewById(R.id.register_show_hide_password);
        passwordVisibilityBtn.setOnClickListener(view -> {
            showHidePassword();
         });
        confirmPasswordVisibilityBtn = findViewById(R.id.register_show_hide_confirm_password);
        confirmPasswordVisibilityBtn.setOnClickListener(view -> {
            showHidePassword();
        });

        registerButton.setOnClickListener(view -> registerUser());

        loginLink.setOnClickListener(view -> {
            finish();
        });

        backBtn.setOnClickListener(view -> {
            finish();
        });
    }

    private void showHidePassword() {
        if (passwordVisible) {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            confirmPasswordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordVisibilityBtn.setImageResource(R.drawable.ic_show_pass);
            confirmPasswordVisibilityBtn.setImageResource(R.drawable.ic_show_pass);
        }
        else {
            passwordField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            confirmPasswordField.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordVisibilityBtn.setImageResource(R.drawable.ic_hide_pass);
            confirmPasswordVisibilityBtn.setImageResource(R.drawable.ic_hide_pass);
        }

        passwordVisible = !passwordVisible;
        passwordField.setSelection(passwordField.getText().length()); // Move cursor to the end of the text
        confirmPasswordField.setSelection(confirmPasswordField.getText().length()); // Move cursor to the end of the text
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