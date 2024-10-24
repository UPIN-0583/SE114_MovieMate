package com.example.moviemate.models;

import java.io.Serializable;

public class Person implements Serializable {
    private String Name;   // Tên diễn viên hoặc đạo diễn
    private String PicUrl; // URL ảnh của diễn viên hoặc đạo diễn

    // Constructor mặc định (Firebase yêu cầu)
    public Person() {
    }

    // Constructor đầy đủ để khởi tạo Person
    public Person(String name, String picUrl) {
        this.Name = name;
        this.PicUrl = picUrl;
    }

    // Getters và Setters
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getPicUrl() {
        return PicUrl;
    }

    public void setPicUrl(String picUrl) {
        this.PicUrl = picUrl;
    }
}
