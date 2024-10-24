package com.example.moviemate.models;

import java.io.Serializable;
import java.util.List;

public class Movie implements Serializable {
    private int MovieID;          // ID của phim
    private String Title;         // Tiêu đề phim
    private String Description;   // Mô tả phim
    private String Poster;        // URL poster phim
    private String Trailer;       // URL trailer phim
    private String Language;      // Ngôn ngữ
    private String Rating;        // Đánh giá độ tuổi
    private double Imdb;          // Đánh giá IMDb
    private String Time;          // Thời lượng phim
    private int Year;             // Năm phát hành
    private String Status;        // Trạng thái (Now Playing, Coming Soon)
    private List<Person> Actor;   // Danh sách diễn viên
    private List<Person> Director; // Danh sách đạo diễn
    private List<String> Genre;   // Danh sách thể loại phim

    // Constructor mặc định
    public Movie() {
    }

    // Constructor đầy đủ
    public Movie(int movieID, String title, String description, String poster, String trailer,
                 String language, String rating, double imdb, String time, int year,
                 String status, List<Person> actor, List<Person> director, List<String> genre) {
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
