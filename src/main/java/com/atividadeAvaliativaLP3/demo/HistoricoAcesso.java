package com.atividadeAvaliativaLP3.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_acesso")
public class HistoricoAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer historicoId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "conteudo_id")
    private Integer conteudoId;

    @Column(name = "acessado_em", nullable = false)
    private LocalDateTime acessadoEm;

    public HistoricoAcesso() {
        this.acessadoEm = LocalDateTime.now();
    }

    public HistoricoAcesso(Usuario usuario, Integer conteudoId) {
        this.usuario = usuario;
        this.conteudoId = conteudoId;
        this.acessadoEm = LocalDateTime.now();
    }

    public Integer getHistoricoId() {
        return historicoId;
    }

    public void setHistoricoId(Integer historicoId) {
        this.historicoId = historicoId;
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

    public LocalDateTime getAcessadoEm() {
        return acessadoEm;
    }

    public void setAcessadoEm(LocalDateTime acessadoEm) {
        this.acessadoEm = acessadoEm;
    }
}
