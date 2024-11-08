package com.example.moviemate.models;

public class User {
    public String name;
    public String phone;
    public String email;
    public String avatarUrl; // URL ảnh avatar

    public User() {
        // Constructor mặc định cho Firebase
    }

    public User(String name, String phone, String email, String avatarUrl) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }
}
