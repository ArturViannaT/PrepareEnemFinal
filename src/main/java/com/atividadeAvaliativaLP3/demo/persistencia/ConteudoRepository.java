package com.atividadeAvaliativaLP3.demo.persistencia;

import com.atividadeAvaliativaLP3.demo.*;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConteudoRepository extends JpaRepository<Conteudo, Integer> {

    List<Conteudo> findByMateria(String materia);

    List<Conteudo> findByAutorId(Integer autorId);

    List<Conteudo> findByVisibilidade(String visibilidade);

    long countByAutorId(Integer autorId);
}
