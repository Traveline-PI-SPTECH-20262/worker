package org.example.persistencia;

import org.example.dominio.ChegadaTurista;

import java.util.List;
import java.util.Optional;

/**
 * ETAPA 5 - PERSISTÊNCIA: contrato do CRUD.
 *
 * Hoje a única implementação é {@link ChegadaTuristaRepositoryLog}, que só loga.
 * Para usar um banco de verdade, crie uma ChegadaTuristaRepositoryJdbc
 * implementando esta interface e troque a instância no Main.
 */
public interface ChegadaTuristaRepository {

    // CREATE
    void salvar(ChegadaTurista chegada);

    void salvarTodos(List<ChegadaTurista> chegadas);

    // READ
    Optional<ChegadaTurista> buscarPorId(long id);

    List<ChegadaTurista> listarPorPais(String pais);

    // UPDATE
    void atualizar(long id, ChegadaTurista chegada);

    // DELETE
    void remover(long id);
}
