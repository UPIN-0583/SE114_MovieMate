package com.example.moviemate;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MovieMateApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
