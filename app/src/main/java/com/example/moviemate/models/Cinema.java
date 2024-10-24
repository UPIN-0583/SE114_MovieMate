package com.example.moviemate.models;

import java.util.List;

public class Cinema {
    private int CinemaID;
    private String CinemaName;
    private String Address,BrandLogo;
    private List<MovieShowtime> Movies;  // Liên kết tới danh sách các phim chiếu tại rạp và giờ chiếu

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

    public List<MovieShowtime> getMovies() {
        return Movies;
    }

    public void setMovies(List<MovieShowtime> movies) {
        Movies = movies;
    }

    public String getBrandLogo() {
        return BrandLogo;
    }

    public void setBrandLogo(String brandLogo) {
        BrandLogo = brandLogo;
    }
}
