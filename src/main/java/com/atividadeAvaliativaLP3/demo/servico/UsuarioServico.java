package com.atividadeAvaliativaLP3.demo.servico;

import com.atividadeAvaliativaLP3.demo.*;
import com.atividadeAvaliativaLP3.demo.excecao.RegraNegocioException;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Serviço de identidade do usuário.
 *
 * FLUXO DE AUTENTICAÇÃO (não existe autenticação própria no backend):
 *   Supabase Auth -> JWT (assinado, validado por JWKS) -> claim `sub` (auth_id)
 *   -> linha em `usuario` -> subtipo JPA (Aluno ou Professor) -> permissões.
 *
 * O PAPEL DO USUÁRIO é PERSISTIDO, nunca inferido por heurística a cada requisição:
 *   1) fonte de verdade: o subtipo da linha de `usuario` (Aluno ou Professor);
 *   2) na PRIMEIRA sincronização o papel vem de `user_metadata.tipo` (definido no cadastro
 *      no Supabase Auth) ou, na falta dele, do domínio institucional do e-mail;
 *   3) `app.email.professores-autorizados` libera e-mails excepcionais (contas criadas
 *      manualmente no painel do Supabase).
 *
 * Este serviço NUNCA apaga a linha de `usuario`. Para corrigir papel, apenas a linha de
 * especialização (`aluno`/`professor`) é trocada — e somente se não existirem dependências
 * (tentativas, respostas, conteúdos, quizzes) que impeçam a troca.
 */
@Service
public class UsuarioServico {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServico.class);

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final ConteudoRepository conteudoRepository;
    private final QuizzRepository quizzRepository;
    private final TentativaQuizzRepository tentativaQuizzRepository;
    private final RespostaQuestaoRepository respostaQuestaoRepository;
    private final EntityManager entityManager;

    @Value("${app.email.professor-domains:ifsul.edu.br}")
    private String professorDomains;

    @Value("${app.email.aluno-domains:academico.ifsul.edu.br}")
    private String alunoDomains;

    @Value("${app.email.professores-autorizados:}")
    private String professoresAutorizados;

    public UsuarioServico(UsuarioRepository usuarioRepository,
                         AlunoRepository alunoRepository,
                         ProfessorRepository professorRepository,
                         ConteudoRepository conteudoRepository,
                         QuizzRepository quizzRepository,
                         TentativaQuizzRepository tentativaQuizzRepository,
                         RespostaQuestaoRepository respostaQuestaoRepository,
                         EntityManager entityManager) {
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.conteudoRepository = conteudoRepository;
        this.quizzRepository = quizzRepository;
        this.tentativaQuizzRepository = tentativaQuizzRepository;
        this.respostaQuestaoRepository = respostaQuestaoRepository;
        this.entityManager = entityManager;
    }

    // ===================== JWT / IDENTIDADE =====================

    public Jwt getJwtAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt)) {
            throw new RegraNegocioException("Usuário não autenticado no Supabase Auth.");
        }
        return (Jwt) auth.getPrincipal();
    }

    public String getAuthIdLogado() {
        return getJwtAutenticado().getClaimAsString("sub");
    }

    public String getEmailLogado() {
        return getJwtAutenticado().getClaimAsString("email");
    }

    /**
     * Papel declarado pelo usuário no Supabase Auth (user_metadata.tipo / app_metadata.tipo).
     * Retorna null quando não há declaração — nesse caso valem as regras de domínio.
     */
    public String obterTipoDeclaradoNoToken() {
        Jwt jwt = getJwtAutenticado();
        String tipo = extrairTipoDoMetadata(jwt.getClaimAsMap("user_metadata"));
        if (tipo == null) {
            tipo = extrairTipoDoMetadata(jwt.getClaimAsMap("app_metadata"));
        }
        return tipo;
    }

    private String extrairTipoDoMetadata(Map<String, Object> metadata) {
        if (metadata == null) {
            return null;
        }
        Object valor = metadata.get("tipo");
        if (valor == null) {
            return null;
        }
        String tipo = valor.toString().trim().toUpperCase();
        return tipo.startsWith("PROF") ? "PROFESSOR" : "ALUNO";
    }

    /**
     * Obtém o usuário da aplicação correspondente ao JWT atual.
     * No primeiro acesso (sem linha em `usuario`), cria o perfil com o papel correto.
     */
    @Transactional
    public Usuario obterUsuarioLogado() {
        String authId = getAuthIdLogado();
        String email = getEmailLogado();

        Optional<Usuario> existente = usuarioRepository.findByAuthId(authId);
        if (existente.isPresent()) {
            Usuario u = existente.get();
            String tipoDeclarado = obterTipoDeclaradoNoToken();

            // Corrige o papel apenas quando há evidência explícita (declaração do token),
            // e nunca apagando a linha de `usuario`.
            if (tipoDeclarado != null && !tipoDeclarado.equals(u.getTipo())) {
                Usuario migrado = migrarPapelSePossivel(u, tipoDeclarado, "declaração do token Supabase");
                if (migrado != null) {
                    return migrado;
                }
            }
            return u;
        }

        return criarPerfilInicial(authId, email, obterTipoDeclaradoNoToken(), null);
    }

    // ===================== CRIAÇÃO / INFERÊNCIA DE PAPEL =====================

    /**
     * Cria a linha de `usuario` (e a especialização correta) para um usuário do Supabase Auth
     * que ainda não existe na aplicação.
     *
     * @param tipoDeclarado papel informado no cadastro (user_metadata.tipo) — pode ser null.
     */
    @Transactional
    public Usuario criarPerfilInicial(String authId, String email, String tipoDeclarado, String nome) {
        String emailNormalizado = (email == null || email.isBlank())
                ? "usuario@supabase.io" : email.toLowerCase().trim();
        String nomeFinal = (nome != null && !nome.isBlank()) ? nome
                : (emailNormalizado.contains("@")
                        ? emailNormalizado.substring(0, emailNormalizado.indexOf('@'))
                        : emailNormalizado);

        String tipo = tipoDeclarado != null ? tipoDeclarado : inferirTipoPorEmail(emailNormalizado);

        if ("PROFESSOR".equals(tipo)) {
            log.info("Criando perfil PROFESSOR para {} (origem do papel: {})", emailNormalizado,
                    tipoDeclarado != null ? "declaração do usuário" : "domínio institucional");
            Professor prof = new Professor(authId, nomeFinal, emailNormalizado, null, null, null);
            return professorRepository.save(prof);
        }

        Aluno aluno = new Aluno(authId, nomeFinal, emailNormalizado, null);
        return alunoRepository.save(aluno);
    }

    /**
     * Infere o papel pelo domínio do e-mail (usado apenas na criação do perfil).
     * A comparação é feita pelo DOMÍNIO (parte após o @), com correspondência exata ou de
     * subdomínio — nunca por "contém a palavra professor/aluno".
     * Domínios de aluno são avaliados primeiro por serem mais específicos
     * (ex.: academico.ifsul.edu.br antes de ifsul.edu.br).
     */
    public String inferirTipoPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return "ALUNO";
        }
        String emailLower = email.toLowerCase().trim();

        if (estaNaListaAutorizada(emailLower)) {
            return "PROFESSOR";
        }
        if (dominioCorresponde(emailLower, alunoDomains)) {
            return "ALUNO";
        }
        if (dominioCorresponde(emailLower, professorDomains)) {
            return "PROFESSOR";
        }
        return "ALUNO";
    }

    /** E-mails explicitamente autorizados como professor (contas criadas fora do fluxo de cadastro). */
    private boolean estaNaListaAutorizada(String emailLower) {
        if (professoresAutorizados == null || professoresAutorizados.isBlank()) {
            return false;
        }
        return Arrays.stream(professoresAutorizados.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(s -> s.equalsIgnoreCase(emailLower));
    }

    /** Verifica se o domínio do e-mail é igual a (ou subdomínio de) algum domínio da lista. */
    private boolean dominioCorresponde(String emailLower, String dominiosConfigurados) {
        int arroba = emailLower.indexOf('@');
        if (arroba < 0 || dominiosConfigurados == null || dominiosConfigurados.isBlank()) {
            return false;
        }
        String dominio = emailLower.substring(arroba + 1);
        return Arrays.stream(dominiosConfigurados.split(","))
                .map(d -> d.trim().toLowerCase())
                .filter(d -> !d.isEmpty())
                .anyMatch(d -> dominio.equals(d) || dominio.endsWith("." + d));
    }

    /** Validação usada no CADASTRO explícito de professor (RF8). */
    public void validarEmailProfessor(String email) {
        if (email == null || email.isBlank()) {
            throw new RegraNegocioException("E-mail é obrigatório.");
        }
        String emailLower = email.toLowerCase().trim();

        if (dominioCorresponde(emailLower, alunoDomains)) {
            throw new RegraNegocioException("Este e-mail é de aluno. Por favor, cadastre-se como Aluno.");
        }
        if (estaNaListaAutorizada(emailLower)) {
            return;
        }
        if (!dominioCorresponde(emailLower, professorDomains)) {
            throw new RegraNegocioException(
                    "E-mail institucional de professor inválido. Domínios permitidos: " + professorDomains);
        }
    }

    /** Validação usada no CADASTRO explícito de aluno (RF9). */
    public void validarEmailAluno(String email) {
        if (email == null || email.isBlank()) {
            throw new RegraNegocioException("E-mail é obrigatório.");
        }
        if (!dominioCorresponde(email.toLowerCase().trim(), alunoDomains)) {
            throw new RegraNegocioException(
                    "E-mail acadêmico de aluno inválido. Domínios permitidos: " + alunoDomains);
        }
    }

    /**
     * Sincroniza/atualiza o perfil do usuário autenticado (chamado pelo frontend após o cadastro
     * e no login). NUNCA apaga o usuário e NUNCA cria um segundo sistema de autenticação:
     * a senha continua sendo responsabilidade exclusiva do Supabase Auth.
     */
    @Transactional
    public Usuario sincronizarOuObterPerfil(String tipo, String nome, String fotoPerfil,
                                            String instituicao, String biografia) {
        String authId = getAuthIdLogado();
        String email = getEmailLogado();

        Optional<Usuario> usuarioOpt = usuarioRepository.findByAuthId(authId);
        if (usuarioOpt.isEmpty()) {
            // Cadastro explícito: o tipo informado é validado contra os domínios permitidos.
            String tipoDeclarado = (tipo != null && "PROFESSOR".equalsIgnoreCase(tipo.trim())) ? "PROFESSOR"
                    : (tipo != null && "ALUNO".equalsIgnoreCase(tipo.trim()) ? "ALUNO" : null);
            if ("PROFESSOR".equals(tipoDeclarado)) {
                validarEmailProfessor(email);
            } else if ("ALUNO".equals(tipoDeclarado)) {
                validarEmailAluno(email);
            }
            Usuario novo = criarPerfilInicial(authId, email, tipoDeclarado, nome);
            return aplicarDadosDoPerfil(novo, nome, fotoPerfil, instituicao, biografia);
        }

        Usuario u = usuarioOpt.get();

        // Se a declaração (token ou requisição) divergir do papel persistido, tenta migração segura.
        String tipoDeclarado = obterTipoDeclaradoNoToken();
        if (tipoDeclarado == null && tipo != null && !tipo.isBlank()) {
            tipoDeclarado = "PROFESSOR".equalsIgnoreCase(tipo.trim()) ? "PROFESSOR" : "ALUNO";
        }
        if (tipoDeclarado != null && !tipoDeclarado.equals(u.getTipo())) {
            Usuario migrado = migrarPapelSePossivel(u, tipoDeclarado, "sincronização explícita de perfil");
            if (migrado != null) {
                u = migrado;
            }
        }

        return aplicarDadosDoPerfil(u, nome, fotoPerfil, instituicao, biografia);
    }

    private Usuario aplicarDadosDoPerfil(Usuario u, String nome, String fotoPerfil,
                                        String instituicao, String biografia) {
        if (nome != null && !nome.isBlank()) {
            u.setNome(nome);
        }
        if (fotoPerfil != null && !fotoPerfil.isBlank()) {
            u.setFotoPerfil(fotoPerfil);
        }
        if (biografia != null && !biografia.isBlank()) {
            u.setDescricao(biografia);
        }
        if (u instanceof Professor && instituicao != null && !instituicao.isBlank()) {
            ((Professor) u).setInstituicao(instituicao);
        }
        return usuarioRepository.save(u);
    }

    // ===================== MIGRAÇÃO SEGURA DE PAPEL =====================

    /**
     * Troca o papel (Aluno <-> Professor) PRESERVANDO a linha de `usuario` e o seu id.
     *
     * Retorna null (mantendo o papel atual, sem apagar nada) quando a troca não é segura, isto é,
     * quando o usuário já possui dados vinculados ao papel atual:
     *   - Aluno com tentativas/respostas de quiz;
     *   - Professor com conteúdos/quizzes de sua autoria.
     */
    @Transactional
    public Usuario migrarPapelSePossivel(Usuario usuario, String tipoDesejado, String origem) {
        if (usuario == null || usuario.getId() == null || tipoDesejado == null) {
            return null;
        }
        if (tipoDesejado.equalsIgnoreCase(usuario.getTipo())) {
            return usuario;
        }

        if (usuario instanceof Aluno) {
            long dependencias = tentativaQuizzRepository.countByAlunoId(usuario.getId())
                    + respostaQuestaoRepository.countByAlunoId(usuario.getId());
            if (dependencias > 0) {
                log.warn("Papel de {} ({}) não alterado para PROFESSOR: já possui {} registro(s) de aluno. Origem: {}",
                        usuario.getEmail(), usuario.getId(), dependencias, origem);
                return null;
            }
            usuarioRepository.removerEspecializacaoAluno(usuario.getId());
            usuarioRepository.inserirEspecializacaoProfessor(usuario.getId(), null);
        } else {
            long dependencias = conteudoRepository.countByAutorId(usuario.getId())
                    + quizzRepository.countByAutorId(usuario.getId());
            if (dependencias > 0) {
                log.warn("Papel de {} ({}) não alterado para ALUNO: já possui {} conteúdo(s)/quiz(zes). Origem: {}",
                        usuario.getEmail(), usuario.getId(), dependencias, origem);
                return null;
            }
            usuarioRepository.removerEspecializacaoProfessor(usuario.getId());
            usuarioRepository.inserirEspecializacaoAluno(usuario.getId());
        }

        entityManager.flush();
        entityManager.clear();
        log.info("Papel do usuário {} ({}) alterado para {}. Origem: {}",
                usuario.getEmail(), usuario.getId(), tipoDesejado, origem);
        return usuarioRepository.findById(usuario.getId()).orElse(usuario);
    }

    // ===================== CONSULTAS DE APOIO =====================

    public long totalAlunos() {
        return alunoRepository.count();
    }

    public long totalProfessores() {
        return professorRepository.count();
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}
