package com.atividadeAvaliativaLP3.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "Conteudo")
public class Conteudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer conteudoId;

    @Column(name = "titulo", length = 255, nullable = false)
    private String titulo;

    @Column(name = "materia", length = 100, nullable = false)
    private String materia;

    @Column(name = "texto", columnDefinition = "TEXT", nullable = false)
    private String texto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(name = "url_video", length = 500)
    private String urlVideo;

    @Column(name = "visibilidade", length = 20, nullable = false)
    private String visibilidade; // PUBLICO ou PRIVADO

    public Conteudo() {
        this.visibilidade = "PUBLICO";
    }

    public Conteudo(String titulo, String materia, String texto, Usuario autor, String urlVideo, String visibilidade) {
        this.titulo = titulo;
        this.materia = materia;
        this.texto = texto;
        this.autor = autor;
        this.urlVideo = urlVideo;
        this.visibilidade = visibilidade;
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

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
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
}
