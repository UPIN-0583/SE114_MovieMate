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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.moviemate.R;
import com.example.moviemate.adapters.MovieDateTimeAdapter;
import com.example.moviemate.adapters.PersonAdapter;
import com.example.moviemate.models.Movie;
import com.example.moviemate.models.MovieDateTime;
import com.example.moviemate.models.Person;
import com.example.moviemate.utils.CustomDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;
import io.github.cdimascio.dotenv.Dotenv;

public class AddMovieActivity extends AppCompatActivity {
    private Movie newMovie;
    private static final int PICK_IMAGE_REQUEST = 1;
    ImageButton backButton, editPosterButton;
    Button saveButton;
    ImageView movieBanner;
    TextView addDirectorTv, addActorTv, addShowTimeTv;
    EditText timeHourEt, timeMinuteEt, yearEt, priceEt, languageEt, ratingEt, titleEt, genreEt, storylineEt,  trailerEt;
    Spinner statusSpinner;
    RecyclerView directorRv, actorRv, showTimeRv;
    private PersonAdapter directorAdapter, actorAdapter;
    private MovieDateTimeAdapter showTimeAdapter;
    private List<Person> directors, actors;
    private List<MovieDateTime> dateTimes;
    private Uri posterUri;

    private ActivityResultLauncher<Intent> launcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_movie);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupViews();
        initRecyclerView();
        initLauncher();
    }


    private void initLauncher() {
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_CANCELED) return;

                    Intent data = result.getData();
                    if (data == null) return;

                    if (data.getStringExtra("type").equals("Add Director")) {
                        Person person = (Person) data.getSerializableExtra("person");
                        directors.add(person);
                        directorAdapter.notifyDataSetChanged();
                    } else if (data.getStringExtra("type").equals("Add Actor")) {
                        Person person = (Person) data.getSerializableExtra("person");
                        actors.add(person);
                        actorAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupViews() {
        addActorTv = findViewById(R.id.addActorTextView);
        addActorTv.setOnClickListener(v -> {
            addActor();
        });
        addDirectorTv = findViewById(R.id.addDirectorTextView);
        addDirectorTv.setOnClickListener(v -> {
            addDirector();
        });
        addShowTimeTv = findViewById(R.id.addShowTimeTextView);
        addShowTimeTv.setOnClickListener(v -> {
            addShowTime();
        });

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        editPosterButton = findViewById(R.id.editMovieBanner);
        editPosterButton.setOnClickListener(v -> {
            editPoster();
        });
        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            addNewMovie();
        });

        movieBanner = findViewById(R.id.movieBanner);

        timeHourEt = findViewById(R.id.timeHourEditText);
        timeMinuteEt = findViewById(R.id.timeMinuteEditText);
        yearEt = findViewById(R.id.yearEditText);
        languageEt = findViewById(R.id.languageEditText);
        ratingEt = findViewById(R.id.ratingEditText);
        priceEt = findViewById(R.id.priceEditText);
        titleEt = findViewById(R.id.titleEditText);
        genreEt = findViewById(R.id.genreTextEdit);
        storylineEt = findViewById(R.id.storylineTextEdit);
        trailerEt = findViewById(R.id.trailerUrlTextEdit);
        statusSpinner = findViewById(R.id.statusSpinner);
    }

    private void addNewMovie() {
        saveButton.setEnabled(false);
        if (!checkData()) {
            saveButton.setEnabled(true);
            return;
        }
        Toast.makeText(this, "Adding movie...", Toast.LENGTH_SHORT).show();
        uploadMoviePoster();
        saveButton.setEnabled(true);
    }

    private boolean checkData() {
        if (titleEt.getText().toString().isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Title cannot be empty", false);
            return false;
        }

        if (timeHourEt.getText().toString().isEmpty() || timeMinuteEt.getText().toString().isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Time cannot be empty", false);
            return false;
        }

        if (yearEt.getText().toString().isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Year cannot be empty", false);
            return false;
        }

        if (priceEt.getText().toString().isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Price cannot be empty", false);
            return false;
        }

        if (languageEt.getText().toString().isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Language cannot be empty", false);
            return false;
        }

        if (ratingEt.getText().toString().isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Rating cannot be empty", false);
            return false;
        }

        if (genreEt.getText().toString().isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Genre cannot be empty", false);
            return false;
        }

        if (trailerEt.getText().toString().isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Trailer cannot be empty", false);
            return false;
        }

        if (storylineEt.getText().toString().isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Storyline cannot be empty", false);
            return false;
        }

        if (directors.isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Director cannot be empty", false);
            return false;
        }

        if (actors.isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Actor cannot be empty", false);
            return false;
        }

        if (dateTimes.isEmpty()) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Show time cannot be empty", false);
            return false;
        }

        return true;
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

    private void uploadMoviePoster() {
        if (posterUri == null) {
            CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", "Please select a poster", false);
            return;
        }

        String path = getRealPathFromUri(posterUri);

        Compressor compressor = new Compressor(this)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .setQuality(70);

        try {
            File compressedPoster = compressor.compressToFile(new File(path));

            String publicId = titleEt.getText().toString().replace(" ", "_") + "_" + System.currentTimeMillis();
            MediaManager.get().upload(compressedPoster.getPath())
                    .option("public_id", publicId)
                    .option("resource_type", "image")
                    .option("folder", "Movies")
                    .option("overwrite", true)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {

                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {

                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String publicId = (String) resultData.get("public_id");

                            Dotenv dotenv = Dotenv.configure().directory("/assets").filename("env").load();
                            String cloudName = dotenv.get("CLOUDINARY_CLOUD_NAME");
                            String imageUrl = String.format("https://res.cloudinary.com/%s/image/upload/q_auto/f_auto/%s", cloudName, publicId);

                            addMovie(imageUrl);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {

                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {

                        }
                    }).dispatch();
        } catch (Exception e) {
            Log.e("Compressor", Objects.requireNonNull(e.getMessage()));
            String publicId = titleEt.getText().toString().replace(" ", "_") + "_" + System.currentTimeMillis();
            MediaManager.get().upload(posterUri)
                    .option("public_id", publicId)
                    .option("resource_type", "image")
                    .option("folder", "Movies")
                    .option("overwrite", true)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {

                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {

                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String publicId = (String) resultData.get("public_id");

                            Dotenv dotenv = Dotenv.configure().directory("/assets").filename("env").load();
                            String cloudName = dotenv.get("CLOUDINARY_CLOUD_NAME");
                            String imageUrl = String.format("https://res.cloudinary.com/%s/image/upload/q_auto/f_auto/%s", cloudName, publicId);

                            addMovie(imageUrl);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {

                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {

                        }
                    }).dispatch();
        }
    }
    private void addMovie(String moviePosterUrl) {
        String title = titleEt.getText().toString();
        String time = timeHourEt.getText().toString() + "h " + timeMinuteEt.getText().toString() + "m";
        int year = Integer.parseInt(yearEt.getText().toString());
        String status = statusSpinner.getSelectedItem().toString();
        int price = Integer.parseInt(priceEt.getText().toString());
        String language = languageEt.getText().toString();
        String rating = ratingEt.getText().toString();
        String genre = genreEt.getText().toString();
        String trailer = trailerEt.getText().toString();
        String storyline = storylineEt.getText().toString();

        List<String> genres = new ArrayList<>();
        for (String g : genre.split(",")) {
            genres.add(g.trim());
        }

        newMovie = new Movie();
        newMovie.setMovieID((int)(Math.random() * 1000000));
        newMovie.setImdb(10.0);
        newMovie.setPoster(moviePosterUrl);
        newMovie.setTitle(title);
        newMovie.setTime(time);
        newMovie.setYear(year);
        newMovie.setStatus(status);
        newMovie.setSeatPrice(price);
        newMovie.setLanguage(language);
        newMovie.setRating(rating);
        newMovie.setGenre(genres);
        newMovie.setTrailer(trailer);
        newMovie.setDescription(storyline);
        newMovie.setDirector(directorAdapter.getPersonList());
        newMovie.setActor(actorAdapter.getPersonList());

        String movieId = "Movie" + newMovie.getMovieID();
        DatabaseReference movieRef = FirebaseDatabase.getInstance().getReference("Movies").child(movieId);
        movieRef.setValue(newMovie).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("AddMovieActivity", "Movie added successfully");
                addShowTimeToFirebase();
            } else {
                CustomDialog.showAlertDialog(this, R.drawable.ic_error, "Error", Objects.requireNonNull(task.getException()).getMessage(), false);
                Log.e("AddMovieActivity", "Failed to add movie", task.getException());
            }
        });
    }

    private void addShowTimeToFirebase() {
        DatabaseReference showTimeRef = FirebaseDatabase.getInstance().getReference("Cinemas")
                .child("Cinema1")
                .child("Movies")
                .child("Movie" + newMovie.getMovieID())
                .child("ShowTimes");

        Map<String, String> seatData = new LinkedHashMap<>();
        for (int i = 'A'; i <= 'E'; i++) {
            for (int j = 1; j <= 9; j++) {
                String seat = (char) i + String.valueOf(j);
                seatData.put(seat, "available");
            }
        }

        showTimeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (MovieDateTime mdt : dateTimes) {
                    DataSnapshot dateRef = snapshot.child(mdt.date);

                    if (dateRef.child(mdt.time).getValue() != null) {
                        continue; // Ignore if the date and time already exist
                    }

                    showTimeRef.child(mdt.date).child(mdt.time).child("ShowTimeID").setValue("Cinema1_Movie" + newMovie.getMovieID() + "_" + mdt.date + "_" + mdt.time);
                    showTimeRef.child(mdt.date).child(mdt.time).child("Seats").setValue(seatData);
                }

                CustomDialog.showAlertDialog(AddMovieActivity.this, R.drawable.ic_success, "Success", "Movie added successfully", true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addShowTime() {
        SwitchDateTimeDialogFragment dateTimeDialogFragment = SwitchDateTimeDialogFragment.newInstance(
                "Select Date & Time",
                "OK",
                "Cancel"
        );

        Calendar calendar = Calendar.getInstance();
        dateTimeDialogFragment.setMinimumDateTime(calendar.getTime());
        dateTimeDialogFragment.setDefaultDateTime(calendar.getTime());
        dateTimeDialogFragment.set24HoursMode(true);

        dateTimeDialogFragment.setOnButtonClickListener(new SwitchDateTimeDialogFragment.OnButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Date date) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    String selectedDateTime = format.format(date);
                    String[] dateTime = selectedDateTime.split(" ");

                    MovieDateTime movieDateTime = new MovieDateTime(dateTime[0], dateTime[1]);
                    for (MovieDateTime mdt : dateTimes) {
                        if (mdt.date.equals(movieDateTime.date) && mdt.time.equals(movieDateTime.time)) {
                            return; // Ignore if the date and time already exist
                        }
                    }

                    dateTimes.add(movieDateTime);
                    showTimeAdapter.notifyDataSetChanged();
                }
                catch (Exception e) {
                    CustomDialog.showAlertDialog(AddMovieActivity.this, R.drawable.ic_error, "Error", e.getMessage(), false);
                }

            }

            @Override
            public void onNegativeButtonClick(Date date) {

            }
        });

        dateTimeDialogFragment.show(getSupportFragmentManager(), "dialog_time");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            posterUri = data.getData();
            Picasso.get().load(posterUri).into(movieBanner);
        }
    }
    private void editPoster() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
    }

    private void addDirector() {
        Intent intent = new Intent(this, AddPeopleActivity.class);
        intent.putExtra("title", "Add Director");
        launcher.launch(intent);
    }

    private void addActor() {
        Intent intent = new Intent(this, AddPeopleActivity.class);
        intent.putExtra("title", "Add Actor");
        launcher.launch(intent);
    }

    private void initRecyclerView() {
        directors = new ArrayList<>();
        directorRv = findViewById(R.id.directorRecylerView);
        directorRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        directorAdapter = new PersonAdapter(this, directors, true);
        directorRv.setAdapter(directorAdapter);

        actors = new ArrayList<>();
        actorRv = findViewById(R.id.actorRecyclerView);
        actorRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        actorAdapter = new PersonAdapter(this, actors, true);
        actorRv.setAdapter(actorAdapter);

        dateTimes = new ArrayList<>();
        showTimeRv = findViewById(R.id.showTimeRecyclerView);
        showTimeRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        showTimeAdapter = new MovieDateTimeAdapter(this, dateTimes);
        showTimeRv.setAdapter(showTimeAdapter);
    }
}