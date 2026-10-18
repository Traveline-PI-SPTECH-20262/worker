package org.example.mapeamento;

import org.example.dominio.ViaAcesso;

import java.util.EnumMap;
import java.util.Map;

/**
 * Onde está cada informação na aba "Brasil_1.1" da planilha do Anuário de Turismo.
 *
 * Estrutura observada no arquivo:
 *   Coluna B (1)          nome do continente (com espaços à esquerda) ou do país
 *   Colunas D, E, F       Total     2019, 2020, 2021
 *   Colunas H, I, J       Aérea     2019, 2020, 2021
 *   Colunas L, M, N       Terrestre 2019, 2020, 2021
 *   Colunas P, Q, R       Marítima  2019, 2020, 2021
 *   Colunas T, U, V       Fluvial   2019, 2020, 2021
 *
 * Se o layout da planilha mudar, só esta classe precisa ser alterada.
 */
public final class LayoutAbaBrasil11 {

    private LayoutAbaBrasil11() {
    }

    public static final int COLUNA_NOME = 1; // B

    /**
     * Primeira coluna de cada bloco; o bloco ocupa 3 colunas seguidas (um por ano).
     * EnumMap mantém a ordem do enum (TOTAL, AEREA, ...) ao percorrer.
     */
    public static final Map<ViaAcesso, Integer> COLUNA_INICIAL_POR_VIA = new EnumMap<>(Map.of(
            ViaAcesso.TOTAL, 3,      // D
            ViaAcesso.AEREA, 7,      // H
            ViaAcesso.TERRESTRE, 11, // L
            ViaAcesso.MARITIMA, 15,  // P
            ViaAcesso.FLUVIAL, 19)); // T

    public static final int[] ANOS = {2019, 2020, 2021};

    /** Linha de total geral, não é país nem continente. */
    public static final String LINHA_TOTAL_GERAL = "Brasil";

    /** Continentes vêm recuados com espaços na coluna B. */
    public static boolean ehContinente(String nomeOriginal) {
        return nomeOriginal.startsWith(" ");
    }
}
