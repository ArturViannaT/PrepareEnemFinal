package com.atividadeAvaliativaLP3.demo.persistencia;

import com.atividadeAvaliativaLP3.demo.Professor;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Integer> {

    Optional<Professor> findByAuthId(String authId);

    Optional<Professor> findByEmail(String email);
}
