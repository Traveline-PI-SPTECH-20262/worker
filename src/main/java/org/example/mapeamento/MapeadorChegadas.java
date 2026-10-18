package org.example.mapeamento;

import org.example.dominio.ChegadaTurista;
import org.example.dominio.ViaAcesso;
import org.example.leitura.LinhaPlanilha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.example.mapeamento.LayoutAbaBrasil11.*;

/**
 * ETAPA 3 - MAPEAMENTO: transforma linhas cruas em objetos de domínio.
 *
 * Regras:
 *  - Linhas sem número na coluna D são cabeçalho, título ou rodapé ("Fonte: ...") -> ignoradas
 *  - "Brasil" é o total geral -> ignorada
 *  - Nome recuado (continente) não gera registro, só define o continente das próximas linhas
 *  - Cada país gera 15 registros: 5 vias de acesso x 3 anos
 */
public class MapeadorChegadas {

    private static final Logger log = LoggerFactory.getLogger(MapeadorChegadas.class);

    public List<ChegadaTurista> mapear(List<LinhaPlanilha> linhas) {
        List<ChegadaTurista> chegadas = new ArrayList<>();
        String continenteAtual = null;
        int ignoradas = 0;

        for (LinhaPlanilha linha : linhas) {
            String nomeOriginal = linha.texto(COLUNA_NOME);

            if (nomeOriginal == null || linha.numero(COLUNA_INICIAL_POR_VIA.get(ViaAcesso.TOTAL)) == null) {
                log.debug("[MAPEAMENTO] Linha {} ignorada (cabeçalho/rodapé): {}", linha.numeroLinha(), nomeOriginal);
                ignoradas++;
                continue;
            }

            String nome = nomeOriginal.strip();

            if (LINHA_TOTAL_GERAL.equals(nome)) {
                log.debug("[MAPEAMENTO] Linha {} é o total geral, ignorada", linha.numeroLinha());
                continue;
            }

            if (ehContinente(nomeOriginal)) {
                // TODO: "Países não especificados" vem recuado como continente e não tem países abaixo
                continenteAtual = nome;
                log.info("[MAPEAMENTO] Continente: {}", continenteAtual);
                continue;
            }

            for (Map.Entry<ViaAcesso, Integer> bloco : COLUNA_INICIAL_POR_VIA.entrySet()) {
                for (int i = 0; i < ANOS.length; i++) {
                    Double valor = linha.numero(bloco.getValue() + i);
                    chegadas.add(new ChegadaTurista(
                            continenteAtual,
                            nome,
                            bloco.getKey(),
                            ANOS[i],
                            valor == null ? null : Math.round(valor),
                            linha.numeroLinha()));
                }
            }
        }

        log.info("[MAPEAMENTO] {} registro(s) de domínio gerado(s), {} linha(s) de cabeçalho/rodapé ignorada(s)",
                chegadas.size(), ignoradas);
        return chegadas;
    }
}
