package com.atividadeAvaliativaLP3.demo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RespostaQuestaoRepository extends JpaRepository<RespostaQuestao, Integer> {

    List<RespostaQuestao> findByQuestaoId(Integer questaoId);

    List<RespostaQuestao> findByQuestaoIdOrderByRespostaIdAsc(Integer questaoId);

    List<RespostaQuestao> findByAlunoId(Integer alunoId);

    Optional<RespostaQuestao> findByAlunoIdAndQuestaoId(Integer alunoId, Integer questaoId);

    long countByQuestaoId(Integer questaoId);

    long countByQuestaoIdAndAcertouTrue(Integer questaoId);

    long countByAlunoId(Integer alunoId);
}
