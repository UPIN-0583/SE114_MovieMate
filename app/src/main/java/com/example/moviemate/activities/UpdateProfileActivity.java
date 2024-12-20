package com.example.moviemate.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.moviemate.R;
import com.example.moviemate.models.User;
import com.example.moviemate.utils.CustomDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import io.github.cdimascio.dotenv.Dotenv;

public class UpdateProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageButton backBtn;
    private EditText editTextName, editTextPhone;
    private Button buttonSave;
    private ImageButton buttonChooseImage;
    private CircleImageView avatarImageView;
    private DatabaseReference userRef;
    private FirebaseAuth auth;
    private Cloudinary cloudinary;

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

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");

        Dotenv dotenv = Dotenv.configure().directory("/assets").filename("env").load();
        cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            loadUserInfo(user.getUid()); // Tải avatar hiện tại từ Firebase
        }

        buttonSave.setOnClickListener(v -> saveUserInfo());
        buttonChooseImage.setOnClickListener(v -> openFileChooser());
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadUserInfo(String uid) {
        userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
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
            Toast.makeText(this, "Uploading avatar", Toast.LENGTH_SHORT).show();

            uploadImage();
        }
    }
    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();

        return path;
    }

    private void uploadImage() {
        FirebaseUser user = auth.getCurrentUser();

        if (imageUri == null || user == null) {
            Toast.makeText(this, "Failed to upload avatar", Toast.LENGTH_SHORT).show();
            Log.e("UpdateProfileActivity", "imageUri or user is null");
        }

        Compressor avatarCompressor = new Compressor(this)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .setQuality(70)
                .setMaxWidth(125);
        boolean isCompressed = false;

        try {
            // Compress avatar before uploading
            File compressedAvatar = avatarCompressor.compressToFile(new File(getRealPathFromUri(imageUri)));
            isCompressed = true;

            // Upload avatar to Cloudinary
            assert user != null;
            String publicId = user.getUid() + System.currentTimeMillis();
            String requestID = MediaManager.get().upload(compressedAvatar.getPath())
                    .option("folder", "avatars")
                    .option("public_id", publicId)
                    .option("overwrite", true)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Toast.makeText(UpdateProfileActivity.this, "Uploading avatar", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            Toast.makeText(UpdateProfileActivity.this, "Avatar uploaded", Toast.LENGTH_SHORT).show();
                            Picasso.get().load(imageUri).into(avatarImageView);
                            saveAvatarUrlToDatabase(user.getUid(), (String) resultData.get("public_id"));
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            CustomDialog.showAlertDialog(UpdateProfileActivity.this,
                                    R.drawable.ic_error, "Error",
                                    String.format("Failed to upload avatar due to %s", error.getDescription()),
                                    false);
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                        }
                    })
                    .dispatch();
        } catch (Exception e) {
            Log.e("Compressor", "Failed to compress image", e);
        }
    }

    private void saveAvatarUrlToDatabase(String userUID, String publicId) {
        Dotenv dotenv = Dotenv.configure().directory("/assets").filename("env").load();
        String cloudName = dotenv.get("CLOUDINARY_CLOUD_NAME");
        String imageUrl = String.format("https://res.cloudinary.com/%s/image/upload/q_auto/f_auto/%s", cloudName, publicId);

        userRef.child(userUID).child("avatarUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String avatarUrl = snapshot.getValue(String.class);
                String oldPublicId = avatarUrl != null ? avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1) : null;

                // Xóa avatar cũ trên Cloudinary
                if (oldPublicId != null) {
                    oldPublicId = "avatars/" + oldPublicId;

                    try {
                        cloudinary.uploader().destroy(oldPublicId, null);
                    }
                    catch (Exception e) {
                        Log.e("UpdateProfileActivity", "Failed to delete old avatar", e);
                    }
                }

                userRef.child(userUID).child("avatarUrl").setValue(imageUrl); // Lưu avatar mới vào Firebase
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UpdateProfileActivity", "Failed to save avatar URL to database", error.toException());
            }
        });
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
            userRef.child(uid).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(UpdateProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng Activity sau khi lưu
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UpdateProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
