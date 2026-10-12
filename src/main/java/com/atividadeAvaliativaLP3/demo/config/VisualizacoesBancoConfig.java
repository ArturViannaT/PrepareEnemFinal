package com.atividadeAvaliativaLP3.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Cria/atualiza VISUALIZAÇÕES (views) de leitura para facilitar a gestão do banco.
 *
 * Motivo: com herança JPA JOINED, as tabelas `aluno` e `professor` guardam apenas o
 * `usuario_id` + os campos específicos. Isso é correto no modelo, mas dificulta abrir a
 * tabela no painel do Supabase e não saber quem é quem. As views abaixo juntam os dados de
 * `usuario` com a especialização, sem DUPLICAR nem alterar dados (CREATE OR REPLACE VIEW é
 * idempotente e não destrutivo).
 */
@Component
@Order(1000)
public class VisualizacoesBancoConfig implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(VisualizacoesBancoConfig.class);

    private final JdbcTemplate jdbcTemplate;

    public VisualizacoesBancoConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String[] VIEWS = {
            """
            CREATE OR REPLACE VIEW vw_usuario_completo AS
            SELECT u.id,
                   u.auth_id,
                   u.nome,
                   u.email,
                   CASE WHEN p.usuario_id IS NOT NULL THEN 'PROFESSOR' ELSE 'ALUNO' END AS tipo,
                   u.foto_perfil,
                   u.descricao,
                   u.created_at
              FROM usuario u
              LEFT JOIN professor p ON p.usuario_id = u.id
            """,
            """
            CREATE OR REPLACE VIEW vw_aluno_completo AS
            SELECT a.usuario_id,
                   u.nome,
                   u.email,
                   u.auth_id,
                   u.foto_perfil,
                   u.descricao,
                   a.pontos,
                   a.nivel,
                   a.xp,
                   a.quizzes_resolvidos,
                   (SELECT COUNT(*) FROM resposta_questao r WHERE r.aluno_id = a.usuario_id) AS total_respostas,
                   (SELECT COUNT(*) FROM resposta_questao r WHERE r.aluno_id = a.usuario_id AND r.acertou = true) AS total_acertos
              FROM aluno a
              JOIN usuario u ON u.id = a.usuario_id
            """,
            """
            CREATE OR REPLACE VIEW vw_professor_completo AS
            SELECT p.usuario_id,
                   u.nome,
                   u.email,
                   u.auth_id,
                   u.foto_perfil,
                   p.instituicao,
                   u.descricao,
                   (SELECT COUNT(*) FROM conteudo c WHERE c.autor_id = p.usuario_id) AS total_conteudos,
                   (SELECT COUNT(*) FROM quizz q WHERE q.autor_id = p.usuario_id) AS total_quizzes
              FROM professor p
              JOIN usuario u ON u.id = p.usuario_id
            """,
            """
            CREATE OR REPLACE VIEW vw_questao_desempenho AS
            SELECT q.questao_id,
                   q.quizz_id,
                   qz.nome_quizz AS quiz,
                   q.pergunta,
                   q.dificuldade_inicial,
                   q.dificuldade_atual,
                   CASE q.dificuldade_atual
                        WHEN 'FACIL' THEN 10
                        WHEN 'DIFICIL' THEN 100
                        ELSE 50
                   END AS valor_pontos_atual,
                   q.total_respostas,
                   q.total_acertos,
                   q.percentual_acertos,
                   q.calibrada
              FROM questao q
              LEFT JOIN quizz qz ON qz.quizz_id = q.quizz_id
            """,
            """
            CREATE OR REPLACE VIEW vw_resposta_detalhada AS
            SELECT r.resposta_id,
                   u.nome AS aluno,
                   r.questao_id,
                   r.quizz_id,
                   q.dificuldade_atual,
                   r.acertou,
                   r.pontos_aplicados,
                   r.tempo_segundos,
                   r.respondido_em
              FROM resposta_questao r
              JOIN usuario u ON u.id = r.aluno_id
              LEFT JOIN questao q ON q.questao_id = r.questao_id
            """
    };

    @Override
    public void run(String... args) {
        int criadas = 0;
        for (String sql : VIEWS) {
            try {
                jdbcTemplate.execute(sql);
                criadas++;
            } catch (Exception e) {
                // A criação das views é um recurso de apoio à gestão: se falhar, o sistema continua no ar.
                Throwable causa = (e instanceof org.springframework.core.NestedRuntimeException nre)
                        ? nre.getMostSpecificCause()
                        : e;
                log.warn("Não foi possível criar/atualizar uma view de apoio no banco: {}",
                        causa.getMessage());
            }
        }
        log.info("Views de apoio do banco verificadas: {}/{} (vw_usuario_completo, vw_aluno_completo, "
                + "vw_professor_completo, vw_questao_desempenho, vw_resposta_detalhada)", criadas, VIEWS.length);
    }
}
