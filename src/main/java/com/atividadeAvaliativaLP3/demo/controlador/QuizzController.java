package com.atividadeAvaliativaLP3.demo.controlador;

import com.atividadeAvaliativaLP3.demo.*;
import com.atividadeAvaliativaLP3.demo.dto.*;
import com.atividadeAvaliativaLP3.demo.excecao.RecursoNaoEncontradoException;
import com.atividadeAvaliativaLP3.demo.excecao.RegraNegocioException;
import com.atividadeAvaliativaLP3.demo.servico.TriServico;
import com.atividadeAvaliativaLP3.demo.servico.UsuarioServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes")
public class QuizzController {

    private final QuizzRepository quizzRepository;
    private final QuestaoRepository questaoRepository;
    private final AlunoRepository alunoRepository;
    private final TentativaQuizzRepository tentativaQuizzRepository;
    private final RespostaQuestaoRepository respostaQuestaoRepository;
    private final TriServico triServico;
    private final UsuarioServico usuarioServico;

    /** Tolerância (segundos) entre o tempo informado pelo navegador e o limite do quiz. */
    private static final int TOLERANCIA_TEMPO_SEGUNDOS = 10;

    public QuizzController(QuizzRepository quizzRepository,
                           QuestaoRepository questaoRepository,
                           AlunoRepository alunoRepository,
                           TentativaQuizzRepository tentativaQuizzRepository,
                           RespostaQuestaoRepository respostaQuestaoRepository,
                           TriServico triServico,
                           UsuarioServico usuarioServico) {
        this.quizzRepository = quizzRepository;
        this.questaoRepository = questaoRepository;
        this.alunoRepository = alunoRepository;
        this.tentativaQuizzRepository = tentativaQuizzRepository;
        this.respostaQuestaoRepository = respostaQuestaoRepository;
        this.triServico = triServico;
        this.usuarioServico = usuarioServico;
    }

    // ===================== VISUALIZAÇÕES =====================

    /** Questão SEM o gabarito: usada na realização do quiz pelo aluno. */
    private Map<String, Object> visaoQuestaoSemGabarito(Questao q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("questaoId", q.getQuestaoId());
        m.put("pergunta", q.getPergunta());
        m.put("alternativaA", q.getAlternativaA());
        m.put("alternativaB", q.getAlternativaB());
        m.put("alternativaC", q.getAlternativaC());
        m.put("alternativaD", q.getAlternativaD());
        m.put("alternativaE", q.getAlternativaE());
        m.put("dificuldadeInicial", q.getDificuldadeInicial());
        m.put("dificuldadeAtual", q.getDificuldadeAtual());
        m.put("valorPontos", triServico.valorAtualQuestao(q));
        m.put("calibrada", q.isCalibrada());
        m.put("totalRespostas", q.getTotalRespostas());
        return m;
    }

    /** Questão COM o gabarito: usada somente na visão do autor (professor dono do quiz). */
    private Map<String, Object> visaoQuestaoDoAutor(Questao q) {
        Map<String, Object> m = visaoQuestaoSemGabarito(q);
        m.put("alternativaCorreta", q.getAlternativaCorreta());
        m.put("totalAcertos", q.getTotalAcertos());
        m.put("percentualAcertos", q.getPercentualAcertos());
        m.put("tempoMedio", q.getTempoMedio());
        return m;
    }

    private Map<String, Object> visaoQuizz(Quizz q, boolean incluirGabarito) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("quizzId", q.getQuizzId());
        m.put("nomeQuizz", q.getNomeQuizz());
        m.put("materia", q.getMateria());
        m.put("dificuldadeGeral", q.getDificuldadeGeral());
        m.put("limiteTempo", q.getLimiteTempo());
        m.put("urlVideo", q.getUrlVideo());
        m.put("autorId", q.getAutor() != null ? q.getAutor().getId() : null);
        m.put("autorNome", q.getAutor() != null ? q.getAutor().getNome() : null);

        List<Questao> questoes = questaoRepository.findByQuizzId(q.getQuizzId());
        m.put("quantidadeQuestoes", questoes.size());
        m.put("questoes", questoes.stream()
                .map(incluirGabarito ? this::visaoQuestaoDoAutor : this::visaoQuestaoSemGabarito)
                .collect(Collectors.toList()));
        return m;
    }

    /**
     * Cria uma nova questão a partir da requisição do professor, usando a dificuldade
     * inicial escolhida por ele (FACIL=10 / INTERMEDIARIA=50 / DIFICIL=100 pontos).
     */
    private Questao montarQuestao(QuestaoRequisicao qReq, Integer quizzId) {
        Questao questao = new Questao();
        questao.setQuizzId(quizzId);
        questao.setPergunta(qReq.getPergunta());
        questao.setAlternativaA(qReq.getAlternativaA());
        questao.setAlternativaB(qReq.getAlternativaB());
        questao.setAlternativaC(qReq.getAlternativaC());
        questao.setAlternativaD(qReq.getAlternativaD());
        questao.setAlternativaE(qReq.getAlternativaE());
        questao.setAlternativaCorreta(qReq.getAlternativaCorreta().trim().toUpperCase());
        String dificuldade = Questao.normalizarDificuldade(qReq.getDificuldade());
        questao.setDificuldadeInicial(dificuldade);
        questao.setDificuldadeAtual(dificuldade);
        return questao;
    }

    /**
     * Atualiza os dados de uma questão já existente PRESERVANDO as estatísticas de
     * dificuldade adaptativa e as respostas dos alunos (por isso o gabarito não é alterado
     * quando a questão já entrou em calibração, para não invalidar dados já coletados).
     */
    private Questao aplicarRequisicaoEmQuestaoExistente(Questao questao, QuestaoRequisicao qReq) {
        questao.setPergunta(qReq.getPergunta());
        questao.setAlternativaA(qReq.getAlternativaA());
        questao.setAlternativaB(qReq.getAlternativaB());
        questao.setAlternativaC(qReq.getAlternativaC());
        questao.setAlternativaD(qReq.getAlternativaD());
        questao.setAlternativaE(qReq.getAlternativaE());

        String novoGabarito = qReq.getAlternativaCorreta().trim().toUpperCase();
        if (!novoGabarito.equals(questao.getAlternativaCorreta()) && questao.getTotalRespostas() > 0) {
            throw new RegraNegocioException(
                    "A questão " + questao.getQuestaoId() + " já possui respostas de alunos e a alternativa "
                            + "correta não pode mais ser alterada (isso invalidaria as estatísticas de dificuldade).");
        }
        questao.setAlternativaCorreta(novoGabarito);

        String dificuldade = Questao.normalizarDificuldade(qReq.getDificuldade());
        questao.setDificuldadeInicial(dificuldade);
        if (!questao.isCalibrada()) {
            questao.setDificuldadeAtual(dificuldade);
        }
        return questao;
    }

    private void exigirProfessor(Usuario logado, String acao) {
        if (!(logado instanceof Professor)) {
            throw new RegraNegocioException("Somente professores cadastrados podem " + acao + ".");
        }
    }

    private void exigirAutoria(Quizz quizz, Usuario logado, String acao) {
        if (quizz.getAutor() == null || !quizz.getAutor().getId().equals(logado.getId())) {
            throw new RegraNegocioException("Acesso negado: você só pode " + acao
                    + " atividades de sua própria autoria.");
        }
    }

    // RF2. Publicar atividades (Quizzes) na plataforma
    @PostMapping
    public ResponseEntity<Map<String, Object>> criarQuizz(@Valid @RequestBody QuizzRequisicao req) {
        Usuario logado = usuarioServico.obterUsuarioLogado();
        exigirProfessor(logado, "criar quizzes");

        Quizz quizz = new Quizz();
        quizz.setNomeQuizz(req.getNomeQuizz());
        quizz.setMateria(req.getMateria());
        quizz.setAutor(logado);
        quizz.setRecompensaPontos(req.getRecompensaPontos() != null ? req.getRecompensaPontos() : 0);
        quizz.setLimiteTempo(req.getLimiteTempo());
        quizz.setDificuldadeGeral(Questao.normalizarDificuldade(req.getDificuldadeGeral()));
        quizz.setUrlVideo(req.getUrlVideo());

        Quizz quizzSalvo = quizzRepository.save(quizz);

        for (QuestaoRequisicao qReq : req.getQuestoes()) {
            questaoRepository.save(montarQuestao(qReq, quizzSalvo.getQuizzId()));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(visaoQuizz(quizzSalvo, true));
    }

    /**
     * RF2.2. Editar atividade de sua própria autoria.
     * Questões enviadas com `questaoId` existente são ATUALIZADAS no lugar (preservando
     * estatísticas e respostas dos alunos); questões ausentes são removidas (junto com as
     * respostas vinculadas, recalculando a pontuação dos alunos afetados).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> editarQuizz(@PathVariable("id") Integer id,
                                                            @Valid @RequestBody QuizzRequisicao req) {
        Usuario logado = usuarioServico.obterUsuarioLogado();
        exigirProfessor(logado, "editar quizzes");

        Quizz quizz = quizzRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quizz não encontrado"));
        exigirAutoria(quizz, logado, "editar");

        quizz.setNomeQuizz(req.getNomeQuizz());
        quizz.setMateria(req.getMateria());
        quizz.setRecompensaPontos(req.getRecompensaPontos() != null ? req.getRecompensaPontos() : 0);
        quizz.setLimiteTempo(req.getLimiteTempo());
        quizz.setDificuldadeGeral(Questao.normalizarDificuldade(req.getDificuldadeGeral()));
        quizz.setUrlVideo(req.getUrlVideo());
        Quizz quizzAtualizado = quizzRepository.save(quizz);

        Map<Integer, Questao> existentes = new LinkedHashMap<>();
        for (Questao q : questaoRepository.findByQuizzId(id)) {
            existentes.put(q.getQuestaoId(), q);
        }

        for (QuestaoRequisicao qReq : req.getQuestoes()) {
            if (qReq.getQuestaoId() != null) {
                Questao existente = existentes.get(qReq.getQuestaoId());
                if (existente == null) {
                    throw new RegraNegocioException(
                            "A questão " + qReq.getQuestaoId() + " não pertence a este quiz.");
                }
                existentes.remove(qReq.getQuestaoId());
                questaoRepository.save(aplicarRequisicaoEmQuestaoExistente(existente, qReq));
            } else {
                questaoRepository.save(montarQuestao(qReq, quizzAtualizado.getQuizzId()));
            }
        }

        // Questões removidas na edição: apaga também as respostas vinculadas e recalcula
        // a pontuação/nível dos alunos afetados (mantém a fonte de verdade consistente).
        for (Questao removida : existentes.values()) {
            removerQuestaoEItsRespostas(removida);
        }

        return ResponseEntity.ok(visaoQuizz(quizzAtualizado, true));
    }

    /** Remove a questão e as respostas vinculadas, recalculando os alunos afetados. */
    private void removerQuestaoEItsRespostas(Questao questao) {
        List<RespostaQuestao> respostas = respostaQuestaoRepository.findByQuestaoId(questao.getQuestaoId());
        List<Aluno> afetados = new ArrayList<>();
        for (RespostaQuestao r : respostas) {
            if (r.getAluno() != null) {
                afetados.add(r.getAluno());
            }
        }
        respostaQuestaoRepository.deleteAll(respostas);
        questaoRepository.delete(questao);
        for (Aluno aluno : afetados) {
            triServico.recalcularAluno(aluno);
        }
    }

    // RF2.3. Excluir atividade de sua própria autoria
    @DeleteMapping("/{id}")
    public ResponseEntity<String> excluirQuizz(@PathVariable("id") Integer id) {
        Usuario logado = usuarioServico.obterUsuarioLogado();
        exigirProfessor(logado, "excluir quizzes");

        Quizz quizz = quizzRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quizz não encontrado"));
        exigirAutoria(quizz, logado, "excluir");

        for (Questao questao : questaoRepository.findByQuizzId(id)) {
            removerQuestaoEItsRespostas(questao);
        }
        quizzRepository.delete(quizz);

        return ResponseEntity.ok("Quizz excluído com sucesso.");
    }

    // RF2.4. Visualizar atividades de sua autoria (visão com gabarito e estatísticas)
    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> listarMeusQuizzes() {
        Usuario logado = usuarioServico.obterUsuarioLogado();
        exigirProfessor(logado, "gerenciar quizzes");

        List<Map<String, Object>> response = new ArrayList<>();
        for (Quizz q : quizzRepository.findByAutorId(logado.getId())) {
            response.add(visaoQuizz(q, true));
        }
        return ResponseEntity.ok(response);
    }

    // RF16. Listar quizzes disponíveis (sem expor o gabarito)
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarQuizzesDisponiveis() {
        List<Map<String, Object>> response = new ArrayList<>();
        for (Quizz q : quizzRepository.findAll()) {
            response.add(visaoQuizz(q, false));
        }
        return ResponseEntity.ok(response);
    }

    /** Tabela de níveis (progressão) usada na interface de perfil/ranking. */
    @GetMapping("/tri/niveis")
    public ResponseEntity<List<Map<String, Object>>> tabelaDeNiveis() {
        return ResponseEntity.ok(triServico.tabelaDeNiveis(12));
    }

    /**
     * Reconciliação da pontuação (manutenção, apenas professor): recalcula estatísticas das
     * questões, executa calibrações pendentes e reconstrói pontos/nível de todos os alunos.
     */
    @PostMapping("/tri/recalcular")
    public ResponseEntity<Map<String, Object>> recalcularPontuacao() {
        Usuario logado = usuarioServico.obterUsuarioLogado();
        exigirProfessor(logado, "recalcular a pontuação do sistema");

        TriServico.ResumoRecalculo resumo = triServico.recalcularTudo();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("mensagem", "Pontuação, dificuldade e níveis recalculados com base nas respostas registradas.");
        response.put("questoesProcessadas", resumo.questoesProcessadas());
        response.put("questoesRecalibradas", resumo.questoesRecalibradas());
        response.put("alunosRecalculados", resumo.alunosRecalculados());
        return ResponseEntity.ok(response);
    }

    /**
     * Obter dados do quiz para realização: devolve apenas o necessário e NUNCA o gabarito.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obterQuizzParaRealizacao(@PathVariable("id") Integer id) {
        Quizz quizz = quizzRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quizz não encontrado"));

        Map<String, Object> response = visaoQuizz(quizz, false);

        // Se quem está abrindo for o aluno, informa o que ele já respondeu neste quiz
        // (para o frontend deixar claro que o melhor resultado é preservado).
        Optional<Aluno> alunoOpt = alunoRepository.findByAuthId(usuarioServico.getAuthIdLogado());
        if (alunoOpt.isPresent()) {
            Aluno aluno = alunoOpt.get();
            List<RespostaQuestao> respostas = respostaQuestaoRepository.findByAlunoId(aluno.getId());
            Map<Integer, Boolean> respostasPorQuestao = new LinkedHashMap<>();
            double pontosNoQuiz = 0.0;
            for (RespostaQuestao r : respostas) {
                if (r.getQuizzId() != null && r.getQuizzId().equals(id)) {
                    respostasPorQuestao.put(r.getQuestaoId(), r.isAcertou());
                    pontosNoQuiz += r.getPontosAplicados();
                }
            }
            response.put("jaRespondido", !respostasPorQuestao.isEmpty());
            response.put("pontosJaObtidosNoQuiz", pontosNoQuiz);
            response.put("questoesJaRespondidas", respostasPorQuestao);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * REALIZAÇÃO do quiz: valida a submissão, corrige no BACKEND e aplica o modelo adaptativo.
     *
     * Regras de integridade:
     *  - somente Aluno responde e pontua;
     *  - todas as questões do quiz devem ser respondidas (RNF6);
     *  - não são aceitas questões de outro quiz nem respostas duplicadas;
     *  - o tempo informado é validado contra o limite definido pelo professor
     *    (o backend não confia cegamente no cronômetro do navegador);
     *  - reenviar o mesmo quiz NÃO duplica pontos: existe no máximo uma resposta por
     *    (aluno, questão) e o melhor resultado é preservado.
     */
    @PostMapping("/{id}/attempt")
    public ResponseEntity<SubmissaoQuizResposta> submeterRespostas(
            @PathVariable("id") Integer id,
            @Valid @RequestBody SubmissaoQuizRequisicao req
    ) {
        Usuario logado = usuarioServico.obterUsuarioLogado();

        Aluno aluno = alunoRepository.findByAuthId(logado.getAuthId())
                .orElseThrow(() -> new RegraNegocioException(
                        "Somente contas de Aluno podem realizar tentativas de quiz e pontuar."));

        Quizz quizz = quizzRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Quizz não encontrado"));

        List<Questao> questoes = questaoRepository.findByQuizzId(id);
        if (questoes.isEmpty()) {
            throw new RegraNegocioException("Este quizz não possui questões cadastradas.");
        }

        // ---- Validação do tempo contra o limite definido pelo professor ----
        int limite = quizz.getLimiteTempo() != null ? quizz.getLimiteTempo() : 0;
        Integer tempoInformado = req.getTempoGasto();
        if (tempoInformado == null || tempoInformado < 0) {
            throw new RegraNegocioException("Tempo de realização inválido.");
        }
        int tempoGasto = tempoInformado;
        if (limite > 0 && tempoGasto > limite + TOLERANCIA_TEMPO_SEGUNDOS) {
            // O cronômetro do navegador pode ter sido manipulado: o backend registra no
            // máximo o limite definido pelo professor, sem premiar tempo extra.
            tempoGasto = limite;
        }

        // ---- Validação das respostas: pertencimento ao quiz, unicidade e completude (RNF6) ----
        Set<Integer> idsDoQuiz = questoes.stream().map(Questao::getQuestaoId).collect(Collectors.toSet());
        Map<Integer, String> respostasMap = new LinkedHashMap<>();
        for (RespostaQuizRequisicao r : req.getRespostas()) {
            if (r.getQuestaoId() == null || !idsDoQuiz.contains(r.getQuestaoId())) {
                throw new RegraNegocioException("A questão " + r.getQuestaoId() + " não pertence a este quiz.");
            }
            if (respostasMap.put(r.getQuestaoId(), r.getOpcaoSelecionada().trim().toUpperCase()) != null) {
                throw new RegraNegocioException("A questão " + r.getQuestaoId() + " foi enviada mais de uma vez.");
            }
        }
        for (Questao q : questoes) {
            String resp = respostasMap.get(q.getQuestaoId());
            if (resp == null || resp.isBlank()) {
                throw new RegraNegocioException("Erro de Submissão: responda todas as questões do quiz. "
                        + "A questão " + q.getQuestaoId() + " está sem resposta.");
            }
        }

        // Nível ANTES do processamento (o TriServico recalcula o nível do aluno).
        int nivelAnterior = aluno.getNivel();

        // A tentativa é gravada primeiro para gerar o id usado como auditoria nas respostas.
        TentativaQuizz tentativa = tentativaQuizzRepository.save(
                new TentativaQuizz(aluno, id, 0.0, tempoGasto, 0, questoes.size()));

        double pontosNaTentativa = 0.0;
        int acertos = 0;
        // O tempo total é distribuído igualmente entre as questões para alimentar o tempo médio
        // (o frontend envia apenas o tempo total da tentativa).
        int tempoPorQuestao = Math.max(1, tempoGasto / questoes.size());
        List<SubmissaoQuizResposta.DetalheQuestao> detalhes = new ArrayList<>();
        List<String> recalibradas = new ArrayList<>();

        for (Questao q : questoes) {
            String respostaAluno = respostasMap.get(q.getQuestaoId());
            boolean acertou = q.getAlternativaCorreta().equalsIgnoreCase(respostaAluno);
            boolean jaAcertouAntes = respostaQuestaoRepository
                    .findByAlunoIdAndQuestaoId(aluno.getId(), q.getQuestaoId())
                    .map(RespostaQuestao::isAcertou)
                    .orElse(false);

            // Registra/atualiza a resposta e atualiza estatísticas + dificuldade adaptativa
            TriServico.ResultadoResposta resultado =
                    triServico.registrarResposta(aluno, q, acertou, tempoPorQuestao, tentativa.getTentativaId());

            if (acertou) {
                acertos++;
                // Só soma pontos em NOVOS acertos: reenviar o quiz não gera recompensa duplicada.
                if (!jaAcertouAntes && resultado.acertou()) {
                    pontosNaTentativa += resultado.pontos();
                }
            }
            if (resultado.calibrou()) {
                recalibradas.add(q.getQuestaoId() + " (" + q.getDificuldadeAtual() + ")");
            }

            detalhes.add(new SubmissaoQuizResposta.DetalheQuestao(
                    q.getQuestaoId(), q.getPergunta(), respostaAluno, q.getAlternativaCorreta(),
                    acertou, resultado.pontos(), q.getDificuldadeAtual(), jaAcertouAntes));
        }

        // Grava os totais da TENTATIVA (log de desempenho). A pontuação TOTAL do aluno é a soma
        // dos valores atuais das suas respostas, calculada pelo TriServico (fonte de verdade).
        tentativa.setAcertos(acertos);
        tentativa.setPontuacaoGanha(Math.round(pontosNaTentativa * 100.0) / 100.0);
        tentativaQuizzRepository.save(tentativa);

        // Recarrega o aluno: pontos/nível foram recalculados dentro do TriServico.
        Aluno alunoAtualizado = alunoRepository.findById(aluno.getId()).orElse(aluno);
        alunoAtualizado.setQuizzesResolvidos(
                (int) tentativaQuizzRepository.countByAlunoId(alunoAtualizado.getId()));
        alunoAtualizado = alunoRepository.save(alunoAtualizado);

        int nivelNovo = alunoAtualizado.getNivel();
        SubmissaoQuizResposta responseDto = new SubmissaoQuizResposta(
                Math.round(pontosNaTentativa * 100.0) / 100.0,
                alunoAtualizado.getPontos(),
                tempoGasto,
                acertos,
                questoes.size(),
                nivelAnterior,
                nivelNovo,
                nivelNovo > nivelAnterior
        );
        responseDto.setDetalhes(detalhes);

        if (!recalibradas.isEmpty()) {
            responseDto.setMensagemCalibracao("Recalibração automática: " + recalibradas.size()
                    + " questão(ões) deste quiz atingiram 10 respostas válidas e tiveram a dificuldade "
                    + "ajustada. Sua pontuação total já foi recalculada com os novos valores.");
        }

        return ResponseEntity.ok(responseDto);
    }

    // RF7. Visualizar o desempenho em quizzes realizados
    @GetMapping("/attempts")
    public ResponseEntity<List<Map<String, Object>>> listarMinhasTentativas() {
        Usuario logado = usuarioServico.obterUsuarioLogado();

        Aluno aluno = alunoRepository.findByAuthId(logado.getAuthId())
                .orElseThrow(() -> new RegraNegocioException("Somente contas de Aluno possuem histórico de tentativas de quiz."));

        List<TentativaQuizz> tentativas = tentativaQuizzRepository
                .findByAlunoIdOrderByRealizadoEmDesc(aluno.getId());

        List<Map<String, Object>> response = new ArrayList<>();
        for (TentativaQuizz t : tentativas) {
            Map<String, Object> map = new HashMap<>();
            map.put("tentativaId", t.getTentativaId());
            map.put("pontuacaoGanha", t.getPontuacaoGanha());
            map.put("tempoGasto", t.getTempoGasto());
            map.put("acertos", t.getAcertos());
            map.put("totalQuestoes", t.getTotalQuestoes());
            map.put("realizadoEm", t.getRealizadoEm());

            Optional<Quizz> quizOpt = quizzRepository.findById(t.getQuizzId());
            if (quizOpt.isPresent()) {
                Quizz q = quizOpt.get();
                map.put("quizzId", q.getQuizzId());
                map.put("nomeQuizz", q.getNomeQuizz());
                map.put("materia", q.getMateria());
            } else {
                map.put("quizzId", t.getQuizzId());
                map.put("nomeQuizz", "Quiz Excluído");
            }
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }
}
