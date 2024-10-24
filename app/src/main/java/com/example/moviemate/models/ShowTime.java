package com.example.moviemate.models;

import java.io.Serializable;
import java.util.Map;

public class ShowTime implements Serializable {
    private String Time;  // Thời gian chiếu
    private int Price;    // Giá vé
    private Map<String, String> Seats;  // Trạng thái ghế (VD: "A1": "available")

    // Constructor mặc định
    public ShowTime() {
    }

    // Constructor đầy đủ để khởi tạo ShowTime
    public ShowTime(String time, int price, Map<String, String> seats) {
        this.Time = time;
        this.Price = price;
        this.Seats = seats;
    }

    // Getters và Setters
    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public int getPrice() {
        return Price;
    }

    public void setPrice(int price) {
        Price = price;
    }

    public Map<String, String> getSeats() {
        return Seats;
    }

    public void setSeats(Map<String, String> seats) {
        Seats = seats;
    }
}
