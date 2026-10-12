package com.atividadeAvaliativaLP3.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SubmissaoQuizRequisicao {

    @NotEmpty(message = "Não é possível submeter um quiz sem respostas") // RNF6
    @Valid
    private List<RespostaQuizRequisicao> respostas;

    @NotNull(message = "O tempo de realização é obrigatório") // RNF5, RF15
    private Integer tempoGasto; // em segundos

    public List<RespostaQuizRequisicao> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<RespostaQuizRequisicao> respostas) {
        this.respostas = respostas;
    }

    public Integer getTempoGasto() {
        return tempoGasto;
    }

    public void setTempoGasto(Integer tempoGasto) {
        this.tempoGasto = tempoGasto;
    }
}
