package com.atividadeAvaliativaLP3.demo.dto;

public class ConteudoResposta {

    private Integer conteudoId;
    private String titulo;
    private String materia;
    private String texto;
    private String emailProfessor;
    private Integer autorId;
    private String autorNome;
    private String urlVideo;
    private String visibilidade;
    private double mediaEstrelas;
    private long totalAvaliacoes;

    public ConteudoResposta() {
    }

    public ConteudoResposta(Integer conteudoId, String titulo, String materia, String texto, String emailProfessor,
                            Integer autorId, String autorNome, String urlVideo, String visibilidade,
                            double mediaEstrelas, long totalAvaliacoes) {
        this.conteudoId = conteudoId;
        this.titulo = titulo;
        this.materia = materia;
        this.texto = texto;
        this.emailProfessor = emailProfessor;
        this.autorId = autorId;
        this.autorNome = autorNome;
        this.urlVideo = urlVideo;
        this.visibilidade = visibilidade;
        this.mediaEstrelas = mediaEstrelas;
        this.totalAvaliacoes = totalAvaliacoes;
    }

    public Integer getAutorId() {
        return autorId;
    }

    public void setAutorId(Integer autorId) {
        this.autorId = autorId;
    }

    public String getAutorNome() {
        return autorNome;
    }

    public void setAutorNome(String autorNome) {
        this.autorNome = autorNome;
    }

    public long getTotalAvaliacoes() {
        return totalAvaliacoes;
    }

    public void setTotalAvaliacoes(long totalAvaliacoes) {
        this.totalAvaliacoes = totalAvaliacoes;
    }

    public Integer getConteudoId() {
        return conteudoId;
    }

    public void setConteudoId(Integer conteudoId) {
        this.conteudoId = conteudoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getEmailProfessor() {
        return emailProfessor;
    }

    public void setEmailProfessor(String emailProfessor) {
        this.emailProfessor = emailProfessor;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public String getVisibilidade() {
        return visibilidade;
    }

    public void setVisibilidade(String visibilidade) {
        this.visibilidade = visibilidade;
    }

    public double getMediaEstrelas() {
        return mediaEstrelas;
    }

    public void setMediaEstrelas(double mediaEstrelas) {
        this.mediaEstrelas = mediaEstrelas;
    }
}
