package com.atividadeAvaliativaLP3.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ConteudoRequisicao {

    @NotBlank(message = "O título é obrigatório") // RNF5
    private String titulo;

    @NotBlank(message = "A matéria é obrigatória") // RNF5
    private String materia;

    @NotBlank(message = "O texto teórico é obrigatório") // RNF5
    private String texto;

    private String urlVideo; // RF1.1 vídeo opcional

    @NotBlank(message = "A visibilidade é obrigatória") // RNF5
    @Pattern(regexp = "^(PUBLICO|PRIVADO)$", message = "A visibilidade deve ser PUBLICO ou PRIVADO") // RF3
    private String visibilidade;

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
