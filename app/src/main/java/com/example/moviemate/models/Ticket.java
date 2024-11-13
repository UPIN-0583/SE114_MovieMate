package com.example.moviemate.models;

import java.io.Serializable;
import java.util.List;

public class Ticket implements Serializable {
    private String TicketID;
    private String movie; // Movie ID
    private String cinema; // Cinema ID
    private String date;
    private String time;
    private int totalPrice;
    private List<String> seats;
    private String moviePoster;
    private String movieTitle;
    private String cinemaLocation;
    private String cinemaAddress; // Địa chỉ rạp, nếu có
    private String BarcodeImage;

    public Ticket() {}

    // Getters and setters for each field
    // ...

    public String getTicketID() {
        return TicketID;
    }

    public void setTicketID(String ticketID) {
        this.TicketID = ticketID;
    }

    public String getMovie() {
        return movie;
    }

    public void setMovie(String movie) {
        this.movie = movie;
    }

    public String getCinema() {
        return cinema;
    }

    public void setCinema(String cinema) {
        this.cinema = cinema;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<String> getSeats() {
        return seats;
    }

    public void setSeats(List<String> seats) {
        this.seats = seats;
    }

    public String getMoviePoster() {
        return moviePoster;
    }

    public void setMoviePoster(String moviePoster) {
        this.moviePoster = moviePoster;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getCinemaLocation() {
        return cinemaLocation;
    }

    public void setCinemaLocation(String cinemaLocation) {
        this.cinemaLocation = cinemaLocation;
    }

    public String getCinemaAddress() {
        return cinemaAddress;
    }

    public void setCinemaAddress(String cinemaAddress) {
        this.cinemaAddress = cinemaAddress;
    }

    public String getBarcodeImage() {
        return BarcodeImage;
    }

    public void setBarcodeImage(String barcodeImage) {
        this.BarcodeImage = barcodeImage;
    }
}
