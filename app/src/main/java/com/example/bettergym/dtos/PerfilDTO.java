package com.example.bettergym.dtos;

import com.example.bettergym.model.Perfil;
import com.google.firebase.firestore.PropertyName;

public class PerfilDTO {

    private String id;
    private String nomeCompleto;
    private String username;
    private String email;
    private String dataNascimento;
    private String numeroTelemovel;
    private String fotoUrl;
    private String dataRegisto;

    // Construtor sem parâmetros, obrigatório para o Firestore criar o objeto.
    public PerfilDTO() {
    }

    //Converte o modelo de domínio num DTO, pronto a ser enviado para o Firestore.

    public static PerfilDTO fromModel(Perfil perfil) {
        PerfilDTO dto = new PerfilDTO();
        dto.setId(perfil.getId());
        dto.setNomeCompleto(perfil.getNome_completo());
        dto.setUsername(perfil.getUsername());
        dto.setEmail(perfil.getEmail());
        dto.setDataNascimento(perfil.getData_nascimento());
        dto.setNumeroTelemovel(perfil.getNumero_telemovel());
        dto.setFotoUrl(perfil.getFoto_url());
        dto.setDataRegisto(perfil.getData_registo());
        return dto;
    }

    //Converte este DTO no modelo de domínio.
    public Perfil toModel() {
        return new Perfil(id, nomeCompleto, username, email, dataNascimento,
                numeroTelemovel, fotoUrl, dataRegisto);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("nome_completo")
    public String getNomeCompleto() {
        return nomeCompleto;
    }

    @PropertyName("nome_completo")
    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @PropertyName("data_nascimento")
    public String getDataNascimento() {
        return dataNascimento;
    }

    @PropertyName("data_nascimento")
    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    @PropertyName("numero_telemovel")
    public String getNumeroTelemovel() {
        return numeroTelemovel;
    }

    @PropertyName("numero_telemovel")
    public void setNumeroTelemovel(String numeroTelemovel) {
        this.numeroTelemovel = numeroTelemovel;
    }

    @PropertyName("foto_url")
    public String getFotoUrl() {
        return fotoUrl;
    }

    @PropertyName("foto_url")
    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    @PropertyName("data_registo")
    public String getDataRegisto() {
        return dataRegisto;
    }

    @PropertyName("data_registo")
    public void setDataRegisto(String dataRegisto) {
        this.dataRegisto = dataRegisto;
    }

    //Devolve os dados do DTO numa String formatada
    @Override
    public String toString() {
        return "PerfilDTO{id=" + id
                + ", nomeCompleto=" + nomeCompleto
                + ", username=" + username
                + ", email=" + email
                + ", dataNascimento=" + dataNascimento
                + ", numeroTelemovel=" + numeroTelemovel
                + ", fotoUrl=" + fotoUrl
                + ", dataRegisto=" + dataRegisto + "}";
    }
}