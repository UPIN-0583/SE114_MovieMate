package com.example.moviemate;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import io.github.cdimascio.dotenv.Dotenv;

public class MovieMateApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Dotenv dotenv = Dotenv.configure().directory("/assets").filename("env").load();
        HashMap<String, String> config = new HashMap<>();
        config.put("cloud_name", dotenv.get("CLOUDINARY_CLOUD_NAME"));
        config.put("api_key", dotenv.get("CLOUDINARY_API_KEY"));
        config.put("api_secret", dotenv.get("CLOUDINARY_API_SECRET"));

        MediaManager.init(this, config);
    }
}
