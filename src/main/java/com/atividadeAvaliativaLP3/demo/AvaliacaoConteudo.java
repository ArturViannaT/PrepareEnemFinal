package com.atividadeAvaliativaLP3.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "AvaliacaoConteudo")
public class AvaliacaoConteudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer avaliacaoId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "conteudo_id", nullable = false)
    private Integer conteudoId;

    @Column(name = "estrelas", nullable = false)
    private int estrelas; // 1 a 5 estrelas

    public AvaliacaoConteudo() {
    }

    public AvaliacaoConteudo(Usuario usuario, Integer conteudoId, int estrelas) {
        this.usuario = usuario;
        this.conteudoId = conteudoId;
        this.estrelas = estrelas;
    }

    public Integer getAvaliacaoId() {
        return avaliacaoId;
    }

    public void setAvaliacaoId(Integer avaliacaoId) {
        this.avaliacaoId = avaliacaoId;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Integer getConteudoId() {
        return conteudoId;
    }

    public void setConteudoId(Integer conteudoId) {
        this.conteudoId = conteudoId;
    }

    public int getEstrelas() {
        return estrelas;
    }

    public void setEstrelas(int estrelas) {
        this.estrelas = estrelas;
    }
}
