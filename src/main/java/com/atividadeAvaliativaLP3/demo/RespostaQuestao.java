package com.atividadeAvaliativaLP3.demo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Registro da resposta de um aluno a uma questão.
 *
 * Esta entidade é a FONTE DE VERDADE da pontuação de questões objetivas:
 * a pontuação do aluno é a soma de `pontosAplicados` das suas respostas
 * (ver {@link com.atividadeAvaliativaLP3.demo.servico.TriServico}).
 *
 * Existe no máximo UMA resposta por par (aluno, questão). Isso garante que:
 *  - "10 respostas válidas" signifiquem 10 alunos distintos respondendo;
 *  - o aluno não acumule pontos infinitos ao reenviar o mesmo quiz;
 *  - ao recalibrar a dificuldade da questão, o valor histórico possa ser
 *    recalculado sem risco de duplicação.
 */
@Entity
@Table(
        name = "RespostaQuestao",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_resposta_aluno_questao",
                columnNames = {"aluno_id", "questao_id"}
        )
)
public class RespostaQuestao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resposta_id")
    private Integer respostaId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @Column(name = "questao_id", nullable = false)
    private Integer questaoId;

    @Column(name = "quizz_id", nullable = false)
    private Integer quizzId;

    /** Tentativa que originou (ou atualizou) esta resposta — apenas auditoria. */
    @Column(name = "tentativa_id")
    private Integer tentativaId;

    /** Resultado consolidado da questão para este aluno (melhor resultado obtido). */
    @Column(name = "acertou", nullable = false)
    private boolean acertou;

    /**
     * Pontos efetivamente computados para esta resposta.
     * Sempre igual ao valor ATUAL da questão quando `acertou == true`, e 0 quando errou.
     * É reescrito quando a questão é recalibrada.
     */
    @Column(name = "pontos_aplicados", nullable = false)
    private double pontosAplicados;

    @Column(name = "tempo_segundos")
    private int tempoSegundos;

    @Column(name = "respondido_em", nullable = false)
    private LocalDateTime respondidoEm;

    public RespostaQuestao() {
        this.respondidoEm = LocalDateTime.now();
    }

    public RespostaQuestao(Aluno aluno, Integer questaoId, Integer quizzId, boolean acertou, double pontosAplicados) {
        this.aluno = aluno;
        this.questaoId = questaoId;
        this.quizzId = quizzId;
        this.acertou = acertou;
        this.pontosAplicados = pontosAplicados;
        this.respondidoEm = LocalDateTime.now();
    }

    public Integer getRespostaId() {
        return respostaId;
    }

    public void setRespostaId(Integer respostaId) {
        this.respostaId = respostaId;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Integer getQuestaoId() {
        return questaoId;
    }

    public void setQuestaoId(Integer questaoId) {
        this.questaoId = questaoId;
    }

    public Integer getQuizzId() {
        return quizzId;
    }

    public void setQuizzId(Integer quizzId) {
        this.quizzId = quizzId;
    }

    public Integer getTentativaId() {
        return tentativaId;
    }

    public void setTentativaId(Integer tentativaId) {
        this.tentativaId = tentativaId;
    }

    public boolean isAcertou() {
        return acertou;
    }

    public void setAcertou(boolean acertou) {
        this.acertou = acertou;
    }

    public double getPontosAplicados() {
        return pontosAplicados;
    }

    public void setPontosAplicados(double pontosAplicados) {
        this.pontosAplicados = pontosAplicados;
    }

    public int getTempoSegundos() {
        return tempoSegundos;
    }

    public void setTempoSegundos(int tempoSegundos) {
        this.tempoSegundos = tempoSegundos;
    }

    public LocalDateTime getRespondidoEm() {
        return respondidoEm;
    }

    public void setRespondidoEm(LocalDateTime respondidoEm) {
        this.respondidoEm = respondidoEm;
    }
}
