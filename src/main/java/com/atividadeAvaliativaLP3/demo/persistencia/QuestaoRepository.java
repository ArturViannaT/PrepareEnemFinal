package com.atividadeAvaliativaLP3.demo.persistencia;

import com.atividadeAvaliativaLP3.demo.Questao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestaoRepository extends JpaRepository<Questao, Integer> {

    List<Questao> findByDificuldadeInicial(String dificuldadeInicial);

    List<Questao> findByDificuldadeAtual(String dificuldadeAtual);

    List<Questao> findByQuizzId(Integer quizzId);
}
