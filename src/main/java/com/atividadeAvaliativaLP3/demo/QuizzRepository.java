package com.atividadeAvaliativaLP3.demo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizzRepository extends JpaRepository<Quizz, Integer> {

    Optional<Quizz> findByNomeQuizz(String nomeQuizz);

    List<Quizz> findByMateria(String materia);

    List<Quizz> findByAutorId(Integer autorId);

    long countByAutorId(Integer autorId);
}
