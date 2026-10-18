package com.example.bettergym.model;

import java.io.Serializable;

public class Ginasio implements Serializable {
    private String id;
    private String nome;
    private double latitude;
    private double longitude;
    private double rating;

    public Ginasio() {}

    public Ginasio(String google_place_id, String nome, double latitude, double longitude, double rating) {
        this.id = google_place_id;
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }


    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}