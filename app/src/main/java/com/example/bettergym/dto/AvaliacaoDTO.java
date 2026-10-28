package com.example.bettergym.dto;

public class AvaliacaoDTO {
    private String id;
    private String user_id;
    private String ginasio_id;
    private String nota;
    private String descricao;
    private String data_avaliacao;

    // Construtor vazio obrigatório para o Firebase.
    public AvaliacaoDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getGinasio_id() { return ginasio_id; }
    public void setGinasio_id(String ginasio_id) { this.ginasio_id = ginasio_id; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getData_avaliacao() { return data_avaliacao; }
    public void setData_avaliacao(String data_avaliacao) { this.data_avaliacao = data_avaliacao; }
}