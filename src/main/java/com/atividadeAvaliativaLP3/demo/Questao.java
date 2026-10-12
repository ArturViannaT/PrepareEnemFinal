package com.atividadeAvaliativaLP3.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Questao")
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "questao_id")
    private Integer questaoId;

    @Column(name = "pergunta", length = 1000, nullable = false)
    private String pergunta;

    @Column(name = "alternativa_correta", length = 5, nullable = false)
    private String alternativaCorreta; // "A", "B", "C", "D" ou "E"

    @Column(name = "quizz_id", nullable = false)
    private Integer quizzId;

    @Column(name = "alternativa_a", length = 500, nullable = false)
    private String alternativaA;

    @Column(name = "alternativa_b", length = 500, nullable = false)
    private String alternativaB;

    @Column(name = "alternativa_c", length = 500, nullable = false)
    private String alternativaC;

    @Column(name = "alternativa_d", length = 500, nullable = false)
    private String alternativaD;

    @Column(name = "alternativa_e", length = 500, nullable = false)
    private String alternativaE;

    // Campos do Modelo Adaptativo Inspirado na TRI
    @Column(name = "dificuldade_inicial", length = 20, nullable = false)
    private String dificuldadeInicial; // FACIL, INTERMEDIARIA, DIFICIL

    @Column(name = "dificuldade_atual", length = 20, nullable = false)
    private String dificuldadeAtual; // FACIL, INTERMEDIARIA, DIFICIL (após recalibração)

    @Column(name = "total_respostas")
    private int totalRespostas;

    @Column(name = "total_acertos")
    private int totalAcertos;

    @Column(name = "percentual_acertos")
    private double percentualAcertos;

    @Column(name = "tempo_medio")
    private double tempoMedio; // Tempo médio de resposta em segundos

    /**
     * Marca que a questão já passou pela recalibração única (após 10 respostas válidas).
     * É um {@link Boolean} (e não um `boolean` primitivo) porque a coluna foi adicionada
     * depois, em bancos já existentes, e pode conter NULL — que é tratado como "não calibrada".
     */
    @Column(name = "calibrada")
    private Boolean calibrada;

    @Column(name = "calibrada_em")
    private LocalDateTime calibradaEm;

    /**
     * Normaliza a dificuldade recebida do frontend/banco.
     * Valores legados ("MEDIO") são convertidos para o padrão atual ("INTERMEDIARIA").
     */
    public static String normalizarDificuldade(String dificuldade) {
        if (dificuldade == null || dificuldade.isBlank()) {
            return "INTERMEDIARIA";
        }
        String valor = dificuldade.trim().toUpperCase();
        switch (valor) {
            case "FACIL":
                return "FACIL";
            case "DIFICIL":
                return "DIFICIL";
            case "MEDIO":
            case "MEDIA":
            case "INTERMEDIARIA":
            case "INTERMEDIARIO":
                return "INTERMEDIARIA";
            default:
                return "INTERMEDIARIA";
        }
    }

    public Questao() {
        this.dificuldadeInicial = "INTERMEDIARIA";
        this.dificuldadeAtual = "INTERMEDIARIA";
        this.totalRespostas = 0;
        this.totalAcertos = 0;
        this.percentualAcertos = 0.0;
        this.tempoMedio = 0.0;
        this.calibrada = false;
    }

    public Questao(String pergunta, String alternativaCorreta, String dificuldadeInicial, Integer quizzId,
                   String alternativaA, String alternativaB, String alternativaC, String alternativaD, String alternativaE) {
        this.pergunta = pergunta;
        this.alternativaCorreta = alternativaCorreta;
        this.dificuldadeInicial = normalizarDificuldade(dificuldadeInicial);
        this.dificuldadeAtual = this.dificuldadeInicial;
        this.quizzId = quizzId;
        this.alternativaA = alternativaA;
        this.alternativaB = alternativaB;
        this.alternativaC = alternativaC;
        this.alternativaD = alternativaD;
        this.alternativaE = alternativaE;
        this.totalRespostas = 0;
        this.totalAcertos = 0;
        this.percentualAcertos = 0.0;
        this.tempoMedio = 0.0;
    }

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

    public String getAlternativaCorreta() {
        return alternativaCorreta;
    }

    public void setAlternativaCorreta(String alternativaCorreta) {
        this.alternativaCorreta = alternativaCorreta;
    }

    public Integer getQuizzId() {
        return quizzId;
    }

    public void setQuizzId(Integer quizzId) {
        this.quizzId = quizzId;
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

    public String getDificuldadeInicial() {
        return dificuldadeInicial;
    }

    public void setDificuldadeInicial(String dificuldadeInicial) {
        this.dificuldadeInicial = normalizarDificuldade(dificuldadeInicial);
        if (this.dificuldadeAtual == null || this.dificuldadeAtual.isBlank()) {
            this.dificuldadeAtual = this.dificuldadeInicial;
        }
    }

    public String getDificuldadeAtual() {
        return dificuldadeAtual;
    }

    public void setDificuldadeAtual(String dificuldadeAtual) {
        this.dificuldadeAtual = normalizarDificuldade(dificuldadeAtual);
    }

    public int getTotalRespostas() {
        return totalRespostas;
    }

    public void setTotalRespostas(int totalRespostas) {
        this.totalRespostas = totalRespostas;
    }

    public int getTotalAcertos() {
        return totalAcertos;
    }

    public void setTotalAcertos(int totalAcertos) {
        this.totalAcertos = totalAcertos;
    }

    public double getPercentualAcertos() {
        return percentualAcertos;
    }

    public void setPercentualAcertos(double percentualAcertos) {
        this.percentualAcertos = percentualAcertos;
    }

    public double getTempoMedio() {
        return tempoMedio;
    }

    public void setTempoMedio(double tempoMedio) {
        this.tempoMedio = tempoMedio;
    }

    /** true somente quando a questão já foi recalibrada (NULL no banco = false). */
    public boolean isCalibrada() {
        return Boolean.TRUE.equals(calibrada);
    }

    public void setCalibrada(Boolean calibrada) {
        this.calibrada = calibrada;
    }

    public LocalDateTime getCalibradaEm() {
        return calibradaEm;
    }

    public void setCalibradaEm(LocalDateTime calibradaEm) {
        this.calibradaEm = calibradaEm;
    }
}
