package org.example.validacao;

import org.example.dominio.ChegadaTurista;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ETAPA 4 - VALIDAÇÃO/TRATAMENTO: regras de negócio antes de gravar.
 *
 * Registros inválidos não derrubam o processamento: são separados
 * com o motivo para serem logados (ou, no futuro, gravados numa tabela de erros).
 */
public class ValidadorChegadas {

    private static final Logger log = LoggerFactory.getLogger(ValidadorChegadas.class);

    public ResultadoValidacao validar(List<ChegadaTurista> chegadas) {
        List<ChegadaTurista> validos = new ArrayList<>();
        List<ResultadoValidacao.Rejeicao> rejeitados = new ArrayList<>();

        for (ChegadaTurista chegada : chegadas) {
            Optional<String> erro = verificar(chegada);
            if (erro.isPresent()) {
                log.warn("[VALIDACAO] Rejeitado (linha {}): {} | {}", chegada.linhaOrigem(), erro.get(), chegada);
                rejeitados.add(new ResultadoValidacao.Rejeicao(chegada, erro.get()));
            } else {
                validos.add(chegada);
            }
        }

        log.info("[VALIDACAO] {} válido(s), {} rejeitado(s)", validos.size(), rejeitados.size());
        return new ResultadoValidacao(validos, rejeitados);
    }

    private Optional<String> verificar(ChegadaTurista c) {
        if (c.continente() == null) {
            return Optional.of("país sem continente");
        }
        if (c.pais() == null || c.pais().isBlank()) {
            return Optional.of("país vazio");
        }
        if (c.quantidade() == null) {
            return Optional.of("quantidade vazia");
        }
        if (c.quantidade() < 0) {
            return Optional.of("quantidade negativa");
        }
        return Optional.empty();
    }
}
