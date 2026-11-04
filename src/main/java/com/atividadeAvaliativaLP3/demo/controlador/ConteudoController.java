package com.atividadeAvaliativaLP3.demo.controlador;

import com.atividadeAvaliativaLP3.demo.*;
import com.atividadeAvaliativaLP3.demo.persistencia.*;
import com.atividadeAvaliativaLP3.demo.dto.AvaliacaoRequisicao;
import com.atividadeAvaliativaLP3.demo.dto.ConteudoRequisicao;
import com.atividadeAvaliativaLP3.demo.dto.ConteudoResposta;
import com.atividadeAvaliativaLP3.demo.excecao.RecursoNaoEncontradoException;
import com.atividadeAvaliativaLP3.demo.excecao.RegraNegocioException;
import com.atividadeAvaliativaLP3.demo.servico.UsuarioServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contents")
public class ConteudoController {

    private final ConteudoRepository conteudoRepository;
    private final AvaliacaoConteudoRepository avaliacaoConteudoRepository;
    private final HistoricoAcessoRepository historicoAcessoRepository;
    private final UsuarioServico usuarioServico;

    public ConteudoController(ConteudoRepository conteudoRepository,
                              AvaliacaoConteudoRepository avaliacaoConteudoRepository,
                              HistoricoAcessoRepository historicoAcessoRepository,
                              UsuarioServico usuarioServico) {
        this.conteudoRepository = conteudoRepository;
        this.avaliacaoConteudoRepository = avaliacaoConteudoRepository;
        this.historicoAcessoRepository = historicoAcessoRepository;
        this.usuarioServico = usuarioServico;
    }

    private double calcularMediaAvaliacao(Integer conteudoId) {
        List<AvaliacaoConteudo> avaliacoes = avaliacaoConteudoRepository.findByConteudoId(conteudoId);
        double media = avaliacoes.stream()
                .mapToInt(AvaliacaoConteudo::getEstrelas)
                .average()
                .orElse(0.0);
        return Math.round(media * 100.0) / 100.0;
    }

    private ConteudoResposta converterParaResposta(Conteudo c) {
        List<AvaliacaoConteudo> avaliacoes = avaliacaoConteudoRepository.findByConteudoId(c.getConteudoId());
        double media = avaliacoes.stream().mapToInt(AvaliacaoConteudo::getEstrelas).average().orElse(0.0);
        return new ConteudoResposta(
                c.getConteudoId(),
                c.getTitulo(),
                c.getMateria(),
                c.getTexto(),
                c.getAutor() != null ? c.getAutor().getEmail() : "",
                c.getAutor() != null ? c.getAutor().getId() : null,
                c.getAutor() != null ? c.getAutor().getNome() : "Autor desconhecido",
                c.getUrlVideo(),
                c.getVisibilidade(),
                Math.round(media * 100.0) / 100.0,
                avaliacoes.size()
        );
    }

    // RF1. Publicar conteúdo teórico (Professor autenticado)
    @PostMapping
    public ResponseEntity<ConteudoResposta> publicarConteudo(@Valid @RequestBody ConteudoRequisicao req) {
        Usuario logado = usuarioServico.obterUsuarioLogado();
        if (!(logado instanceof Professor)) {
            throw new RegraNegocioException("Somente professores cadastrados podem publicar conteúdos teóricos.");
        }

        Conteudo conteudo = new Conteudo();
        conteudo.setTitulo(req.getTitulo());
        conteudo.setMateria(req.getMateria());
        conteudo.setTexto(req.getTexto());
        conteudo.setAutor(logado);
        conteudo.setUrlVideo(req.getUrlVideo());
        conteudo.setVisibilidade(req.getVisibilidade().toUpperCase());

        Conteudo salvo = conteudoRepository.save(conteudo);
        return ResponseEntity.status(HttpStatus.CREATED).body(converterParaResposta(salvo));
    }

    // RF1.2. Editar conteúdo de sua autoria
    @PutMapping("/{id}")
    public ResponseEntity<ConteudoResposta> editarConteudo(@PathVariable("id") Integer id, @Valid @RequestBody ConteudoRequisicao req) {
        Usuario logado = usuarioServico.obterUsuarioLogado();

        Conteudo conteudo = conteudoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteúdo teórico não encontrado"));

        if (!conteudo.getAutor().getId().equals(logado.getId())) {
            throw new RegraNegocioException("Acesso negado: Você só pode editar conteúdos de sua própria autoria.");
        }

        conteudo.setTitulo(req.getTitulo());
        conteudo.setMateria(req.getMateria());
        conteudo.setTexto(req.getTexto());
        conteudo.setUrlVideo(req.getUrlVideo());
        conteudo.setVisibilidade(req.getVisibilidade().toUpperCase());

        Conteudo atualizado = conteudoRepository.save(conteudo);
        return ResponseEntity.ok(converterParaResposta(atualizado));
    }

    // RF1.3. Excluir conteúdo de sua autoria
    @DeleteMapping("/{id}")
    public ResponseEntity<String> excluirConteudo(@PathVariable("id") Integer id) {
        Usuario logado = usuarioServico.obterUsuarioLogado();

        Conteudo conteudo = conteudoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteúdo teórico não encontrado"));

        if (!conteudo.getAutor().getId().equals(logado.getId())) {
            throw new RegraNegocioException("Acesso negado: Você só pode excluir conteúdos de sua própria autoria.");
        }

        // Limpeza das avaliações do conteúdo (referenciam o conteúdo apenas por id) para
        // não deixar registros órfãos. O histórico de acesso é mantido (exibe "Conteúdo Excluído").
        avaliacaoConteudoRepository.deleteAll(avaliacaoConteudoRepository.findByConteudoId(id));

        conteudoRepository.delete(conteudo);
        return ResponseEntity.ok("Conteúdo excluído com sucesso.");
    }

    // RF1.4. Visualizar conteúdos de sua autoria (Professor autenticado)
    @GetMapping("/my")
    public ResponseEntity<List<ConteudoResposta>> visualizarPropriosConteudos() {
        Usuario logado = usuarioServico.obterUsuarioLogado();

        List<Conteudo> meusConteudos = conteudoRepository.findByAutorId(logado.getId());
        List<ConteudoResposta> respostas = meusConteudos.stream()
                .map(this::converterParaResposta)
                .collect(Collectors.toList());

        return ResponseEntity.ok(respostas);
    }

    // RF5. Acessar conteúdos públicos disponíveis na plataforma (Listagem)
    @GetMapping
    public ResponseEntity<List<ConteudoResposta>> listarConteudosPublicos() {
        List<Conteudo> publicos = conteudoRepository.findByVisibilidade("PUBLICO");
        List<ConteudoResposta> respostas = publicos.stream()
                .map(this::converterParaResposta)
                .collect(Collectors.toList());

        return ResponseEntity.ok(respostas);
    }

    // RF5 & RF6. Acessar um conteúdo específico por ID e registrar no histórico
    @GetMapping("/{id}")
    public ResponseEntity<ConteudoResposta> obterConteudoEGravarHistorico(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(registrarAcessoConteudo(id));
    }

    @PostMapping("/{id}/access")
    public ResponseEntity<ConteudoResposta> registrarAcesso(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(registrarAcessoConteudo(id));
    }

    private ConteudoResposta registrarAcessoConteudo(Integer id) {
        Usuario logado = usuarioServico.obterUsuarioLogado();

        Conteudo conteudo = conteudoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteúdo não encontrado"));

        if ("PRIVADO".equalsIgnoreCase(conteudo.getVisibilidade()) &&
            !conteudo.getAutor().getId().equals(logado.getId())) {
            throw new RegraNegocioException("Acesso negado: Este conteúdo é privado.");
        }

        if (!(logado instanceof Aluno)) {
            return converterParaResposta(conteudo);
        }

        // Mantém apenas o acesso mais recente por aluno e material.
        HistoricoAcesso historico = historicoAcessoRepository
                .findFirstByUsuarioIdAndConteudoIdOrderByAcessadoEmDesc(logado.getId(), id)
                .orElseGet(() -> new HistoricoAcesso(logado, id));
        historico.setAcessadoEm(LocalDateTime.now());
        historico = historicoAcessoRepository.save(historico);
        historicoAcessoRepository.removerAcessosDuplicadosDeConteudo(
                logado.getId(), id, historico.getHistoricoId());

        return converterParaResposta(conteudo);
    }

    // RF4. Avaliar conteúdo publicado (1 a 5 estrelas) — atualiza a avaliação existente
    @PostMapping("/{id}/rate")
    public ResponseEntity<Map<String, Object>> avaliarConteudo(@PathVariable("id") Integer id,
                                                               @Valid @RequestBody AvaliacaoRequisicao req) {
        Usuario logado = usuarioServico.obterUsuarioLogado();

        Conteudo conteudo = conteudoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteúdo não encontrado"));

        if ("PRIVADO".equalsIgnoreCase(conteudo.getVisibilidade()) &&
            !conteudo.getAutor().getId().equals(logado.getId())) {
            throw new RegraNegocioException("Não é permitido avaliar conteúdos privados de terceiros.");
        }

        int estrelas = req.getEstrelas();
        if (estrelas < 1 || estrelas > 5) {
            throw new RegraNegocioException("A avaliação deve ser de 1 a 5 estrelas.");
        }

        Optional<AvaliacaoConteudo> avalOpt = avaliacaoConteudoRepository
                .findByUsuarioIdAndConteudoId(logado.getId(), id);

        AvaliacaoConteudo avaliacao;
        if (avalOpt.isPresent()) {
            avaliacao = avalOpt.get();
            avaliacao.setEstrelas(estrelas);
        } else {
            avaliacao = new AvaliacaoConteudo(logado, id, estrelas);
        }

        avaliacaoConteudoRepository.save(avaliacao);

        long total = avaliacaoConteudoRepository.findByConteudoId(id).size();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("mensagem", "Avaliação registrada com sucesso!");
        response.put("conteudoId", id);
        response.put("estrelas", estrelas);
        response.put("mediaEstrelas", calcularMediaAvaliacao(id));
        response.put("totalAvaliacoes", total);

        return ResponseEntity.ok(response);
    }

    // Consulta a avaliação do conteúdo (média, total e a avaliação do próprio usuário)
    @GetMapping("/{id}/rating")
    public ResponseEntity<Map<String, Object>> consultarAvaliacao(@PathVariable("id") Integer id) {
        Usuario logado = usuarioServico.obterUsuarioLogado();

        conteudoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conteúdo não encontrado"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("conteudoId", id);
        response.put("mediaEstrelas", calcularMediaAvaliacao(id));
        response.put("totalAvaliacoes", avaliacaoConteudoRepository.findByConteudoId(id).size());
        response.put("minhaAvaliacao", avaliacaoConteudoRepository
                .findByUsuarioIdAndConteudoId(logado.getId(), id)
                .map(AvaliacaoConteudo::getEstrelas)
                .orElse(0));
        return ResponseEntity.ok(response);
    }

    // RF6. Visualizar o histórico de conteúdos acessados
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> obterHistoricoAcesso() {
        Usuario logado = usuarioServico.obterUsuarioLogado();

        List<HistoricoAcesso> historico = historicoAcessoRepository
                .findByUsuarioIdOrderByAcessadoEmDesc(logado.getId());

        Map<String, Map<String, Object>> acessosUnicos = new LinkedHashMap<>();
        for (HistoricoAcesso h : historico) {
            if (h.getConteudoId() == null) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("historicoId", h.getHistoricoId());
            map.put("acessadoEm", h.getAcessadoEm());

            String chave = "material:" + h.getConteudoId();
            map.put("tipo", "Material");
            map.put("conteudoId", h.getConteudoId());
            Optional<Conteudo> contOpt = conteudoRepository.findById(h.getConteudoId());
            if (contOpt.isPresent()) {
                map.put("titulo", contOpt.get().getTitulo());
                map.put("materia", contOpt.get().getMateria());
            } else {
                map.put("titulo", "Material excluído");
            }
            acessosUnicos.putIfAbsent(chave, map);
        }

        return ResponseEntity.ok(new ArrayList<>(acessosUnicos.values()));
    }
}
