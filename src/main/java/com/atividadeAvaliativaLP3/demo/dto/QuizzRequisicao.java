package com.atividadeAvaliativaLP3.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public class QuizzRequisicao {

    @NotBlank(message = "O nome do quizz é obrigatório") // RNF5
    private String nomeQuizz;

    @NotBlank(message = "A matéria é obrigatória") // RNF5
    private String materia;

    private Integer recompensaPontos;

    @NotNull(message = "O tempo limite é obrigatório") // RNF5
    private Integer limiteTempo; // em segundos (RF13.12.2)

    @NotBlank(message = "A dificuldade geral é obrigatória") // RNF5
    @Pattern(regexp = "^(FACIL|MEDIO|MEDIA|INTERMEDIARIA|INTERMEDIARIO|DIFICIL)$",
            message = "A dificuldade geral deve ser FACIL, INTERMEDIARIA ou DIFICIL")
    private String dificuldadeGeral;

    private String urlVideo; // RF2.1.1 vídeo opcional

    @NotEmpty(message = "O quizz deve conter pelo menos uma questão")
    @Valid
    private List<QuestaoRequisicao> questoes;

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

    public List<QuestaoRequisicao> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(List<QuestaoRequisicao> questoes) {
        this.questoes = questoes;
    }
}
