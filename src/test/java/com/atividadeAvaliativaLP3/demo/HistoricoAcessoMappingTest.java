package com.atividadeAvaliativaLP3.demo;

import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HistoricoAcessoMappingTest {

    @Test
    void historicoAcessoDeveMapearTabelaSemColunaDeQuiz() {
        Table table = HistoricoAcesso.class.getAnnotation(Table.class);

        assertNotNull(table, "A entidade HistoricoAcesso deve definir a tabela mapeada.");
        assertEquals("historico_acesso", table.name(), "O mapeamento deve usar a tabela real do banco em snake_case.");
        assertThrows(NoSuchFieldException.class, () -> HistoricoAcesso.class.getDeclaredField("quizzId"));
    }
}
