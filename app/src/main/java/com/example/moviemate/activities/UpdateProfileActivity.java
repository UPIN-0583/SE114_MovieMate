package com.example.moviemate.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moviemate.R;
import com.example.moviemate.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhone, editTextEmail;
    private Button buttonSave;
    private DatabaseReference database;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonSave = findViewById(R.id.buttonSave);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Users");

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });
    }

    private void saveUserInfo() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            User updatedUser = new User(name, phone, user.getEmail(),null);

            // Lưu thông tin cập nhật vào Firebase Database
            database.child(uid).setValue(updatedUser)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(UpdateProfileActivity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng Activity sau khi lưu
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UpdateProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
