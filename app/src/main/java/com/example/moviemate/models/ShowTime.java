package com.example.moviemate.models;

import java.io.Serializable;
import java.util.Map;

public class ShowTime implements Serializable {
    private String showTimeID;  // ID của suất chiếu
    private Integer movieID;    // ID của phim
    private Map<String, String> seats;  // Trạng thái ghế (ví dụ: "A1": "available")
    private String time;        // Giờ chiếu
    private String day;         // Ngày chiếu

    // Constructor mặc định
    public ShowTime() {
    }

    // Constructor đầy đủ để khởi tạo ShowTime
    public ShowTime(String showTimeID, Integer movieID, Map<String, String> seats, String time, String day) {
        this.showTimeID = showTimeID;
        this.movieID = movieID;
        this.seats = seats;
        this.time = time;
        this.day = day;
    }

    // Getters và Setters
    public String getShowTimeID() {
        return showTimeID;
    }

    public void setShowTimeID(String showTimeID) {
        this.showTimeID = showTimeID;
    }

    public Integer getMovieID() {
        return movieID;
    }

    public void setMovieID(Integer movieID) {
        this.movieID = movieID;
    }

    public Map<String, String> getSeats() {
        return seats;
    }

    public void setSeats(Map<String, String> seats) {
        this.seats = seats;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}
