package org.example.ingestao;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Um arquivo que chegou ao worker, independente da origem (e-mail ou disco).
 * Guardamos os bytes para poder abrir o InputStream quantas vezes for preciso.
 *
 * @param nome    nome original do arquivo (ex.: anexo.xlsx)
 * @param origem  de onde veio (ex.: "email: assunto X" ou "disco: /caminho")
 * @param conteudo bytes do arquivo
 */
public record ArquivoRecebido(String nome, String origem, byte[] conteudo) {

    public InputStream abrir() {
        return new ByteArrayInputStream(conteudo);
    }
}
