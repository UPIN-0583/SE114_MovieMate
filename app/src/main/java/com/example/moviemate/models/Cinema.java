package com.example.moviemate.models;

import java.util.List;
import java.util.Map;

public class Cinema {
    private int CinemaID;
    private String CinemaName;
    private String Address;
    private String BrandLogo;
    private List<ShowTime> showTimeList;// Lưu trữ giờ chiếu theo ngày và giờ sử dụng ShowTime trực tiếp

    // Constructor mặc định
    public Cinema() {
    }

    // Getters và Setters
    public int getCinemaID() {
        return CinemaID;
    }

    public void setCinemaID(int cinemaID) {
        CinemaID = cinemaID;
    }

    public String getCinemaName() {
        return CinemaName;
    }

    public void setCinemaName(String cinemaName) {
        CinemaName = cinemaName;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getBrandLogo() {
        return BrandLogo;
    }

    public void setBrandLogo(String brandLogo) {
        BrandLogo = brandLogo;
    }

    public List<ShowTime> getShowTimeList() {
        return showTimeList;
    }

    public void setShowTimeList(List<ShowTime> showTimeList) {
        this.showTimeList = showTimeList;
    }
}
