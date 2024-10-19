package com.example.moviemate.models;

import java.util.List;

public class Movie {
    private String Title;
    private String Poster;
    private String Time;
    private List<String> Genre;

    public Movie() {
        // Required empty constructor for Firebase
    }

    public String getTitle() {
        return Title;
    }

    public String getPoster() {
        return Poster;
    }

    public String getTime() {
        return Time;
    }

    public List<String> getGenre() {
        return Genre;
    }
}

