package com.example.bettergym.dto;
import java.util.List;
import java.util.ArrayList;

public class PlaceGymDTO {
    private String id;
    private String nome;
    private double latitude;
    private double longitude;
    private double rating;
    private boolean abertoAgora;
    private String morada;
    private int numeroAvaliacoes;

    private List<AvaliacaoDTO> avaliacoes = new ArrayList<>();
    private String referenciaFoto;
    private List<String> fotos = new ArrayList<>();

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

    public PlaceGymDTO(String id, String nome, double latitude, double longitude, double rating,
                       boolean abertoAgora, String morada, int numeroAvaliacoes, String referenciaFoto) {
        this(id, nome, latitude, longitude, rating, abertoAgora);
        this.morada = morada;
        this.numeroAvaliacoes = numeroAvaliacoes;
        this.referenciaFoto = referenciaFoto;
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

    public List<AvaliacaoDTO> getAvaliacoes() { return avaliacoes; }
    public void setAvaliacoes(List<AvaliacaoDTO> avaliacoes) { this.avaliacoes = avaliacoes; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getMorada() { return morada; }
    public void setMorada(String morada) { this.morada = morada; }

    public int getNumeroAvaliacoes() { return numeroAvaliacoes; }
    public void setNumeroAvaliacoes(int numeroAvaliacoes) { this.numeroAvaliacoes = numeroAvaliacoes; }

    public String getReferenciaFoto() { return referenciaFoto; }
    public void setReferenciaFoto(String referenciaFoto) { this.referenciaFoto = referenciaFoto; }

    public List<String> getFotos() { return fotos; }
    public void setFotos(List<String> fotos) { this.fotos = fotos; }
}
