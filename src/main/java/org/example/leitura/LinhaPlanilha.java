package org.example.leitura;

import java.util.List;

/**
 * Uma linha "crua" da planilha, já fora do Apache POI.
 *
 * Depois desta classe o resto do worker não depende mais de Row/Cell:
 * cada célula vira String, Double, Boolean ou null (vazia).
 *
 * @param numeroLinha número da linha como aparece no Excel (começa em 1)
 * @param celulas     valores por índice de coluna (0 = A, 1 = B, 2 = C ...)
 */
public record LinhaPlanilha(int numeroLinha, List<Object> celulas) {

    public Object valor(int coluna) {
        return coluna < celulas.size() ? celulas.get(coluna) : null;
    }

    public String texto(int coluna) {
        Object v = valor(coluna);
        return v == null ? null : v.toString();
    }

    public Double numero(int coluna) {
        return valor(coluna) instanceof Double d ? d : null;
    }
}
