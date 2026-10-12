package com.atividadeAvaliativaLP3.demo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TentativaQuizzRepository extends JpaRepository<TentativaQuizz, Integer> {

    List<TentativaQuizz> findByAlunoIdOrderByRealizadoEmDesc(Integer alunoId);

    long countByAlunoId(Integer alunoId);
}
