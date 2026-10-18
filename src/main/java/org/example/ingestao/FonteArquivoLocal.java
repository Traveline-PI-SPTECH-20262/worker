package org.example.ingestao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * ETAPA 1 (modo desenvolvimento): lê a planilha direto do disco.
 * Útil para testar o fluxo sem precisar de uma caixa de e-mail.
 */
public class FonteArquivoLocal implements FonteArquivo {

    private static final Logger log = LoggerFactory.getLogger(FonteArquivoLocal.class);

    private final Path caminho;

    public FonteArquivoLocal(String caminho) {
        this.caminho = Path.of(caminho);
    }

    @Override
    public List<ArquivoRecebido> buscarArquivos() {
        log.info("[INGESTAO][LOCAL] Lendo arquivo do disco: {}", caminho.toAbsolutePath());

        if (!Files.exists(caminho)) {
            log.warn("[INGESTAO][LOCAL] Arquivo não encontrado, nada a processar");
            return List.of();
        }

        try {
            byte[] bytes = Files.readAllBytes(caminho);
            log.info("[INGESTAO][LOCAL] {} bytes lidos", bytes.length);
            return List.of(new ArquivoRecebido(
                    caminho.getFileName().toString(), "disco: " + caminho, bytes));
        } catch (IOException e) {
            log.error("[INGESTAO][LOCAL] Falha ao ler o arquivo", e);
            return List.of();
        }
    }
}
