package com.atividadeAvaliativaLP3.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "professor")
@PrimaryKeyJoinColumn(name = "usuario_id")
public class Professor extends Usuario {

    @Column(name = "instituicao", length = 255)
    private String instituicao;

    public Professor() {
        super();
    }

    public Professor(String authId, String nome, String email, String fotoPerfil, String instituicao, String biografia) {
        super(authId, nome, email, fotoPerfil);
        this.instituicao = instituicao;
        // A biografia é armazenada em usuario.descricao (evita coluna duplicada).
        this.setDescricao(biografia);
    }

    public String getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(String instituicao) {
        this.instituicao = instituicao;
    }

    /**
     * @deprecated Use {@link Usuario#getDescricao()}. Mantido por compatibilidade com o código
     * que já chamava "biografia" (a coluna antiga `professor.biografia` não é mais utilizada).
     */
    @Deprecated
    public String getBiografia() {
        return getDescricao();
    }

    /** @deprecated Use {@link Usuario#setDescricao(String)}. */
    @Deprecated
    public void setBiografia(String biografia) {
        setDescricao(biografia);
    }
}
