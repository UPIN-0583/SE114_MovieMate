package com.example.moviemate.models;

import java.io.Serializable;

public class Banner implements Serializable {
    private String BannerID;
    private String Image;

    // Constructor mặc định
    public Banner() {
    }

    // Constructor đầy đủ
    public Banner(String bannerID, String image) {
        this.BannerID = bannerID;
        this.Image = image;
    }

    // Getters và Setters
    public String getBannerID() {
        return BannerID;
    }

    public void setBannerID(String bannerID) {
        BannerID = bannerID;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }
}
