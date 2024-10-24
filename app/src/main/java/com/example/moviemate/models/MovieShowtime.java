package com.example.moviemate.models;

import java.io.Serializable;
import java.util.List;

public class MovieShowtime implements Serializable {
    private int MovieID;             // ID của phim
    private List<ShowTime> ShowTimes; // Danh sách giờ chiếu của phim

    // Constructor mặc định
    public MovieShowtime() {
    }

    // Constructor đầy đủ
    public MovieShowtime(int movieID, List<ShowTime> showTimes) {
        this.MovieID = movieID;
        this.ShowTimes = showTimes;
    }

    // Getters và Setters
    public int getMovieID() {
        return MovieID;
    }

    public void setMovieID(int movieID) {
        MovieID = movieID;
    }

    public List<ShowTime> getShowTimes() {
        return ShowTimes;
    }

    public void setShowTimes(List<ShowTime> showTimes) {
        this.ShowTimes = showTimes;
    }
}
