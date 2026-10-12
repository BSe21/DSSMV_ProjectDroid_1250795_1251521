package com.example.bettergym.model;

public class Ginasio {
    private String id;
    private String nome;
    private String google_place_id;

    public Ginasio() {}

    public Ginasio(String id, String nome, String google_place_id) {
        this.id = id;
        this.nome = nome;
        this.google_place_id = google_place_id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getGoogle_place_id() { return google_place_id; }
    public void setGoogle_place_id(String google_place_id) { this.google_place_id = google_place_id; }
}