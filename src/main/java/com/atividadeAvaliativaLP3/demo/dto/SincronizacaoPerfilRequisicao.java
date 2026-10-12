package com.atividadeAvaliativaLP3.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class SincronizacaoPerfilRequisicao {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O tipo de usuário é obrigatório (ALUNO ou PROFESSOR)")
    private String tipo; // "ALUNO" ou "PROFESSOR"

    private String fotoPerfil;
    private String instituicao;
    private String biografia;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(String instituicao) {
        this.instituicao = instituicao;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }
}
