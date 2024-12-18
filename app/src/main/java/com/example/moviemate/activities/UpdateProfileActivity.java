package com.example.moviemate.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moviemate.R;
import com.example.moviemate.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageButton backBtn;
    private EditText editTextName, editTextPhone;
    private Button buttonSave;
    private ImageButton buttonChooseImage;
    private CircleImageView avatarImageView;
    private DatabaseReference database;
    private FirebaseAuth auth;
    private StorageReference storageReference;

    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        backBtn = findViewById(R.id.BackBtn);
        editTextName = findViewById(R.id.editTextName);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonSave = findViewById(R.id.buttonSave);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        avatarImageView = findViewById(R.id.imageViewAvatar);
        storageReference = FirebaseStorage.getInstance().getReference("avatars");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Users");

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            loadUserInfo(user.getUid()); // Tải avatar hiện tại từ Firebase
        }

        buttonSave.setOnClickListener(v -> saveUserInfo());
        buttonChooseImage.setOnClickListener(v -> openFileChooser());
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadUserInfo(String uid) {
        database.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userInfo = snapshot.getValue(User.class);
                if (userInfo != null) {
                    editTextName.setText(userInfo.name);
                    editTextPhone.setText(userInfo.phone);
                    if (userInfo.avatarUrl != null) {
                        Picasso.get().load(userInfo.avatarUrl).into(avatarImageView); // Hiển thị avatar hiện tại
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this, "Lỗi khi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(avatarImageView); // Hiển thị ảnh mới bằng Picasso
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                StorageReference fileReference = storageReference.child(user.getUid() + ".jpg");
                fileReference.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveAvatarUrlToDatabase(user.getUid(), imageUrl);
                            Toast.makeText(this, "Avatar uploaded successfully", Toast.LENGTH_SHORT).show();
                        }))
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to upload avatar", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void saveAvatarUrlToDatabase(String uid, String imageUrl) {
        database.child(uid).child("avatarUrl").setValue(imageUrl);
    }

    private void saveUserInfo() {
        String name = editTextName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please fill all information", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // Sử dụng một map để lưu trữ những giá trị cần cập nhật
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("phone", phone);

            // Cập nhật chỉ những giá trị cần thiết vào Firebase Database
            database.child(uid).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(UpdateProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng Activity sau khi lưu
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UpdateProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    });

            uploadImageToFirebase(); // Tải ảnh lên Firebase Storage
        }
    }
}
