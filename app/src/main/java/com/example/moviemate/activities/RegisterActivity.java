package com.example.moviemate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


import com.example.moviemate.R;
import com.example.moviemate.models.User;
import com.example.moviemate.utils.CustomDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailField, passwordField, confirmPasswordField;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference database;

    private boolean passwordVisible = false;
    private ImageButton passwordVisibilityBtn;
    private ImageButton confirmPasswordVisibilityBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Users");

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
            CustomDialog.showAlertDialog(RegisterActivity.this, R.drawable.ic_error, "Lỗi", "Vui lòng điền đầy đủ thông tin", false);
            return;
        }

        if (password.compareTo(confirmPassword) != 0) {
            CustomDialog.showAlertDialog(RegisterActivity.this, R.drawable.ic_error, "Lỗi", "Mật khẩu không khớp", false);
            return;
        }

        registerButton.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    registerButton.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            User user = new User(null, null, email, null); // Khởi tạo User với avatarUrl là null

                            database.child(uid).setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        CustomDialog.showAlertDialog(RegisterActivity.this, R.drawable.ic_success, "Thông báo", "Đăng ký thành công", true);
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Lưu thông tin thất bại", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        CustomDialog.showAlertDialog(RegisterActivity.this, R.drawable.ic_error, "Lỗi", task.getException().getMessage(), false);
                    }
                });
    }
}