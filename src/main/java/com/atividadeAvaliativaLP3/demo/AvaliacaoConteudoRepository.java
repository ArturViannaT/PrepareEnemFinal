package com.atividadeAvaliativaLP3.demo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvaliacaoConteudoRepository extends JpaRepository<AvaliacaoConteudo, Integer> {

    List<AvaliacaoConteudo> findByConteudoId(Integer conteudoId);

    List<AvaliacaoConteudo> findByUsuarioId(Integer usuarioId);

    Optional<AvaliacaoConteudo> findByUsuarioIdAndConteudoId(Integer usuarioId, Integer conteudoId);
}
