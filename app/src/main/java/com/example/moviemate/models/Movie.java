package com.example.moviemate.models;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Movie implements Serializable {
    private int MovieID;
    private String Title;
    private String Description;
    private String Poster;
    private String Trailer;
    private String Language;
    private String Rating;
    private double Imdb;
    private String Time;
    private int Year;
    private int SeatPrice;
    private String Status;
    private List<Person> Actor;
    private List<Person> Director;
    private List<String> Genre;

    // Constructor mặc định
    public Movie() {
    }

    // Constructor đầy đủ
    public Movie(int movieID, String title, String description, String poster, String trailer,
                 String language, String rating, double imdb, String time, int year, int seatPrice,
                 String status, List<Person> actor, List<Person> director, List<String> genre, Map<String, Map<String, ShowTime>> showTimes) {
        this.MovieID = movieID;
        this.Title = title;
        this.Description = description;
        this.Poster = poster;
        this.Trailer = trailer;
        this.Language = language;
        this.Rating = rating;
        this.Imdb = imdb;
        this.Time = time;
        this.Year = year;
        this.SeatPrice = seatPrice;
        this.Status = status;
        this.Actor = actor;
        this.Director = director;
        this.Genre = genre;
    }

    // Getters và Setters
    public int getMovieID() {
        return MovieID;
    }

    public void setMovieID(int movieID) {
        MovieID = movieID;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public int getSeatPrice() {
        return SeatPrice;
    }

    public void setSeatPrice(int seatPrice) {
        SeatPrice = seatPrice;
    }
    public void setDescription(String description) {
        Description = description;
    }

    public String getPoster() {
        return Poster;
    }

    public void setPoster(String poster) {
        Poster = poster;
    }

    public String getTrailer() {
        return Trailer;
    }

    public void setTrailer(String trailer) {
        Trailer = trailer;
    }

    public String getLanguage() {
        return Language;
    }

    public void setLanguage(String language) {
        Language = language;
    }

    public String getRating() {
        return Rating;
    }

    public void setRating(String rating) {
        Rating = rating;
    }

    public double getImdb() {
        return Imdb;
    }

    public void setImdb(double imdb) {
        Imdb = imdb;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public int getYear() {
        return Year;
    }

    public void setYear(int year) {
        Year = year;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public List<Person> getActor() {
        return Actor;
    }

    public void setActor(List<Person> actor) {
        Actor = actor;
    }

    public List<Person> getDirector() {
        return Director;
    }

    public void setDirector(List<Person> director) {
        Director = director;
    }

    public List<String> getGenre() {
        return Genre;
    }

    public void setGenre(List<String> genre) {
        Genre = genre;
    }


}
