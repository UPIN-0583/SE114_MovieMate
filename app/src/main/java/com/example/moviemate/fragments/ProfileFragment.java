package com.example.moviemate.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moviemate.R;
import com.example.moviemate.activities.UpdateProfileActivity;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView nameTextView, phoneTextView, emailTextView;
    private ImageView avatarImageView;
    private Button chooseImageButton;
    private DatabaseReference database;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ánh xạ các TextView và ImageView
        nameTextView = view.findViewById(R.id.textViewName);
        phoneTextView = view.findViewById(R.id.textViewPhone);
        emailTextView = view.findViewById(R.id.textViewEmail);
        avatarImageView = view.findViewById(R.id.imageViewAvatar);
        chooseImageButton = view.findViewById(R.id.buttonChooseImage);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("avatars");

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            loadUserInfo(user.getUid());
        }

        chooseImageButton.setOnClickListener(v -> openFileChooser());
        Button buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        buttonEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
            startActivity(intent);
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            loadUserInfo(user.getUid()); // Tải lại thông tin khi ProfileFragment được hiển thị lại
        }
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
                            Picasso.get().load(imageUrl).into(avatarImageView); // Hiển thị ảnh mới bằng Picasso
                            Toast.makeText(getActivity(), "Upload ảnh thành công", Toast.LENGTH_SHORT).show();
                        }))
                        .addOnFailureListener(e -> Toast.makeText(getActivity(), "Upload ảnh thất bại", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void saveAvatarUrlToDatabase(String uid, String imageUrl) {
        database.child("Users").child(uid).child("avatarUrl").setValue(imageUrl);
    }

    private void loadUserInfo(String uid) {
        database.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User userInfo = snapshot.getValue(User.class);
                if (userInfo != null) {
                    if (userInfo.name == null )  nameTextView.setText("Null");
                    else nameTextView.setText(userInfo.name);
                    if (userInfo.phone == null) phoneTextView.setText("Null");
                    else phoneTextView.setText(userInfo.phone);
                    emailTextView.setText(userInfo.email);

                    // Load avatar nếu có, sử dụng Picasso
                    if (userInfo.avatarUrl != null) {
                        Picasso.get().load(userInfo.avatarUrl).into(avatarImageView);
                    }
                } else {
                    Toast.makeText(getActivity(), "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Lỗi khi tải thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
