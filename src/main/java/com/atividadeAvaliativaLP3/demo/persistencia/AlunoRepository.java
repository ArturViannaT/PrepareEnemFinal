package com.atividadeAvaliativaLP3.demo.persistencia;

import com.atividadeAvaliativaLP3.demo.*;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Integer> {

    Optional<Aluno> findByAuthId(String authId);

    Optional<Aluno> findByEmail(String email);

    List<Aluno> findByOrderByPontosDesc();

    long countByEmail(String email);
}
