package com.atividadeAvaliativaLP3.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "aluno")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Aluno extends Usuario {

    @Column(name = "pontos")
    private double pontos;

    @Column(name = "nivel")
    private int nivel;

    @Column(name = "xp")
    private int xp;

    @Column(name = "quizzes_resolvidos")
    private int quizzesResolvidos;

    public Aluno() {
        super();
        this.pontos = 0.0;
        this.nivel = 1;
        this.xp = 0;
        this.quizzesResolvidos = 0;
    }

    public Aluno(String authId, String nome, String email, String fotoPerfil) {
        super(authId, nome, email, fotoPerfil);
        this.pontos = 0.0;
        this.nivel = 1;
        this.xp = 0;
        this.quizzesResolvidos = 0;
    }

    public double getPontos() {
        return pontos;
    }

    public void setPontos(double pontos) {
        this.pontos = pontos;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getQuizzesResolvidos() {
        return quizzesResolvidos;
    }

    public void setQuizzesResolvidos(int quizzesResolvidos) {
        this.quizzesResolvidos = quizzesResolvidos;
    }
}
