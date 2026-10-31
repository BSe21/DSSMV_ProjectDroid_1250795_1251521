package com.example.bettergym.dto;

import java.util.ArrayList;
import java.util.List;

public class PerfilDTO {
    private String nome_completo;
    private String username;
    private String email;
    private String numero_telemovel;
    private String data_nascimento;
    private String data_registo;
    private String foto_url;
    private List<VisitaDTO> historico_ginasios_visitados = new ArrayList<>();

    // Construtor vazio obrigatório para o Firebase
    public PerfilDTO() {}

    // Getters e Setters
    public String getNome_completo() { return nome_completo; }
    public void setNome_completo(String nome_completo) { this.nome_completo = nome_completo; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNumero_telemovel() { return numero_telemovel; }
    public void setNumero_telemovel(String numero_telemovel) { this.numero_telemovel = numero_telemovel; }

    public String getData_nascimento() { return data_nascimento; }
    public void setData_nascimento(String data_nascimento) { this.data_nascimento = data_nascimento; }

    public String getData_registo() { return data_registo; }
    public void setData_registo(String data_registo) { this.data_registo = data_registo; }

    public String getFoto_url() { return foto_url; }
    public void setFoto_url(String foto_url) { this.foto_url = foto_url; }

    public List<VisitaDTO> getHistorico_ginasios_visitados() { return historico_ginasios_visitados; }
    public void setHistorico_ginasios_visitados(List<VisitaDTO> historico_ginasios_visitados) {
        this.historico_ginasios_visitados = historico_ginasios_visitados != null ? historico_ginasios_visitados : new ArrayList<>();
    }
}
