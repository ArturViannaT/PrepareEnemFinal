package com.atividadeAvaliativaLP3.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class QuestaoRequisicao {

    /**
     * Preenchido apenas na EDIÇÃO: identifica uma questão já existente do quiz.
     * Quando ausente, a questão é criada como nova.
     */
    private Integer questaoId;

    @NotBlank(message = "A pergunta da questão é obrigatória") // RNF5
    private String pergunta;

    @NotBlank(message = "A alternativa A é obrigatória") // RNF5
    private String alternativaA;

    @NotBlank(message = "A alternativa B é obrigatória") // RNF5
    private String alternativaB;

    @NotBlank(message = "A alternativa C é obrigatória") // RNF5
    private String alternativaC;

    @NotBlank(message = "A alternativa D é obrigatória") // RNF5
    private String alternativaD;

    @NotBlank(message = "A alternativa E é obrigatória") // RNF5
    private String alternativaE;

    @NotBlank(message = "A alternativa correta é obrigatória") // RNF5
    @Pattern(regexp = "^[A-E]$", message = "A alternativa correta deve ser uma letra de A a E")
    private String alternativaCorreta;

    @NotBlank(message = "A dificuldade inicial é obrigatória")
    @Pattern(regexp = "^(FACIL|MEDIO|MEDIA|INTERMEDIARIA|INTERMEDIARIO|DIFICIL)$",
            message = "A dificuldade inicial deve ser FACIL, INTERMEDIARIA ou DIFICIL")
    private String dificuldade;

    public Integer getQuestaoId() {
        return questaoId;
    }

    public void setQuestaoId(Integer questaoId) {
        this.questaoId = questaoId;
    }

    public String getPergunta() {
        return pergunta;
    }

    public void setPergunta(String pergunta) {
        this.pergunta = pergunta;
    }

    public String getAlternativaA() {
        return alternativaA;
    }

    public void setAlternativaA(String alternativaA) {
        this.alternativaA = alternativaA;
    }

    public String getAlternativaB() {
        return alternativaB;
    }

    public void setAlternativaB(String alternativaB) {
        this.alternativaB = alternativaB;
    }

    public String getAlternativaC() {
        return alternativaC;
    }

    public void setAlternativaC(String alternativaC) {
        this.alternativaC = alternativaC;
    }

    public String getAlternativaD() {
        return alternativaD;
    }

    public void setAlternativaD(String alternativaD) {
        this.alternativaD = alternativaD;
    }

    public String getAlternativaE() {
        return alternativaE;
    }

    public void setAlternativaE(String alternativaE) {
        this.alternativaE = alternativaE;
    }

    public String getAlternativaCorreta() {
        return alternativaCorreta;
    }

    public void setAlternativaCorreta(String alternativaCorreta) {
        this.alternativaCorreta = alternativaCorreta;
    }

    public String getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(String dificuldade) {
        this.dificuldade = dificuldade;
    }
}
