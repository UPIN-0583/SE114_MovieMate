package com.example.moviemate.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.moviemate.R;
import com.example.moviemate.models.Person;
import com.example.moviemate.utils.CustomDialog;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import io.github.cdimascio.dotenv.Dotenv;

public class AddPeopleActivity extends AppCompatActivity {
    private TextView titleTv;
    private CircleImageView personImage;
    private EditText personName;
    private Uri personImagePath;
    private String imageUrl;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_people);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupViews();

        Intent intent = getIntent();

        String title = intent.getStringExtra("title");
        if (title == null) finish();

        titleTv.setText(title);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            save();
        });

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent data = new Intent();
            setResult(Activity.RESULT_CANCELED, data);
            finish();
        });

        Button changeAvatarButton = findViewById(R.id.changeAvatarButton);
        changeAvatarButton.setOnClickListener(v -> {
            changeAvatar();
        });
    }

    private void changeAvatar() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            personImagePath = data.getData();
            Picasso.get().load(personImagePath).into(personImage);
        }
    }

    private void uploadImageAndFinish() {
        if (personImagePath == null) return;

        Compressor avatarCompressor = new Compressor(this)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .setQuality(70)
                .setMaxWidth(125);
        boolean isCompressed = false;

        try {
            // Compress avatar before uploading
            File compressedAvatar = avatarCompressor.compressToFile(new File(getRealPathFromUri(personImagePath)));
            isCompressed = true;

            String publicId = String.valueOf(System.currentTimeMillis());

            String requestID = MediaManager.get().upload(compressedAvatar.getPath())
                    .option("folder", "MoviePersonnels")
                    .option("public_id", publicId)
                    .option("overwrite", true)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Toast.makeText(AddPeopleActivity.this, "Compressing image", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            Toast.makeText(AddPeopleActivity.this, "Avatar uploaded", Toast.LENGTH_SHORT).show();
                            Dotenv dotenv = Dotenv.configure().directory("/assets").filename("env").load();
                            String cloudName = dotenv.get("CLOUDINARY_CLOUD_NAME");
                            String public_id = (String) resultData.get("public_id");
                            imageUrl = String.format("https://res.cloudinary.com/%s/image/upload/q_auto/f_auto/%s", cloudName, public_id);

                            Person person = new Person();
                            person.setName(personName.getText().toString());
                            person.setPicUrl(imageUrl);

                            Intent intent = new Intent();
                            intent.putExtra("type", titleTv.getText().toString());
                            intent.putExtra("person", person);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e("Compressor", "Failed to upload image" + error.getDescription());
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

    private void save() {
        if (personName.getText().toString().isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Please enter person name", false);
            return;
        }

        if (personImagePath == null) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Please select an image", false);
            return;
        }
        
        uploadImageAndFinish();
    }

    private void setupViews() {
        titleTv = findViewById(R.id.title);
        personName = findViewById(R.id.personNameEditText);
        personImage = findViewById(R.id.peopleAvatar);
    }
}