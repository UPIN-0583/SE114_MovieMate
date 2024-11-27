package com.example.moviemate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;


import com.example.moviemate.R;

import com.example.moviemate.models.User;
import com.example.moviemate.utils.CustomDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference database;
    private boolean passwordVisible = false;
    private ImageButton showHidePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(new SplashScreen.KeepOnScreenCondition() {
            @Override
            public boolean shouldKeepOnScreen() {
                // Add logic to determine when to remove the splash screen
                return false;
            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        // Nếu người dùng login rồi thì không yêu cầu họ login lại nữa
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        database = FirebaseDatabase.getInstance().getReference("Users");

        emailField = findViewById(R.id.login_email);
        passwordField = findViewById(R.id.login_password);
        Button loginButton = findViewById(R.id.login_button);
        TextView registerLink = findViewById(R.id.register_link);
        TextView forgotPasswordLink = findViewById(R.id.forgot_password_link);
        showHidePasswordButton = findViewById(R.id.login_show_hide_password_btn);
        showHidePasswordButton.setOnClickListener(view -> {
            showHidePassword();
        });

        loginButton.setOnClickListener(view -> loginUser());

        registerLink.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        forgotPasswordLink.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Lấy token từ Firebase Console
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.google_sign_in_button).setOnClickListener(v -> signInWithGoogle());
    }

    private void showHidePassword() {
        if (passwordVisible) {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            showHidePasswordButton.setImageResource(R.drawable.ic_show_pass);

        } else {
            passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            showHidePasswordButton.setImageResource(R.drawable.ic_hide_pass);
        }

        passwordVisible = !passwordVisible;
        passwordField.setSelection(passwordField.getText().length()); // Move cursor to the end of the text
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            CustomDialog.showAlertDialog(LoginActivity.this, R.drawable.ic_error, "Error", "Please enter all details", false);
            return;
        }

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Prevent user from going back to login screen
                    } else {
                        CustomDialog.showAlertDialog(LoginActivity.this, R.drawable.ic_error, "Error", "Login failed: " + task.getException().getMessage(), false);
                    }
                });
    }

    // Hàm để khởi động quá trình đăng nhập Google
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Xử lý kết quả trả về sau khi đăng nhập bằng Google
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Kiểm tra nếu là yêu cầu đăng nhập bằng Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Đăng nhập thành công, tiến hành xác thực với Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("LoginActivity", "Google sign in failed", e);
                CustomDialog.showAlertDialog(LoginActivity.this, R.drawable.ic_error, "Error", "Google sign-in failed.", false);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập thành công, lưu thông tin người dùng vào Database nếu chưa tồn tại
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkAndSaveUserToDatabase(user);
                        }
                        Toast.makeText(LoginActivity.this, "Google sign-in successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Đóng LoginActivity sau khi đăng nhập thành công
                    } else {
                        // Đăng nhập thất bại
                        CustomDialog.showAlertDialog(LoginActivity.this, R.drawable.ic_error, "Error", "Google sign-in failed.", false);
                    }
                });
    }

    private void checkAndSaveUserToDatabase(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        DatabaseReference userRef = database.child(uid);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().exists()) {
                // Nếu người dùng chưa tồn tại trong Database, tạo bản ghi mới
                String email = firebaseUser.getEmail();
                String name = firebaseUser.getDisplayName();
                User user = new User(name, null, email, null); // Chỉ lưu name và email

                userRef.setValue(user)
                        .addOnSuccessListener(aVoid -> Log.d("LoginActivity", "User saved to database"))
                        .addOnFailureListener(e -> Log.e("LoginActivity", "Failed to save user to database", e));
            }
        });
    }
}