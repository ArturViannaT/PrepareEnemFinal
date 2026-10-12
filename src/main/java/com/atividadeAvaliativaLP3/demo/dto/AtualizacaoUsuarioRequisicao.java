package com.atividadeAvaliativaLP3.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AtualizacaoUsuarioRequisicao {

    @NotBlank(message = "O nome é obrigatório") // RNF5
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Size(max = 500, message = "A URL da foto de perfil deve ter no máximo 500 caracteres")
    private String fotoPerfil;

    private String instituicao;

    /** Descrição/biografia do usuário (pode ser editada livremente). */
    @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
    private String descricao;

    /** Campo legado: aceito para compatibilidade, gravado em `descricao`. */
    private String biografia;

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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
