package com.example.moviemate.utils;

public class OrderIdGenerator {
    public static int generateOrderId() {
        return (int) (Math.random() * 1000000);
    }
}
