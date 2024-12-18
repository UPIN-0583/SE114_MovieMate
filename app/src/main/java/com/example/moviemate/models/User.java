package com.example.moviemate.models;

import java.io.Serializable;

public class User implements Serializable {
    public String name;
    public String phone;
    public String email;
    public String avatarUrl; // URL ảnh avatar
    public String role;

    public User() {
        // Constructor mặc định cho Firebase
    }

    public User(String name, String phone, String email, String avatarUrl, String role) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }
}
