package org.example.config;

/**
 * Configuração do worker, lida de variáveis de ambiente.
 *
 * WORKER_MODO        LOCAL (padrão) lê um arquivo do disco; EMAIL busca anexos via IMAP
 * WORKER_ARQUIVO     caminho do .xlsx no modo LOCAL
 * WORKER_ABA         nome da aba (Sheet) que será lida
 * MAIL_HOST          servidor IMAP (ex.: imap.gmail.com)
 * MAIL_USUARIO       conta de e-mail
 * MAIL_SENHA         senha ou "senha de app"
 * MAIL_PASTA         pasta da caixa (padrão INBOX)
 */
public record ConfiguracaoWorker(
        Modo modo,
        String caminhoArquivoLocal,
        String nomeAba,
        String mailHost,
        String mailUsuario,
        String mailSenha,
        String mailPasta) {

    public enum Modo { LOCAL, EMAIL }

    private static final String ARQUIVO_PADRAO =
            "01. a 03. Chegadas de turistas internacionais ao Brasil__A2022-2021-V49-1ed.xlsx";
    private static final String ABA_PADRAO = "Brasil_1.1";

    public static ConfiguracaoWorker carregar() {
        return new ConfiguracaoWorker(
                Modo.valueOf(env("WORKER_MODO", "LOCAL").toUpperCase()),
                env("WORKER_ARQUIVO", ARQUIVO_PADRAO),
                env("WORKER_ABA", ABA_PADRAO),
                env("MAIL_HOST", ""),
                env("MAIL_USUARIO", ""),
                env("MAIL_SENHA", ""),
                env("MAIL_PASTA", "INBOX"));
    }

    private static String env(String nome, String padrao) {
        String valor = System.getenv(nome);
        return valor == null || valor.isBlank() ? padrao : valor;
    }

    /** Nunca imprime a senha nos logs. */
    @Override
    public String toString() {
        return "modo=" + modo + ", arquivo=" + caminhoArquivoLocal + ", aba=" + nomeAba
                + ", mailHost=" + mailHost + ", mailUsuario=" + mailUsuario + ", mailPasta=" + mailPasta;
    }
}
