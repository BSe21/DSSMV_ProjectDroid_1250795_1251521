package com.example.bettergym.model;

public class Perfil {
    private String id;
    private String nome_completo;
    private String username;
    private String email;
    private String data_nascimento;
    private String numero_telemovel;
    private String foto_url;
    private String data_registo;

    // Construtor vazio obrigatório para o Firestore
    public Perfil() {}

    public Perfil(String id, String nome_completo, String username, String email,
                  String data_nascimento, String numero_telemovel, String foto_url, String data_registo) {
        this.id = id;
        this.nome_completo = nome_completo;
        this.username = username;
        this.email = email;
        this.data_nascimento = data_nascimento;
        this.numero_telemovel = numero_telemovel;
        this.foto_url = foto_url;
        this.data_registo = data_registo;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome_completo() { return nome_completo; }
    public void setNome_completo(String nome_completo) { this.nome_completo = nome_completo; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getData_nascimento() { return data_nascimento; }
    public void setData_nascimento(String data_nascimento) { this.data_nascimento = data_nascimento; }

    public String getNumero_telemovel() { return numero_telemovel; }
    public void setNumero_telemovel(String numero_telemovel) { this.numero_telemovel = numero_telemovel; }

    public String getFoto_url() { return foto_url; }
    public void setFoto_url(String foto_url) { this.foto_url = foto_url; }

    public String getData_registo() { return data_registo; }
    public void setData_registo(String data_registo) { this.data_registo = data_registo; }
}