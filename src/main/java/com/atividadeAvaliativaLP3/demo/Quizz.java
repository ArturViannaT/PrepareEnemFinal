package com.atividadeAvaliativaLP3.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "Quizz")
public class Quizz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quizz_id")
    private Integer quizzId;

    @Column(name = "nomeQuizz", length = 255, nullable = false)
    private String nomeQuizz;

    @Column(name = "materia", length = 100, nullable = false)
    private String materia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;

    @Column(name = "recompensa_pontos")
    private Integer recompensaPontos;

    @Column(name = "limite_tempo")
    private Integer limiteTempo;

    @Column(name = "dificuldade_geral", length = 20)
    private String dificuldadeGeral;

    @Column(name = "url_video", length = 500)
    private String urlVideo;

    public Quizz() {
        this.recompensaPontos = 0;
        this.limiteTempo = 0;
        this.dificuldadeGeral = "MEDIO";
    }

    public Quizz(String nomeQuizz, String materia, Usuario autor, Integer recompensaPontos, Integer limiteTempo, String dificuldadeGeral, String urlVideo) {
        this.nomeQuizz = nomeQuizz;
        this.materia = materia;
        this.autor = autor;
        this.recompensaPontos = recompensaPontos;
        this.limiteTempo = limiteTempo;
        this.dificuldadeGeral = dificuldadeGeral;
        this.urlVideo = urlVideo;
    }

    public Integer getQuizzId() {
        return quizzId;
    }

    public void setQuizzId(Integer quizzId) {
        this.quizzId = quizzId;
    }

    public String getNomeQuizz() {
        return nomeQuizz;
    }

    public void setNomeQuizz(String nomeQuizz) {
        this.nomeQuizz = nomeQuizz;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public Integer getRecompensaPontos() {
        return recompensaPontos;
    }

    public void setRecompensaPontos(Integer recompensaPontos) {
        this.recompensaPontos = recompensaPontos;
    }

    public Integer getLimiteTempo() {
        return limiteTempo;
    }

    public void setLimiteTempo(Integer limiteTempo) {
        this.limiteTempo = limiteTempo;
    }

    public String getDificuldadeGeral() {
        return dificuldadeGeral;
    }

    public void setDificuldadeGeral(String dificuldadeGeral) {
        this.dificuldadeGeral = dificuldadeGeral;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }
}
