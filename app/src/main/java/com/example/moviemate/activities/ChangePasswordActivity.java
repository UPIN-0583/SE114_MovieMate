package com.example.moviemate.activities;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.moviemate.R;
import com.example.moviemate.utils.CustomDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private Button changePasswordButton;
    private boolean passwordVisible = false;
    private ImageButton showHideCurrentPasswordImageButton, showHideNewPasswordImageButton, showHideConfirmPasswordImageButton;

    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordTextView);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(v -> changePassword());

        showHideCurrentPasswordImageButton = findViewById(R.id.showHideCurrentPasswordImageButton);
        showHideCurrentPasswordImageButton.setOnClickListener(v -> showHidePassword());
        showHideNewPasswordImageButton = findViewById(R.id.showHideNewPasswordImageButton);
        showHideNewPasswordImageButton.setOnClickListener(v -> showHidePassword());
        showHideConfirmPasswordImageButton = findViewById(R.id.showHideConfirmPasswordImageButton);
        showHideConfirmPasswordImageButton.setOnClickListener(v -> showHidePassword());

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // User is not signed in
            return;
        }

        for (UserInfo userInfo : user.getProviderData()) {
            if (GoogleAuthProvider.PROVIDER_ID.equals(userInfo.getProviderId())) {
                CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "You are signed in with Google. Please change your password in your Google account", true);
            }
        }
    }

    private void changePassword() {
        String currentPassword = currentPasswordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Please fill in all fields", false);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "New password and confirm password do not match", false);
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;

        String email = user.getEmail();
        if (email == null)
        {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Email is null", false);
            return;
        }

        changePasswordButton.setEnabled(false);
        Toast.makeText(this, "Changing password...", Toast.LENGTH_SHORT).show();

        AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                CustomDialog.showAlertDialog(ChangePasswordActivity.this, R.drawable.ic_success, "Success", "Password changed successfully", true);
                            }
                            else {
                                CustomDialog.showAlertDialog(ChangePasswordActivity.this, R.drawable.ic_error, "Error", "Failed to change password. Please try again", false);
                            }
                        }
                    });
                }
                else {
                    CustomDialog.showAlertDialog(ChangePasswordActivity.this, R.drawable.ic_error, "Error", "Failed to re-authenticate user. Please try again", false);
                }

                changePasswordButton.setEnabled(true);
            }
        });
    }

    private void showHidePassword() {
        if (passwordVisible) {
            // Hide password
            currentPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            newPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            showHideCurrentPasswordImageButton.setImageResource(R.drawable.ic_show_pass);
            showHideNewPasswordImageButton.setImageResource(R.drawable.ic_show_pass);
            showHideConfirmPasswordImageButton.setImageResource(R.drawable.ic_show_pass);
        }
        else {
            // Show password
            currentPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            newPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

            showHideCurrentPasswordImageButton.setImageResource(R.drawable.ic_hide_pass);
            showHideNewPasswordImageButton.setImageResource(R.drawable.ic_hide_pass);
            showHideConfirmPasswordImageButton.setImageResource(R.drawable.ic_hide_pass);
        }

        passwordVisible = !passwordVisible;
        currentPasswordEditText.setSelection(currentPasswordEditText.getText().length()); // Move cursor to the end of the text
    }
}