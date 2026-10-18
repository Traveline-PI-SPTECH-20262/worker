package org.example.persistencia;

import org.example.dominio.ChegadaTurista;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Implementação "falsa" do CRUD: em vez de executar SQL, loga o que seria executado.
 *
 * Tabela sugerida:
 *   CREATE TABLE chegada_turista (
 *       id          BIGINT PRIMARY KEY AUTO_INCREMENT,
 *       continente  VARCHAR(60)  NOT NULL,
 *       pais        VARCHAR(100) NOT NULL,
 *       via_acesso  VARCHAR(20)  NOT NULL,
 *       ano         INT          NOT NULL,
 *       quantidade  BIGINT       NOT NULL,
 *       UNIQUE (pais, via_acesso, ano)
 *   );
 *
 * Na versão JDBC, cada método abaixo vira um PreparedStatement com o SQL do log.
 */
public class ChegadaTuristaRepositoryLog implements ChegadaTuristaRepository {

    private static final Logger log = LoggerFactory.getLogger(ChegadaTuristaRepositoryLog.class);

    private static final String SQL_INSERT =
            "INSERT INTO chegada_turista (continente, pais, via_acesso, ano, quantidade) VALUES (?, ?, ?, ?, ?)";

    @Override
    public void salvar(ChegadaTurista c) {
        log.info("[CRUD][CREATE] {} -> ('{}', '{}', {}, {}, {})",
                SQL_INSERT, c.continente(), c.pais(), c.viaAcesso(), c.ano(), c.quantidade());
    }

    @Override
    public void salvarTodos(List<ChegadaTurista> chegadas) {
        // Em JDBC: connection.setAutoCommit(false); ps.addBatch() para cada item; ps.executeBatch(); commit()
        log.info("[CRUD][CREATE][BATCH] Iniciando transação com {} registro(s)", chegadas.size());
        chegadas.forEach(this::salvar);
        log.info("[CRUD][CREATE][BATCH] COMMIT de {} registro(s)", chegadas.size());
    }

    @Override
    public Optional<ChegadaTurista> buscarPorId(long id) {
        log.info("[CRUD][READ] SELECT * FROM chegada_turista WHERE id = {}", id);
        return Optional.empty();
    }

    @Override
    public List<ChegadaTurista> listarPorPais(String pais) {
        log.info("[CRUD][READ] SELECT * FROM chegada_turista WHERE pais = '{}'", pais);
        return List.of();
    }

    @Override
    public void atualizar(long id, ChegadaTurista c) {
        log.info("[CRUD][UPDATE] UPDATE chegada_turista SET quantidade = {} WHERE id = {}", c.quantidade(), id);
    }

    @Override
    public void remover(long id) {
        log.info("[CRUD][DELETE] DELETE FROM chegada_turista WHERE id = {}", id);
    }
}
