package com.atividadeAvaliativaLP3.demo.dto;

public class PerfilUsuarioResposta {

    private String email;
    private String nome;
    private String tipo; // "ALUNO" ou "PROFESSOR"
    private Double pontuacao; // null para professor
    private Integer nivel; // null para professor
    private Double theta; // null para professor
    private String matricula; // null para professor
    private Boolean verificado; // null para aluno

    public PerfilUsuarioResposta() {
    }

    // Construtor para Aluno
    public PerfilUsuarioResposta(String email, String nome, double pontuacao, int nivel, double theta, String matricula) {
        this.email = email;
        this.nome = nome;
        this.tipo = "ALUNO";
        this.pontuacao = pontuacao;
        this.nivel = nivel;
        this.theta = theta;
        this.matricula = matricula;
        this.verificado = null;
    }

    // Construtor para Professor
    public PerfilUsuarioResposta(String email, String nome, boolean verificado) {
        this.email = email;
        this.nome = nome;
        this.tipo = "PROFESSOR";
        this.pontuacao = null;
        this.nivel = null;
        this.theta = null;
        this.matricula = null;
        this.verificado = verificado;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

    public Double getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(Double pontuacao) {
        this.pontuacao = pontuacao;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }

    public Double getTheta() {
        return theta;
    }

    public void setTheta(Double theta) {
        this.theta = theta;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public Boolean getVerificado() {
        return verificado;
    }

    public void setVerificado(Boolean verificado) {
        this.verificado = verificado;
    }
}
