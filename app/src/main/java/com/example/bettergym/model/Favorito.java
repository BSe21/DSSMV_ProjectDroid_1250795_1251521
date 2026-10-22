package com.example.bettergym.model;

public class Favorito {
    private String id;
    private String user_id;
    private String ginasio_id;
    private String data_adicionado;
    private Ginasio ginasio;

    public Favorito() {}

    public Favorito(String id, String user_id, String ginasio_id, String data_adicionado) {
        this.id = id;
        this.user_id = user_id;
        this.ginasio_id = ginasio_id;
        this.data_adicionado = data_adicionado;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getGinasio_id() { return ginasio_id; }
    public void setGinasio_id(String ginasio_id) { this.ginasio_id = ginasio_id; }

    public String getData_adicionado() { return data_adicionado; }
    public void setData_adicionado(String data_adicionado) { this.data_adicionado = data_adicionado; }

    public Ginasio getGinasio() { return ginasio; }
    public void setGinasio(Ginasio ginasio) { this.ginasio = ginasio; }
}
