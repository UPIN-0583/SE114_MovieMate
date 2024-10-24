package com.example.moviemate.models;

import java.io.Serializable;

public class Cinema implements Serializable {
    private String CinemaName;
    private String Address;
    private String BrandLogo;
    //private String Distance;

    // Constructor mặc định không có tham số
    public Cinema() {
    }

    // Constructor đầy đủ
    public Cinema(String CinemaName, String Address, String BrandLogo) {
        this.CinemaName = CinemaName;
        this.Address = Address;
        this.BrandLogo = BrandLogo;
        //this.Distance = Distance;
    }

    // Getters và Setters
    public String getCinemaName() {
        return CinemaName;
    }

    public void setCinemaName(String CinemaName) {
        this.CinemaName = CinemaName;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String Address) {
        this.Address = Address;
    }

    public String getBrandLogo() {
        return BrandLogo;
    }

    public void setBrandLogo(String BrandLogo) {
        this.BrandLogo = BrandLogo;
    }

//    public String getDistance() {
//        return Distance;
//    }
//
//    public void setDistance(String Distance) {
//        this.Distance = Distance;
//    }
}
