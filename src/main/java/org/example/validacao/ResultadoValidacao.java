package org.example.validacao;

import org.example.dominio.ChegadaTurista;

import java.util.List;

/**
 * Separação entre o que pode seguir para o banco e o que foi barrado.
 */
public record ResultadoValidacao(List<ChegadaTurista> validos, List<Rejeicao> rejeitados) {

    public record Rejeicao(ChegadaTurista registro, String motivo) {
    }
}
