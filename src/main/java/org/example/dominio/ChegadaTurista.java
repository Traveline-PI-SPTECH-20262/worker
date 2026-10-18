package org.example.dominio;

/**
 * Objeto de domínio: o que o sistema entende como um registro de negócio.
 * Uma linha da planilha (um país) vira VÁRIOS objetos destes,
 * um para cada combinação de via de acesso x ano.
 *
 * @param continente  ex.: "África"
 * @param pais        ex.: "Angola"
 * @param viaAcesso   TOTAL, AEREA, TERRESTRE, MARITIMA ou FLUVIAL
 * @param ano         2019, 2020 ou 2021
 * @param quantidade  número de chegadas de turistas
 * @param linhaOrigem linha do Excel de onde o dado saiu (para rastrear erros)
 */
public record ChegadaTurista(
        String continente,
        String pais,
        ViaAcesso viaAcesso,
        Integer ano,
        Long quantidade,
        int linhaOrigem) {
}
