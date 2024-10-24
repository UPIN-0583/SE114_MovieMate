package com.example.moviemate.models;

import java.io.Serializable;

public class Person implements Serializable {
    private String Name;
    private String PicUrl;

    // Constructor mặc định (cần thiết khi sử dụng Firebase hoặc bất kỳ library nào yêu cầu Constructor trống)
    public Person() {
    }

    // Constructor để khởi tạo đối tượng Person
    public Person(String name, String picUrl) {
        this.Name = name;
        this.PicUrl = picUrl;
    }

    // Getter và Setter cho tên và URL ảnh
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
