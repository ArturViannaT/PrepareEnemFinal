package com.atividadeAvaliativaLP3.demo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricoAcessoRepository extends JpaRepository<HistoricoAcesso, Integer> {

    List<HistoricoAcesso> findByUsuarioIdOrderByAcessadoEmDesc(Integer usuarioId);
}
