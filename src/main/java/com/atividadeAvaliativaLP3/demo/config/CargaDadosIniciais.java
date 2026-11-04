package com.atividadeAvaliativaLP3.demo.config;

import com.atividadeAvaliativaLP3.demo.*;
import com.atividadeAvaliativaLP3.demo.persistencia.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga de dados de DEMONSTRAÇÃO (professor/alunos/conteúdos/quizzes fictícios).
 *
 * Fica DESLIGADA por padrão (`app.dados-demo.habilitado=false`) porque o ranking, os perfis e a
 * pontuação devem refletir apenas dados REAIS da plataforma. Habilite pela propriedade
 * `app.dados-demo.habilitado=true` apenas para apresentação com banco vazio.
 */
@Component
@ConditionalOnProperty(name = "app.dados-demo.habilitado", havingValue = "true")
public class CargaDadosIniciais implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaDadosIniciais.class);

    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final ConteudoRepository conteudoRepository;
    private final QuizzRepository quizzRepository;
    private final QuestaoRepository questaoRepository;

    public CargaDadosIniciais(ProfessorRepository professorRepository,
                              AlunoRepository alunoRepository,
                              ConteudoRepository conteudoRepository,
                              QuizzRepository quizzRepository,
                              QuestaoRepository questaoRepository) {
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.conteudoRepository = conteudoRepository;
        this.quizzRepository = quizzRepository;
        this.questaoRepository = questaoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.warn("CARGA DE DADOS DE DEMONSTRAÇÃO HABILITADA (app.dados-demo.habilitado=true). "
                + "Os registros criados são fictícios e não devem ser usados como dados reais.");
        // Carga de Professor Exemplo (caso o banco esteja vazio)
        Professor professorExemplo;
        if (professorRepository.count() == 0) {
            professorExemplo = new Professor(
                    "prof-exemplo-01",
                    "Prof. Carlos Silva",
                    "carlossilva@ifsul.edu.br",
                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                    "IFSUL — Câmpus Pelotas",
                    "Doutor em Física e Química. 15 anos preparando alunos para gabaritar a prova de Ciências da Natureza do ENEM."
            );
            professorExemplo = professorRepository.save(professorExemplo);
        } else {
            professorExemplo = professorRepository.findAll().get(0);
        }

        // Carga de Alunos Falsos para Popular o Ranking (RF17)
        if (alunoRepository.count() == 0) {
            Aluno a1 = new Aluno("aluno-fake-1", "Ana Clara Souza", "anaclara@academico.ifsul.edu.br", null);
            a1.setPontos(1450.0);
            a1.setNivel(6);
            a1.setXp(450);
            a1.setQuizzesResolvidos(12);

            Aluno a2 = new Aluno("aluno-fake-2", "Bruno Henrique Oliveira", "bruno.oliveira@academico.ifsul.edu.br", null);
            a2.setPontos(980.0);
            a2.setNivel(4);
            a2.setXp(320);
            a2.setQuizzesResolvidos(8);

            Aluno a3 = new Aluno("aluno-fake-3", "Camila Rodrigues", "camila.rodrigues@academico.ifsul.edu.br", null);
            a3.setPontos(720.0);
            a3.setNivel(3);
            a3.setXp(180);
            a3.setQuizzesResolvidos(6);

            Aluno a4 = new Aluno("aluno-fake-4", "Diego Santos", "diego.santos@academico.ifsul.edu.br", null);
            a4.setPontos(450.0);
            a4.setNivel(2);
            a4.setXp(100);
            a4.setQuizzesResolvidos(4);

            alunoRepository.saveAll(List.of(a1, a2, a3, a4));
        }

        // Carga de Materiais Didáticos Teóricos com Vídeo Anexado (RF1, RF1.1, RF5)
        if (conteudoRepository.count() == 0) {
            Conteudo c1 = new Conteudo(
                    "Cinemática: Movimento Uniformemente Variado (MUV)",
                    "Física",
                    "O Movimento Uniformemente Variado (MUV) é aquele em que a aceleração escalar do móvel é constante e diferente de zero. " +
                    "Consequentemente, a velocidade varia em intervalos de tempos iguais por valores iguais.\n\n" +
                    "Função Horária da Velocidade: v = v0 + a·t\n" +
                    "Equação de Torricelli: v² = v0² + 2·a·Δs\n" +
                    "Função Horária do Espaço: s = s0 + v0·t + (a·t²)/2\n\n" +
                    "No ENEM, dê atenção especial à interpretação de gráficos v x t, onde a área sob a curva representa o deslocamento Δs.",
                    professorExemplo,
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    "PUBLICO"
            );

            Conteudo c2 = new Conteudo(
                    "Estequiometria e Leis Ponderais no ENEM",
                    "Química",
                    "A estequiometria estuda as relações quantitativas de massa, mols e volumes entre reagentes e produtos em uma reação química.\n\n" +
                    "1. Lei de Lavoisier (Conservação das Massas): A soma das massas dos reagentes é igual à soma das massas dos produtos.\n" +
                    "2. Lei de Proust (Proporções Definidas): A proporção em massa das substâncias envolvidas em uma reação é sempre constante.\n\n" +
                    "Passo a passo para resolver no ENEM:\n" +
                    "- Escreva e balanceie a equação química.\n" +
                    "- Identifique as substâncias fornecidas e pedidas.\n" +
                    "- Monte a regra de três relacionando massas molares ou volumes CNTP.",
                    professorExemplo,
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                    "PUBLICO"
            );

            conteudoRepository.saveAll(List.of(c1, c2));
        }

        // Carga de Quiz Criado com Questões Objetivas (RF2, RF2.1, RF13, RF16)
        if (quizzRepository.count() == 0) {
            Quizz q1 = new Quizz(
                    "Simulado ENEM — Física: Mecânica e Cinemática",
                    "Física",
                    professorExemplo,
                    100,
                    300,
                    "MEDIO",
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
            );
            Quizz quizzSalvo1 = quizzRepository.save(q1);

            Questao questao1 = new Questao(
                    "Um veículo parte do repouso (v0 = 0 m/s) e acelera a uma taxa constante de 2 m/s² durante 10 segundos. Qual é a velocidade final alcançada pelo veículo?",
                    "B",
                    "FACIL",
                    quizzSalvo1.getQuizzId(),
                    "10 m/s",
                    "20 m/s",
                    "30 m/s",
                    "40 m/s",
                    "50 m/s"
            );

            Questao questao2 = new Questao(
                    "Qual das leis de Newton estabelece que: 'Para toda força de ação exercida por um corpo sobre outro, existe uma força de reação de mesmo módulo, mesma direção e sentido oposto'?",
                    "C",
                    "MEDIO",
                    quizzSalvo1.getQuizzId(),
                    "Primeira Lei de Newton (Inércia)",
                    "Segunda Lei de Newton (F = m·a)",
                    "Terceira Lei de Newton (Ação e Reação)",
                    "Lei da Gravitação Universal",
                    "Lei de Hooke"
            );

            questaoRepository.saveAll(List.of(questao1, questao2));

            Quizz q2 = new Quizz(
                    "Desafio Rápido — Química Orgânica e Funções",
                    "Química",
                    professorExemplo,
                    80,
                    180,
                    "FACIL",
                    "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
            );
            Quizz quizzSalvo2 = quizzRepository.save(q2);

            Questao questao3 = new Questao(
                    "Qual função orgânica é caracterizada pela presença do grupo hidroxila (-OH) ligado diretamente a um carbono saturado (sp³)?",
                    "D",
                    "FACIL",
                    quizzSalvo2.getQuizzId(),
                    "Aldeído",
                    "Cetona",
                    "Ácido Carboxílico",
                    "Álcool",
                    "Éter"
            );

            questaoRepository.save(questao3);
        }
    }
}
