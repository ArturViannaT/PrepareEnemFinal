package com.atividadeAvaliativaLP3.demo.controlador;

import com.atividadeAvaliativaLP3.demo.*;
import com.atividadeAvaliativaLP3.demo.persistencia.*;
import com.atividadeAvaliativaLP3.demo.dto.*;
import com.atividadeAvaliativaLP3.demo.excecao.RecursoNaoEncontradoException;
import com.atividadeAvaliativaLP3.demo.excecao.RegraNegocioException;
import com.atividadeAvaliativaLP3.demo.servico.FotoPerfilServico;
import com.atividadeAvaliativaLP3.demo.servico.TriServico;
import com.atividadeAvaliativaLP3.demo.servico.UsuarioServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", allowedHeaders = "*")

public class PerfilController {

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final ConteudoRepository conteudoRepository;
    private final QuizzRepository quizzRepository;
    private final QuestaoRepository questaoRepository;
    private final TentativaQuizzRepository tentativaQuizzRepository;
    private final RespostaQuestaoRepository respostaQuestaoRepository;
    private final HistoricoAcessoRepository historicoAcessoRepository;
    private final AvaliacaoConteudoRepository avaliacaoConteudoRepository;
    private final TriServico triServico;
    private final UsuarioServico usuarioServico;
    private final FotoPerfilServico fotoPerfilServico;

    public PerfilController(UsuarioRepository usuarioRepository, AlunoRepository alunoRepository,
                            ProfessorRepository professorRepository, ConteudoRepository conteudoRepository,
                            QuizzRepository quizzRepository, QuestaoRepository questaoRepository,
                            TentativaQuizzRepository tentativaQuizzRepository,
                            RespostaQuestaoRepository respostaQuestaoRepository,
                            HistoricoAcessoRepository historicoAcessoRepository,
                            AvaliacaoConteudoRepository avaliacaoConteudoRepository,
                            TriServico triServico, UsuarioServico usuarioServico,
                            FotoPerfilServico fotoPerfilServico) {
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.conteudoRepository = conteudoRepository;
        this.quizzRepository = quizzRepository;
        this.questaoRepository = questaoRepository;
        this.tentativaQuizzRepository = tentativaQuizzRepository;
        this.respostaQuestaoRepository = respostaQuestaoRepository;
        this.historicoAcessoRepository = historicoAcessoRepository;
        this.avaliacaoConteudoRepository = avaliacaoConteudoRepository;
        this.triServico = triServico;
        this.usuarioServico = usuarioServico;
        this.fotoPerfilServico = fotoPerfilServico;
    }

    private String urlFotoOuPadrao(String fotoPerfil) {
        return (fotoPerfil == null || fotoPerfil.isBlank()) ? null : fotoPerfil;
    }

    /**
     * Verifica se um e-mail já está cadastrado na aplicação (usado no cadastro, antes do signUp).
     * Endpoint PÚBLICO (liberado na configuração de segurança) que NUNCA revela dados do usuário,
     * apenas se o e-mail existe ou não.
     */
    @PostMapping("/check-email")
    public ResponseEntity<Map<String, Object>> verificarEmailCadastrado(@RequestBody Map<String, String> body) {
        String email = body != null ? body.get("email") : null;
        Map<String, Object> response = new LinkedHashMap<>();

        if (email == null || email.isBlank()) {
            response.put("exists", false);
            response.put("mensagem", "E-mail não informado.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean existe = usuarioRepository.findByEmail(email.toLowerCase().trim()).isPresent()
                || professorRepository.findByEmail(email.toLowerCase().trim()).isPresent()
                || alunoRepository.findByEmail(email.toLowerCase().trim()).isPresent();

        response.put("exists", existe);
        response.put("mensagem", existe ? "E-mail já cadastrado." : "E-mail disponível para cadastro.");
        return ResponseEntity.ok(response);
    }

    // RF19. Visualizar sua própria conta
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> obterProprioPerfil() {
        Usuario logado = usuarioServico.obterUsuarioLogado();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", logado.getId());
        map.put("authId", logado.getAuthId());
        map.put("nome", logado.getNome());
        map.put("email", logado.getEmail());
        map.put("fotoPerfil", urlFotoOuPadrao(logado.getFotoPerfil()));
        map.put("descricao", logado.getDescricao());
        map.put("createdAt", logado.getCreatedAt());

        if (logado instanceof Aluno) {
            Aluno a = (Aluno) logado;
            map.put("tipo", "ALUNO");
            map.put("pontos", a.getPontos());
            map.put("nivel", a.getNivel());
            map.put("xp", a.getXp());
            map.put("quizzesResolvidos", a.getQuizzesResolvidos());
            map.put("pontosParaProximoNivel", triServico.pontosParaNivel(a.getNivel() + 1));
            map.put("pontosDoNivelAtual", triServico.pontosParaNivel(a.getNivel()));
            map.put("posicaoRanking", calcularPosicaoRanking(a.getId()));
            map.put("totalAlunos", alunoRepository.count());
        } else if (logado instanceof Professor) {
            Professor p = (Professor) logado;
            map.put("tipo", "PROFESSOR");
            map.put("instituicao", p.getInstituicao());
            map.put("biografia", p.getDescricao());
            map.put("totalConteudos", conteudoRepository.countByAutorId(p.getId()));
            map.put("totalQuizzes", quizzRepository.countByAutorId(p.getId()));
        }

        return ResponseEntity.ok(map);
    }

    /** Posição do aluno no ranking (1 = primeiro lugar). */
    private int calcularPosicaoRanking(Integer alunoId) {
        List<Aluno> ranking = alunoRepository.findByOrderByPontosDesc();
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getId().equals(alunoId)) {
                return i + 1;
            }
        }
        return 0;
    }

    // Sincronizar/Criar Perfil do usuário autenticado pelo Supabase
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sincronizarPerfil(@Valid @RequestBody SincronizacaoPerfilRequisicao req) {
        Usuario usuario = usuarioServico.sincronizarOuObterPerfil(
                req.getTipo(), req.getNome(), req.getFotoPerfil(), req.getInstituicao(), req.getBiografia());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Perfil sincronizado com sucesso.");
        response.put("usuarioId", usuario.getId());
        response.put("authId", usuario.getAuthId());
        response.put("email", usuario.getEmail());
        response.put("nome", usuario.getNome());
        response.put("tipo", (usuario instanceof Professor) ? "PROFESSOR" : "ALUNO");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gera a URL assinada de upload da foto de perfil (Supabase Storage, bucket "avatars").
     * Fluxo: tela -> POST /api/users/me/photo-url -> upload direto do arquivo na URL
     * devolvida -> PUT /api/users/me com a urlPublica. O binário NUNCA fica na tabela
     * `usuario` e a service key NUNCA vai ao navegador.
     * Body: { "contentType": "image/png", "tamanhoBytes": 12345 }
     */
    @PostMapping("/me/photo-url")
    public ResponseEntity<Map<String, Object>> gerarUrlFotoPerfil(
            @RequestBody Map<String, Object> body) {
        Usuario logado = usuarioServico.obterUsuarioLogado();
        String contentType = body != null && body.get("contentType") != null
                ? body.get("contentType").toString() : null;
        Long tamanhoBytes = null;
        try {
            if (body != null && body.get("tamanhoBytes") != null) {
                tamanhoBytes = Long.parseLong(body.get("tamanhoBytes").toString());
            }
        } catch (NumberFormatException e) {
            throw new RegraNegocioException("Tamanho do arquivo inválido.");
        }
        return ResponseEntity.ok(fotoPerfilServico.gerarUrlAssinada(logado.getId(), contentType, tamanhoBytes));
    }

    /**
     * RF10. Editar os dados de sua conta (nome, foto, descrição/bio e instituição do professor).
     * Devolve o perfil atualizado para a interface atualizar sem recarregar a página.
     */
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> atualizarPropriaConta(@Valid @RequestBody AtualizacaoUsuarioRequisicao req) {
        Usuario logado = usuarioServico.obterUsuarioLogado();

        logado.setNome(req.getNome().trim());

        if (req.getFotoPerfil() != null) {
            String foto = req.getFotoPerfil().trim();
            if (foto.length() > 500) {
                throw new RegraNegocioException("A URL da foto de perfil é muito longa (máximo 500 caracteres).");
            }
            logado.setFotoPerfil(fotoPerfilServico.validarUrlPublica(logado.getId(), foto.isEmpty() ? null : foto));
        }

        String descricao = req.getDescricao() != null ? req.getDescricao() : req.getBiografia();
        if (descricao != null) {
            logado.setDescricao(descricao.isBlank() ? null : descricao.trim());
        }

        if (logado instanceof Professor && req.getInstituicao() != null) {
            ((Professor) logado).setInstituicao(req.getInstituicao().isBlank() ? null : req.getInstituicao().trim());
        }

        usuarioRepository.save(logado);
        return obterProprioPerfil();
    }

    /**
     * RF11. Excluir sua própria conta e os dados associados na aplicação.
     *
     * A exclusão é feita em ordem, respeitando as chaves estrangeiras:
     * respostas/tentativas/histórico/avaliações do usuário, depois conteúdos e quizzes de sua
     * autoria (com as questões) e, por fim, a linha de `usuario`.
     * A conta de AUTENTICAÇÃO permanece no Supabase Auth (o backend não gerencia senhas);
     * a mensagem orienta o usuário a removê-la também no provedor, se desejar.
     */
    @DeleteMapping("/me")
    @Transactional
    public ResponseEntity<Map<String, Object>> excluirPropriaConta() {
        Usuario logado = usuarioServico.obterUsuarioLogado();
        Integer id = logado.getId();

        // 1) Dados que referenciam o usuário
        historicoAcessoRepository.deleteAll(historicoAcessoRepository.findByUsuarioIdOrderByAcessadoEmDesc(id));
        avaliacaoConteudoRepository.deleteAll(avaliacaoConteudoRepository.findByUsuarioId(id));

        // 2) Dados de aluno: respostas e tentativas
        Optional<Aluno> alunoOpt = alunoRepository.findByAuthId(logado.getAuthId());
        if (alunoOpt.isPresent()) {
            respostaQuestaoRepository.deleteAll(respostaQuestaoRepository.findByAlunoId(id));
            tentativaQuizzRepository.deleteAll(tentativaQuizzRepository.findByAlunoIdOrderByRealizadoEmDesc(id));
            alunoRepository.delete(alunoOpt.get());
        }

        // 3) Dados de professor: conteúdos e quizzes (com questões e respostas vinculadas)
        List<Conteudo> meusConteudos = conteudoRepository.findByAutorId(id);
        for (Conteudo c : meusConteudos) {
            avaliacaoConteudoRepository.deleteAll(avaliacaoConteudoRepository.findByConteudoId(c.getConteudoId()));
        }
        conteudoRepository.deleteAll(meusConteudos);

        for (Quizz q : quizzRepository.findByAutorId(id)) {
            List<Questao> questoes = questaoRepository.findByQuizzId(q.getQuizzId());
            for (Questao questao : questoes) {
                respostaQuestaoRepository.deleteAll(
                        respostaQuestaoRepository.findByQuestaoId(questao.getQuestaoId()));
            }
            questaoRepository.deleteAll(questoes);
            quizzRepository.delete(q);
        }

        professorRepository.findByAuthId(logado.getAuthId()).ifPresent(professorRepository::delete);

        // 4) Usuário
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("mensagem", "Conta e dados da aplicação excluídos com sucesso. Se desejar remover "
                + "também o acesso de login, exclua o usuário no painel do Supabase Auth.");
        return ResponseEntity.ok(response);
    }

    /**
     * RF12. Perfil público de outro usuário: expõe apenas informações públicas
     * (nunca senha, tokens, auth_id ou dados internos de autenticação).
     */
    @GetMapping("/profile/{id}")
    public ResponseEntity<Map<String, Object>> obterPerfilDeOutro(@PathVariable("id") Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil de usuário não encontrado."));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", usuario.getId());
        map.put("nome", usuario.getNome());
        map.put("fotoPerfil", urlFotoOuPadrao(usuario.getFotoPerfil()));
        map.put("descricao", usuario.getDescricao());

        if (usuario instanceof Aluno) {
            // RF12.11.2 Pontuação e desempenho do aluno
            Aluno a = (Aluno) usuario;
            map.put("tipo", "ALUNO");
            map.put("pontos", a.getPontos());
            map.put("nivel", a.getNivel());
            map.put("quizzesResolvidos", a.getQuizzesResolvidos());
            map.put("posicaoRanking", calcularPosicaoRanking(a.getId()));
        } else if (usuario instanceof Professor) {
            // RF12.11.1 Conteúdos públicos publicados pelo professor
            Professor p = (Professor) usuario;
            map.put("tipo", "PROFESSOR");
            map.put("instituicao", p.getInstituicao());
            map.put("biografia", p.getDescricao());

            List<Map<String, Object>> publicos = new ArrayList<>();
            for (Conteudo c : conteudoRepository.findByAutorId(p.getId())) {
                if ("PUBLICO".equalsIgnoreCase(c.getVisibilidade())) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("conteudoId", c.getConteudoId());
                    item.put("titulo", c.getTitulo());
                    item.put("materia", c.getMateria());
                    item.put("urlVideo", c.getUrlVideo());
                    publicos.add(item);
                }
            }
            map.put("conteudosPublicos", publicos);
            map.put("totalQuizzes", quizzRepository.countByAutorId(p.getId()));
        }

        return ResponseEntity.ok(map);
    }

    // RF17. Ranking de pontos dos alunos (somente alunos, com dados reais do banco)
    @GetMapping("/ranking")
    public ResponseEntity<List<Map<String, Object>>> obterRanking() {
        List<Aluno> rankingAlunos = alunoRepository.findByOrderByPontosDesc();

        List<Map<String, Object>> rankingOutput = new ArrayList<>();
        int posicao = 1;
        for (Aluno a : rankingAlunos) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("posicao", posicao++);
            map.put("id", a.getId());
            map.put("nome", a.getNome());
            map.put("fotoPerfil", urlFotoOuPadrao(a.getFotoPerfil()));
            map.put("pontos", a.getPontos());
            map.put("nivel", a.getNivel());
            map.put("quizzesResolvidos", a.getQuizzesResolvidos());
            rankingOutput.add(map);
        }

        return ResponseEntity.ok(rankingOutput);
    }
}
