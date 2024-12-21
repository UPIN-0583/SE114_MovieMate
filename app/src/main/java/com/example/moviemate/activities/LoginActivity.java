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

import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.atomic.AtomicBoolean;

public class LoginActivity extends AppCompatActivity {
    private AtomicBoolean isDataLoaded = new AtomicBoolean(false);
    private EditText emailField, passwordField;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference userRef;
    private boolean passwordVisible = false;
    private ImageButton showHidePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> !isDataLoaded.get());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null || !currentUser.isEmailVerified()) {
            isDataLoaded.set(true);
        }
        else {
            autoLogin(currentUser);
        }

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

    private void autoLogin(FirebaseUser currentUser) {
        // Kiểm tra xem người dùng có phải là admin hay không
        userRef.child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("[MOVIEMATE LOGIN]", "Failed to query user", task.getException());
                    isDataLoaded.set(true);
                    return;
                }

                DataSnapshot snapshot = task.getResult();
                User user = snapshot.getValue(User.class);
                if (user == null)
                    return;

                Intent intent;// Prevent user from going back to login screen
                if (user.role.equals("admin")) {
                    intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                }
                else {
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("user", user);
                }
                startActivity(intent);
                finish();
                isDataLoaded.set(true);
            }
        });
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
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null)
                            return;

                        if (!user.isEmailVerified()) {
                            CustomDialog.showAlertDialog(LoginActivity.this, R.drawable.ic_error, "Error", "Please verify your email before logging in.", false);
                            return;
                        }

                        DatabaseReference userRef = this.userRef.child(user.getUid());
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                if (user == null)
                                    return;

                                if (user.role.equals("admin")) {
                                    Intent intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("user", user);
                                    startActivity(intent);
                                    finish(); // Prevent user from going back to login screen
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("[MOVIEMATE LOGIN]", "Failed to query user");
                            }
                        });
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
                        if (user == null)
                            return;

                        checkAndSaveUserToDatabase(user);
                        userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                if (user == null)
                                    return;

                                if (user.role.equals("admin")) {
                                    Intent intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("user", user);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("[MOVIEMATE LOGIN]", "Failed to query user");
                            }
                        });

                    } else {
                        // Đăng nhập thất bại
                        CustomDialog.showAlertDialog(LoginActivity.this, R.drawable.ic_error, "Error", "Google sign-in failed.", false);
                    }
                });
    }

    private void checkAndSaveUserToDatabase(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        DatabaseReference userRef = this.userRef.child(uid);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().exists()) {
                // Nếu người dùng chưa tồn tại trong Database, tạo bản ghi mới
                String email = firebaseUser.getEmail();
                String name = firebaseUser.getDisplayName();
                User user = new User(uid, name, null, email, null, "user");

                userRef.setValue(user)
                        .addOnSuccessListener(aVoid -> Log.d("LoginActivity", "User saved to database"))
                        .addOnFailureListener(e -> Log.e("LoginActivity", "Failed to save user to database", e));
            }
        });
    }
}