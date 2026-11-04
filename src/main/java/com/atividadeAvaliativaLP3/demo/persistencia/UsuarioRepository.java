package com.atividadeAvaliativaLP3.demo.persistencia;

import com.atividadeAvaliativaLP3.demo.Usuario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByAuthId(String authId);

    Optional<Usuario> findByEmail(String email);

    // ---------------------------------------------------------------------
    // Migração de papel (Aluno <-> Professor)
    //
    // A linha de `usuario` NUNCA é apagada: apenas a linha de especialização
    // (`aluno` ou `professor`) é trocada, preservando o id e, com ele, todas as
    // referências (conteúdos, quizzes, histórico, avaliações).
    // Usado somente quando não há dependências que impeçam a troca.
    // ---------------------------------------------------------------------

    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM aluno WHERE usuario_id = :usuarioId")
    void removerEspecializacaoAluno(@Param("usuarioId") Integer usuarioId);

    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO professor (usuario_id, instituicao) VALUES (:usuarioId, :instituicao)")
    void inserirEspecializacaoProfessor(@Param("usuarioId") Integer usuarioId, @Param("instituicao") String instituicao);

    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM professor WHERE usuario_id = :usuarioId")
    void removerEspecializacaoProfessor(@Param("usuarioId") Integer usuarioId);

    @Modifying
    @Query(nativeQuery = true, value = "INSERT INTO aluno (usuario_id, pontos, nivel, xp, quizzes_resolvidos) VALUES (:usuarioId, 0, 1, 0, 0)")
    void inserirEspecializacaoAluno(@Param("usuarioId") Integer usuarioId);
}
