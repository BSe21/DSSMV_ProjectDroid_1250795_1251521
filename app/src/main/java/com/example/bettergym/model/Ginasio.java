package com.example.bettergym.model;

import java.io.Serializable;

public class Ginasio implements Serializable {
    private String id;
    private String nome;
    private String google_place_id;
    private double latitude;
    private double longitude;

    public Ginasio() {}

    public Ginasio(String id, String nome, String google_place_id, double latitude, double longitude) {
        this.id = id;
        this.nome = nome;
        this.google_place_id = google_place_id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getGoogle_place_id() { return google_place_id; }
    public void setGoogle_place_id(String google_place_id) { this.google_place_id = google_place_id; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}