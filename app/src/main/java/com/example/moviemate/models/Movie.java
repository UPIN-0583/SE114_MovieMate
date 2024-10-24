package com.example.moviemate.models;

import java.util.List;

public class Movie {
    private String Title;
    private List<String> Genre;
    private String Rating;
    private String Language;
    private String Time;
    private String Description;
    private String Poster;
    private List<Cinema> Cinemas;
    private List<Person> Actor;
    private List<Person> Director;
    private String Trailer;


    // Constructor mặc định (cần thiết để sử dụng với Firebase)
    public Movie() {
    }


    // Getter và Setter
    public String getTitle() {
        return Title;
    }

    public List<String> getGenre() {
        return Genre;
    }


    public String getRating() {
        return Rating;
    }


    public String getLanguage() {
        return Language;
    }


    public String getTime() {
        return Time;
    }


    public String getDescription() {
        return Description;
    }


    public String getPoster() {
        return Poster;
    }


    public List<Cinema> getCinemas() {
        return Cinemas;
    }

    public List<Person> getActor() {
        return Actor;
    }

    public List<Person> getDirector() {
        return Director;
    }

    public String getTrailer(){
        return Trailer;
    }


}
