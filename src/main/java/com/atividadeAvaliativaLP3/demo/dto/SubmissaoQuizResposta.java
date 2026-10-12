package com.atividadeAvaliativaLP3.demo.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Resposta da submissão de um quiz.
 *
 * `pontuacaoGanha` = pontos obtidos NESTA tentativa (soma dos valores atuais das questões acertadas).
 * `pontosTotais`   = total ATUAL do aluno (fonte de verdade, recalculado pelo TriServico),
 *                    já considerando eventuais recalibrações de dificuldade.
 */
public class SubmissaoQuizResposta {

    private double pontuacaoGanha;
    private double pontosTotais;
    private int tempoGasto;
    private int acertos;
    private int totalQuestoes;
    private int nivelAnterior;
    private int nivelNovo;
    private boolean subiuDeNivel;
    private String mensagemCalibracao;
    private List<DetalheQuestao> detalhes = new ArrayList<>();

    public SubmissaoQuizResposta() {
    }

    public SubmissaoQuizResposta(double pontuacaoGanha, double pontosTotais, int tempoGasto, int acertos,
                                 int totalQuestoes, int nivelAnterior, int nivelNovo, boolean subiuDeNivel) {
        this.pontuacaoGanha = pontuacaoGanha;
        this.pontosTotais = pontosTotais;
        this.tempoGasto = tempoGasto;
        this.acertos = acertos;
        this.totalQuestoes = totalQuestoes;
        this.nivelAnterior = nivelAnterior;
        this.nivelNovo = nivelNovo;
        this.subiuDeNivel = subiuDeNivel;
    }

    /** Detalhamento por questão, entregue SOMENTE depois do envio (o gabarito nunca vai antes). */
    public record DetalheQuestao(
            Integer questaoId,
            String pergunta,
            String suaResposta,
            String respostaCorreta,
            boolean acertou,
            double valorPontos,
            String dificuldadeAtual,
            boolean jaAcertouAntes
    ) {
    }

    public double getPontuacaoGanha() {
        return pontuacaoGanha;
    }

    public void setPontuacaoGanha(double pontuacaoGanha) {
        this.pontuacaoGanha = pontuacaoGanha;
    }

    public double getPontosTotais() {
        return pontosTotais;
    }

    public void setPontosTotais(double pontosTotais) {
        this.pontosTotais = pontosTotais;
    }

    public int getTempoGasto() {
        return tempoGasto;
    }

    public void setTempoGasto(int tempoGasto) {
        this.tempoGasto = tempoGasto;
    }

    public int getAcertos() {
        return acertos;
    }

    public void setAcertos(int acertos) {
        this.acertos = acertos;
    }

    public int getTotalQuestoes() {
        return totalQuestoes;
    }

    public void setTotalQuestoes(int totalQuestoes) {
        this.totalQuestoes = totalQuestoes;
    }

    public int getNivelAnterior() {
        return nivelAnterior;
    }

    public void setNivelAnterior(int nivelAnterior) {
        this.nivelAnterior = nivelAnterior;
    }

    public int getNivelNovo() {
        return nivelNovo;
    }

    public void setNivelNovo(int nivelNovo) {
        this.nivelNovo = nivelNovo;
    }

    public boolean isSubiuDeNivel() {
        return subiuDeNivel;
    }

    public void setSubiuDeNivel(boolean subiuDeNivel) {
        this.subiuDeNivel = subiuDeNivel;
    }

    public String getMensagemCalibracao() {
        return mensagemCalibracao;
    }

    public void setMensagemCalibracao(String mensagemCalibracao) {
        this.mensagemCalibracao = mensagemCalibracao;
    }

    public List<DetalheQuestao> getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(List<DetalheQuestao> detalhes) {
        this.detalhes = detalhes;
    }
}
