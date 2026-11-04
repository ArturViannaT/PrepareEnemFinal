package com.atividadeAvaliativaLP3.demo.servico;

import com.atividadeAvaliativaLP3.demo.Aluno;
import com.atividadeAvaliativaLP3.demo.Questao;
import com.atividadeAvaliativaLP3.demo.RespostaQuestao;
import com.atividadeAvaliativaLP3.demo.persistencia.AlunoRepository;
import com.atividadeAvaliativaLP3.demo.persistencia.QuestaoRepository;
import com.atividadeAvaliativaLP3.demo.persistencia.RespostaQuestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TriServicoTest {

    private RespostaQuestaoRepository respostaRepository;
    private QuestaoRepository questaoRepository;
    private AlunoRepository alunoRepository;
    private TriServico triServico;

    @BeforeEach
    void configurar() {
        respostaRepository = mock(RespostaQuestaoRepository.class);
        questaoRepository = mock(QuestaoRepository.class);
        alunoRepository = mock(AlunoRepository.class);
        triServico = new TriServico(respostaRepository, questaoRepository, alunoRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "39, DIFICIL",
            "40, INTERMEDIARIA",
            "69, INTERMEDIARIA",
            "70, FACIL"
    })
    void deveClassificarPercentuaisNosLimites(double percentual, String dificuldadeEsperada) {
        assertEquals(dificuldadeEsperada, triServico.dificuldadePorPercentual(percentual));
    }

    @Test
    void naoDeveCalibrarAntesDeDezRespostasDistintas() {
        Questao questao = new Questao();
        questao.setQuestaoId(42);
        List<RespostaQuestao> respostas = criarRespostas(9, 3);
        when(respostaRepository.findByQuestaoIdOrderByRespostaIdAsc(42)).thenReturn(respostas);

        boolean calibrada = triServico.calibrarQuestaoSeNecessario(questao);

        assertFalse(calibrada);
        assertFalse(questao.isCalibrada());
        verify(questaoRepository, never()).save(questao);
    }

    @Test
    void deveCalibrarComAsDezPrimeirasRespostasERecalcularOsPontos() {
        Questao questao = new Questao();
        questao.setQuestaoId(42);
        List<RespostaQuestao> respostas = criarRespostas(10, 3);
        when(respostaRepository.findByQuestaoIdOrderByRespostaIdAsc(42)).thenReturn(respostas);
        when(respostaRepository.findByQuestaoId(42)).thenReturn(respostas);

        boolean calibrada = triServico.calibrarQuestaoSeNecessario(questao);

        assertTrue(calibrada);
        assertTrue(questao.isCalibrada());
        assertEquals(TriServico.DIFICIL, questao.getDificuldadeAtual());
        assertEquals(TriServico.PONTOS_DIFICIL, triServico.valorAtualQuestao(questao));
        verify(respostaRepository).save(respostas.get(0));
        verify(respostas.get(0)).setPontosAplicados(TriServico.PONTOS_DIFICIL);
        verify(respostas.get(3)).setPontosAplicados(0.0);
        verify(questaoRepository).save(questao);
    }

    @Test
    void naoDeveCalibrarUmaQuestaoMaisDeUmaVez() {
        Questao questao = new Questao();
        questao.setQuestaoId(42);
        questao.setCalibrada(true);
        questao.setDificuldadeAtual(TriServico.FACIL);

        boolean recalibrada = triServico.calibrarQuestaoSeNecessario(questao);

        assertFalse(recalibrada);
        assertEquals(TriServico.FACIL, questao.getDificuldadeAtual());
        verify(respostaRepository, never()).findByQuestaoIdOrderByRespostaIdAsc(42);
    }

    private List<RespostaQuestao> criarRespostas(int quantidade, int acertos) {
        List<RespostaQuestao> respostas = new ArrayList<>();
        for (int indice = 0; indice < quantidade; indice++) {
            RespostaQuestao resposta = mock(RespostaQuestao.class);
            when(resposta.isAcertou()).thenReturn(indice < acertos);
            when(resposta.getAluno()).thenReturn((Aluno) null);
            respostas.add(resposta);
        }
        return respostas;
    }
}
