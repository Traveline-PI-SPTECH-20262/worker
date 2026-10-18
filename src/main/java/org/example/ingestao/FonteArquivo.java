package org.example.ingestao;

import java.util.List;

/**
 * ETAPA 1 - INGESTÃO: de onde vêm as planilhas.
 *
 * O resto do worker não sabe se o arquivo veio de e-mail, disco, S3 etc.
 * Para trocar a origem basta criar outra implementação desta interface.
 */
public interface FonteArquivo {

    List<ArquivoRecebido> buscarArquivos();
}
