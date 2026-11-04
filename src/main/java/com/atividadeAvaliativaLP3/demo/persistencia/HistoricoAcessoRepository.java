package com.atividadeAvaliativaLP3.demo.persistencia;

import com.atividadeAvaliativaLP3.demo.HistoricoAcesso;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface HistoricoAcessoRepository extends JpaRepository<HistoricoAcesso, Integer> {

    List<HistoricoAcesso> findByUsuarioIdOrderByAcessadoEmDesc(Integer usuarioId);

    Optional<HistoricoAcesso> findFirstByUsuarioIdAndConteudoIdOrderByAcessadoEmDesc(
            Integer usuarioId, Integer conteudoId);

    @Modifying
    @Transactional
    @Query("delete from HistoricoAcesso h where h.usuario.id = :usuarioId "
            + "and h.conteudoId = :conteudoId and h.historicoId <> :manterId")
    void removerAcessosDuplicadosDeConteudo(@Param("usuarioId") Integer usuarioId,
                                             @Param("conteudoId") Integer conteudoId,
                                             @Param("manterId") Integer manterId);

}
