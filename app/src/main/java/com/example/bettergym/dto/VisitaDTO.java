package com.example.bettergym.dto;

public class VisitaDTO {
    private String id;
    private String user_id;
    private String ginasio_id;
    private String data_visita;
    private String nome_ginasio;

    public VisitaDTO() {}

    public VisitaDTO(String ginasio_id, String data_visita) {
        this.ginasio_id = ginasio_id;
        this.data_visita = data_visita;
    }

    public VisitaDTO(String ginasio_id, String data_visita, String nome_ginasio) {
        this.ginasio_id = ginasio_id;
        this.data_visita = data_visita;
        this.nome_ginasio = nome_ginasio;
    }

    public VisitaDTO(String id, String user_id, String ginasio_id, String data_visita) {
        this.id = id;
        this.user_id = user_id;
        this.ginasio_id = ginasio_id;
        this.data_visita = data_visita;
    }

    public VisitaDTO(String id, String user_id, String ginasio_id, String data_visita, String nome_ginasio) {
        this.id = id;
        this.user_id = user_id;
        this.ginasio_id = ginasio_id;
        this.data_visita = data_visita;
        this.nome_ginasio = nome_ginasio;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getGinasio_id() { return ginasio_id; }
    public void setGinasio_id(String ginasio_id) { this.ginasio_id = ginasio_id; }

    public String getData_visita() { return data_visita; }
    public void setData_visita(String data_visita) { this.data_visita = data_visita; }

    public String getNome_ginasio() { return nome_ginasio; }
    public void setNome_ginasio(String nome_ginasio) { this.nome_ginasio = nome_ginasio; }
}
