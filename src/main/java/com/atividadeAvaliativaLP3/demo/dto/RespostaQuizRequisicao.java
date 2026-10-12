package com.atividadeAvaliativaLP3.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class RespostaQuizRequisicao {

    @NotNull(message = "O ID da questão é obrigatório") // RNF5
    private Integer questaoId;

    @NotBlank(message = "Você deve fornecer uma resposta para todas as questões submetidas") // RNF5, RNF6
    @Pattern(regexp = "^[A-E]$", message = "A opção selecionada deve ser de A a E")
    private String opcaoSelecionada;

    public Integer getQuestaoId() {
        return questaoId;
    }

    public void setQuestaoId(Integer questaoId) {
        this.questaoId = questaoId;
    }

    public String getOpcaoSelecionada() {
        return opcaoSelecionada;
    }

    public void setOpcaoSelecionada(String opcaoSelecionada) {
        this.opcaoSelecionada = opcaoSelecionada;
    }
}
