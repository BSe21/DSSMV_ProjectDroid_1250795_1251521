package com.example.bettergym.dto;

public class PlaceGymDTO {
    private String id;
    private String nome;
    private double latitude;
    private double longitude;
    private double rating;

    private boolean abertoAgora;

    // Construtor vazio (sempre boa prática, especialmente se usares Firebase mais tarde)
    public PlaceGymDTO() {}

    // Construtor com os campos todos
    public PlaceGymDTO(String id, String nome, double latitude, double longitude, double rating, boolean abertoAgora) {
        this.id = id;
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.abertoAgora = abertoAgora;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public boolean isAbertoAgora() { return abertoAgora; }
    public void setAbertoAgora(boolean abertoAgora) { this.abertoAgora = abertoAgora; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}