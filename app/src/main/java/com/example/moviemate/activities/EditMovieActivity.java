package com.example.moviemate.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviemate.R;
import com.example.moviemate.adapters.MovieDateTimeAdapter;
import com.example.moviemate.adapters.PersonAdapter;
import com.example.moviemate.models.Movie;
import com.example.moviemate.models.MovieDateTime;
import com.example.moviemate.models.Person;
import com.example.moviemate.utils.CustomDialog;
import com.kunzisoft.switchdatetime.SwitchDateTimeDialogFragment;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class EditMovieActivity extends AppCompatActivity {
    private Movie movie;
    private static final int PICK_IMAGE_REQUEST = 1;

    ImageButton backButton, editPosterButton;
    Button saveButton;
    ImageView movieBanner;
    TextView addDirectorTv, addActorTv, addShowTimeTv;
    EditText timeHourEt, timeMinuteEt, yearEt, languageEt, ratingEt, statusEt, titleEt, genreEt, storylineEt,  trailerEt;
    Spinner statusSpinner;
    RecyclerView directorRv, actorRv, showTimeRv;
    private PersonAdapter directorAdapter, actorAdapter;
    private MovieDateTimeAdapter showTimeAdapter;
    private List<Person> directors, actors;
    private List<MovieDateTime> dateTimes;
    private List<String> showTimes;
    private Uri posterUri;

    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_movie);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        movie = (Movie) intent.getSerializableExtra("movie");
        if (movie == null) finish();
        setupViews();
        loadMovieData();
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
        saveButton.setOnClickListener(v -> {});

        movieBanner = findViewById(R.id.movieBanner);

        timeHourEt = findViewById(R.id.timeHourEditText);
        timeMinuteEt = findViewById(R.id.timeMinuteEditText);
        yearEt = findViewById(R.id.yearEditText);
        languageEt = findViewById(R.id.languageEditText);
        ratingEt = findViewById(R.id.ratingEditText);
//        statusEt = findViewById(R.id.);
        titleEt = findViewById(R.id.titleEditText);
        genreEt = findViewById(R.id.genreTextEdit);
        storylineEt = findViewById(R.id.storylineTextEdit);
        trailerEt = findViewById(R.id.trailerUrlTextEdit);
        statusSpinner = findViewById(R.id.statusSpinner);
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
                    dateTimes.add(new MovieDateTime(dateTime[0], dateTime[1]));
                    showTimeAdapter.notifyDataSetChanged();
                }
                catch (Exception e) {
                    CustomDialog.showAlertDialog(EditMovieActivity.this, R.drawable.ic_error, "Error", e.getMessage(), false);
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

    private void loadMovieData() {
        Picasso.get().load(movie.getPoster()).into(movieBanner);
        titleEt.setText(movie.getTitle());
        genreEt.setText(String.join(", ", movie.getGenre()));
        ratingEt.setText(movie.getRating());
        languageEt.setText(movie.getLanguage());
        storylineEt.setText(movie.getDescription());
        trailerEt.setText(movie.getTrailer());
        String hour = movie.getTime().split("h")[0].trim();
        String minute = movie.getTime().split("h")[1].split("m")[0].trim();
        timeHourEt.setText(hour);
        timeMinuteEt.setText(minute);
        yearEt.setText(String.valueOf(movie.getYear()));

        directors = movie.getDirector();
        directorRv = findViewById(R.id.directorRecylerView);
        directorRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        directorAdapter = new PersonAdapter(this, directors, true);
        directorRv.setAdapter(directorAdapter);

        actors = movie.getActor();
        actorRv = findViewById(R.id.actorRecyclerView);
        actorRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        actorAdapter = new PersonAdapter(this, actors, true);
        actorRv.setAdapter(actorAdapter);

        dateTimes = new ArrayList<>();
        showTimeRv = findViewById(R.id.showTimeRecyclerView);
        showTimeRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        showTimeAdapter = new MovieDateTimeAdapter(this, dateTimes);
        showTimeRv.setAdapter(showTimeAdapter);

        statusSpinner.setSelection(movie.getStatus().equals("Now Playing") ? 0 : 1);
    }
}