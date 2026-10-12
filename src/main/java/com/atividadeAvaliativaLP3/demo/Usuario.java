package com.atividadeAvaliativaLP3.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "auth_id", unique = true, nullable = false, length = 100)
    private String authId; // UUID vindo do Supabase Auth

    @Column(name = "nome", length = 255, nullable = false)
    private String nome;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "foto_perfil", length = 500)
    private String fotoPerfil;

    /**
     * Descrição/biografia pública do usuário (RF: visualizar e editar perfil).
     * Fica no usuário base para que ALUNO e PROFESSOR tenham descrição sem duplicar colunas.
     */
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Usuario() {
        this.createdAt = LocalDateTime.now();
    }

    public Usuario(String authId, String nome, String email, String fotoPerfil) {
        this.authId = authId;
        this.nome = nome;
        this.email = email;
        this.fotoPerfil = fotoPerfil;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna o papel do usuário de acordo com o subtipo JPA persistido.
     * Fonte de verdade do papel: o subtipo da linha em `usuario` (Aluno ou Professor).
     */
    public String getTipo() {
        return (this instanceof Professor) ? "PROFESSOR" : "ALUNO";
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
