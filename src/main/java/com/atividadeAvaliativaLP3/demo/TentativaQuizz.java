package com.atividadeAvaliativaLP3.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TentativaQuizz")
public class TentativaQuizz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tentativaId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Column(name = "quizz_id", nullable = false)
    private Integer quizzId;

    @Column(name = "pontuacao_ganha", nullable = false)
    private double pontuacaoGanha; // Pontuação estática congelada (não altera retroativamente)

    @Column(name = "tempo_gasto", nullable = false)
    private int tempoGasto; // Tempo gasto em segundos

    @Column(name = "acertos", nullable = false)
    private int acertos;

    @Column(name = "total_questoes", nullable = false)
    private int totalQuestoes;

    @Column(name = "realizado_em", nullable = false)
    private LocalDateTime realizadoEm;

    public TentativaQuizz() {
        this.realizadoEm = LocalDateTime.now();
    }

    public TentativaQuizz(Aluno aluno, Integer quizzId, double pontuacaoGanha, int tempoGasto, int acertos, int totalQuestoes) {
        this.aluno = aluno;
        this.quizzId = quizzId;
        this.pontuacaoGanha = pontuacaoGanha;
        this.tempoGasto = tempoGasto;
        this.acertos = acertos;
        this.totalQuestoes = totalQuestoes;
        this.realizadoEm = LocalDateTime.now();
    }

    public Integer getTentativaId() {
        return tentativaId;
    }

    public void setTentativaId(Integer tentativaId) {
        this.tentativaId = tentativaId;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Integer getQuizzId() {
        return quizzId;
    }

    public void setQuizzId(Integer quizzId) {
        this.quizzId = quizzId;
    }

    public double getPontuacaoGanha() {
        return pontuacaoGanha;
    }

    public void setPontuacaoGanha(double pontuacaoGanha) {
        this.pontuacaoGanha = pontuacaoGanha;
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

    public LocalDateTime getRealizadoEm() {
        return realizadoEm;
    }

    public void setRealizadoEm(LocalDateTime realizadoEm) {
        this.realizadoEm = realizadoEm;
    }
}
