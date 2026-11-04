package com.atividadeAvaliativaLP3.demo.servico;

import com.atividadeAvaliativaLP3.demo.*;
import com.atividadeAvaliativaLP3.demo.persistencia.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Serviço responsável pela DIFICULDADE ADAPTATIVA, PONTUAÇÃO, XP E NÍVEIS.
 *
 * IMPORTANTE: implementa um modelo PRÓPRIO de dificuldade adaptativa INSPIRADO no
 * conceito de dificuldade da TRI (Teoria de Resposta ao Item) do ENEM.
 * NÃO é a TRI oficial do ENEM: não calcula theta/proficiência nem parâmetros de
 * discriminação ou acerto casual. O que ele faz:
 *
 *   1. O professor define a dificuldade inicial da questão (FACIL/INTERMEDIARIA/DIFICIL).
 *   2. Cada dificuldade tem um valor em pontos:
 *        FACIL = 10 | INTERMEDIARIA = 50 | DIFICIL = 100
 *   3. Após EXATAMENTE 10 respostas válidas (10 alunos distintos), a questão é
 *      recalibrada UMA única vez pelo percentual de acertos das 10 primeiras:
 *        0% a 39%   -> DIFICIL      (100 pontos)
 *        40% a 69%  -> INTERMEDIARIA (50 pontos)
 *        70% a 100% -> FACIL         (10 pontos)
 *   4. Quando a questão muda de valor, TODAS as respostas anteriores são reescritas
 *      (`pontos_aplicados`). A pontuação do aluno é SEMPRE a soma dos valores ATUAIS
 *      das suas respostas — nunca acumulativa — impedindo duplicação de pontos.
 *   5. O nível do aluno deriva DIRETAMENTE do total de pontos atual:
 *        1=0 | 2=100 | 3=225 | 4=375 | 5=550 | 6=750 | 7=975 | 8=1225 | 9=1500 | 10=1800
 */
@Service
public class TriServico {

    public static final String FACIL = "FACIL";
    public static final String INTERMEDIARIA = "INTERMEDIARIA";
    public static final String DIFICIL = "DIFICIL";

    public static final double PONTOS_FACIL = 10.0;
    public static final double PONTOS_INTERMEDIARIA = 50.0;
    public static final double PONTOS_DIFICIL = 100.0;

    /** Respostas válidas necessárias para a recalibração única da questão. */
    public static final int RESPOSTAS_PARA_CALIBRACAO = 10;

    /** Limite de segurança para o laço de cálculo de nível. */
    public static final int NIVEL_MAXIMO = 99;

    private final RespostaQuestaoRepository respostaQuestaoRepository;
    private final QuestaoRepository questaoRepository;
    private final AlunoRepository alunoRepository;

    public TriServico(RespostaQuestaoRepository respostaQuestaoRepository,
                      QuestaoRepository questaoRepository,
                      AlunoRepository alunoRepository) {
        this.respostaQuestaoRepository = respostaQuestaoRepository;
        this.questaoRepository = questaoRepository;
        this.alunoRepository = alunoRepository;
    }

    /** Resultado do processamento de uma única resposta de questão. */
    public record ResultadoResposta(boolean acertou, double pontos, boolean calibrou, String dificuldadeAtual) {
    }

    /** Resumo da reconciliação geral. */
    public record ResumoRecalculo(int questoesProcessadas, int questoesRecalibradas, int alunosRecalculados) {
    }

    // ===================== REGRAS PURAS (fáceis de testar) =====================

    /** Valor em pontos de uma dificuldade. */
    public double pontosPorDificuldade(String dificuldade) {
        String d = Questao.normalizarDificuldade(dificuldade);
        switch (d) {
            case FACIL:
                return PONTOS_FACIL;
            case DIFICIL:
                return PONTOS_DIFICIL;
            case INTERMEDIARIA:
            default:
                return PONTOS_INTERMEDIARIA;
        }
    }

    /** Valor ATUAL da questão (considera a dificuldade já recalibrada, se houver). */
    public double valorAtualQuestao(Questao questao) {
        return pontosPorDificuldade(questao.getDificuldadeAtual());
    }

    /** Reclassificação conforme o percentual de acertos observado nas 10 primeiras respostas. */
    public String dificuldadePorPercentual(double percentualAcertos) {
        if (percentualAcertos <= 39.0) {
            return DIFICIL;
        }
        if (percentualAcertos <= 69.0) {
            return INTERMEDIARIA;
        }
        return FACIL;
    }

    /**
     * Pontos necessários para alcançar um nível.
     * Nível 1 = 0; 2 = 100; 3 = 225; 4 = 375; 5 = 550; 6 = 750 ...
     * Fórmula fechada: 100*(n-1) + 12.5*(n-1)*(n-2) (diferenças crescentes de 25 em 25).
     */
    public double pontosParaNivel(int nivel) {
        if (nivel <= 1) {
            return 0.0;
        }
        double n = nivel;
        return 100.0 * (n - 1) + 12.5 * (n - 1) * (n - 2);
    }

    /** Nível atual do aluno a partir do total de pontos. */
    public int calcularNivel(double pontos) {
        int nivel = 1;
        while (nivel < NIVEL_MAXIMO && pontos >= pontosParaNivel(nivel + 1)) {
            nivel++;
        }
        return nivel;
    }

    /** Percentual de acertos arredondado a uma casa decimal. */
    public double percentualAcertos(int acertos, int respostas) {
        if (respostas <= 0) {
            return 0.0;
        }
        double pct = ((double) acertos / respostas) * 100.0;
        return Math.round(pct * 10.0) / 10.0;
    }

    /** Lista de níveis para exibição da progressão no perfil. */
    public List<Map<String, Object>> tabelaDeNiveis(int ateNivel) {
        List<Map<String, Object>> tabela = new ArrayList<>();
        for (int n = 1; n <= Math.max(1, ateNivel); n++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("nivel", n);
            item.put("pontosNecessarios", pontosParaNivel(n));
            tabela.add(item);
        }
        return tabela;
    }

    // ==== PARTE 2 ====

    // ===================== FLUXO DE RESPOSTA / RECALIBRAÇÃO =====================

    /**
     * Registra a resposta de um aluno a uma questão e atualiza as estatísticas da questão.
     * Mantém no máximo UMA resposta por (aluno, questão) preservando o melhor resultado,
     * o que impede ganho duplicado de pontos em reenvios do mesmo quiz.
     */
    @Transactional
    public ResultadoResposta registrarResposta(Aluno aluno, Questao questao, boolean acertou,
                                               int tempoSegundos, Integer tentativaId) {
        double valorAtual = valorAtualQuestao(questao);

        Optional<RespostaQuestao> existente = respostaQuestaoRepository
                .findByAlunoIdAndQuestaoId(aluno.getId(), questao.getQuestaoId());

        RespostaQuestao resposta;
        boolean acertouFinal;

        if (existente.isPresent()) {
            resposta = existente.get();
            // Melhor resultado é preservado: refazer não piora a pontuação já conquistada.
            acertouFinal = resposta.isAcertou() || acertou;
            resposta.setAcertou(acertouFinal);
            resposta.setTentativaId(tentativaId);
            resposta.setTempoSegundos(tempoSegundos);
            resposta.setRespondidoEm(LocalDateTime.now());
        } else {
            acertouFinal = acertou;
            resposta = new RespostaQuestao(aluno, questao.getQuestaoId(), questao.getQuizzId(),
                    acertouFinal, 0.0);
            resposta.setTentativaId(tentativaId);
            resposta.setTempoSegundos(tempoSegundos);
        }

        resposta.setPontosAplicados(acertouFinal ? valorAtual : 0.0);
        respostaQuestaoRepository.save(resposta);

        recalcularEstatisticasQuestao(questao);
        boolean calibrou = calibrarQuestaoSeNecessario(questao);

        // Pontuação do aluno recalculada com base nos valores ATUAIS das respostas.
        recalcularAluno(aluno);

        return new ResultadoResposta(acertouFinal, resposta.getPontosAplicados(), calibrou,
                questao.getDificuldadeAtual());
    }

    /** Recalcula totalRespostas/totalAcertos/percentual/tempoMedio a partir das respostas gravadas. */
    @Transactional
    public void recalcularEstatisticasQuestao(Questao questao) {
        List<RespostaQuestao> respostas = respostaQuestaoRepository.findByQuestaoId(questao.getQuestaoId());
        int total = respostas.size();
        int acertos = 0;
        int tempoTotal = 0;
        for (RespostaQuestao r : respostas) {
            if (r.isAcertou()) {
                acertos++;
            }
            tempoTotal += r.getTempoSegundos();
        }

        questao.setTotalRespostas(total);
        questao.setTotalAcertos(acertos);
        questao.setPercentualAcertos(percentualAcertos(acertos, total));
        questao.setTempoMedio(total > 0 ? Math.round(((double) tempoTotal / total) * 10.0) / 10.0 : 0.0);
        questaoRepository.save(questao);
    }

    /**
     * Executa a recalibração ÚNICA da questão quando ela atinge as primeiras 10 respostas válidas,
     * usando exatamente as 10 primeiras respostas registradas (ordem de inserção).
     *
     * @return true se a questão foi recalibrada nesta chamada.
     */
    @Transactional
    public boolean calibrarQuestaoSeNecessario(Questao questao) {
        if (questao.isCalibrada()) {
            return false; // calibração única: depois da primeira, a classificação é mantida
        }

        List<RespostaQuestao> respostas = respostaQuestaoRepository
                .findByQuestaoIdOrderByRespostaIdAsc(questao.getQuestaoId());

        if (respostas.size() < RESPOSTAS_PARA_CALIBRACAO) {
            return false; // ainda não há 10 respostas válidas: não recalibrar
        }

        int acertosNasDezPrimeiras = 0;
        for (int i = 0; i < RESPOSTAS_PARA_CALIBRACAO; i++) {
            if (respostas.get(i).isAcertou()) {
                acertosNasDezPrimeiras++;
            }
        }

        double pct = percentualAcertos(acertosNasDezPrimeiras, RESPOSTAS_PARA_CALIBRACAO);
        String novaDificuldade = dificuldadePorPercentual(pct);

        questao.setDificuldadeAtual(novaDificuldade);
        questao.setCalibrada(true);
        questao.setCalibradaEm(LocalDateTime.now());
        questaoRepository.save(questao);

        // A questão mudou de valor: reescreve TODAS as respostas anteriores com o novo valor.
        recalcularValoresDasRespostas(questao);

        return true;
    }

    // ===================== RECÁLCULO DE PONTUAÇÃO E NÍVEL =====================

    /**
     * Aplica o valor ATUAL da questão a todas as respostas já registradas e recalcula
     * a pontuação/nível dos alunos afetados. Os valores são SUBSTITUÍDOS (nunca somados
     * ao valor antigo), garantindo consistência após uma recalibração.
     */
    @Transactional
    public void recalcularValoresDasRespostas(Questao questao) {
        double valor = valorAtualQuestao(questao);
        List<RespostaQuestao> respostas = respostaQuestaoRepository.findByQuestaoId(questao.getQuestaoId());

        Map<Integer, Aluno> alunosAfetados = new LinkedHashMap<>();
        for (RespostaQuestao r : respostas) {
            r.setPontosAplicados(r.isAcertou() ? valor : 0.0);
            respostaQuestaoRepository.save(r);
            if (r.getAluno() != null) {
                alunosAfetados.put(r.getAluno().getId(), r.getAluno());
            }
        }

        for (Aluno aluno : alunosAfetados.values()) {
            recalcularAluno(aluno);
        }
    }

    /** Recalcula pontos, XP e nível do aluno a partir das respostas gravadas (fonte de verdade). */
    @Transactional
    public void recalcularAluno(Aluno aluno) {
        if (aluno == null || aluno.getId() == null) {
            return;
        }
        double total = respostaQuestaoRepository.findByAlunoId(aluno.getId())
                .stream()
                .mapToDouble(RespostaQuestao::getPontosAplicados)
                .sum();

        double totalArredondado = Math.round(total * 100.0) / 100.0;
        aluno.setPontos(totalArredondado);
        aluno.setNivel(calcularNivel(totalArredondado));
        aluno.setXp((int) Math.round(totalArredondado));
        alunoRepository.save(aluno);
    }

    /**
     * Reconciliação geral ("fonte de verdade"): recalcula estatísticas e valores de todas as
     * questões, executa calibrações pendentes (questões que já têm 10 respostas mas ainda não
     * foram recalibradas) e recalcula pontos/nível de todos os alunos.
     * Não destrói dados — apenas reescreve valores derivados.
     */
    @Transactional
    public ResumoRecalculo recalcularTudo() {
        List<Questao> questoes = questaoRepository.findAll();
        int questoesCalibradas = 0;

        for (Questao q : questoes) {
            recalcularEstatisticasQuestao(q);
            if (calibrarQuestaoSeNecessario(q)) {
                questoesCalibradas++;
            }
            recalcularValoresDasRespostas(q);
        }

        List<Aluno> alunos = alunoRepository.findAll();
        for (Aluno a : alunos) {
            recalcularAluno(a);
        }

        return new ResumoRecalculo(questoes.size(), questoesCalibradas, alunos.size());
    }
}
