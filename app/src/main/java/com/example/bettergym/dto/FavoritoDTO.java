package com.example.bettergym.dto;

public class FavoritoDTO {
    private String id;
    private String user_id;
    private String ginasio_id;
    private String data_adicionado;
    private PlaceGymDTO ginasio;

    // Construtor vazio obrigatório para o Firebase.
    public FavoritoDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getGinasio_id() { return ginasio_id; }
    public void setGinasio_id(String ginasio_id) { this.ginasio_id = ginasio_id; }

    public String getData_adicionado() { return data_adicionado; }
    public void setData_adicionado(String data_adicionado) { this.data_adicionado = data_adicionado; }

    public PlaceGymDTO getGinasio() { return ginasio; }
    public void setGinasio(PlaceGymDTO ginasio) { this.ginasio = ginasio; }
}
