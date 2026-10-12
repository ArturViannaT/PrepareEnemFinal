package com.atividadeAvaliativaLP3.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AvaliacaoRequisicao {

    @NotNull(message = "A nota em estrelas é obrigatória") // RNF5
    @Min(value = 1, message = "A avaliação mínima é de 1 estrela") // RF4.3.1
    @Max(value = 5, message = "A avaliação máxima é de 5 estrelas") // RF4.3.1
    private Integer estrelas;

    public Integer getEstrelas() {
        return estrelas;
    }

    public void setEstrelas(Integer estrelas) {
        this.estrelas = estrelas;
    }
}
