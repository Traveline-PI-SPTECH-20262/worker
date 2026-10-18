package org.example.ingestao;

import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.search.FlagTerm;
import org.example.config.ConfiguracaoWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * ETAPA 1 (produção): busca planilhas anexadas em e-mails não lidos via IMAP,
 * usando Jakarta Mail (o antigo JavaMail).
 *
 * Caminho do Jakarta Mail:
 *   Session -> Store (conexão IMAP) -> Folder (INBOX) -> Message -> Multipart -> BodyPart (anexo)
 */
public class FonteArquivoEmail implements FonteArquivo {

    private static final Logger log = LoggerFactory.getLogger(FonteArquivoEmail.class);

    private final ConfiguracaoWorker config;

    public FonteArquivoEmail(ConfiguracaoWorker config) {
        this.config = config;
    }

    @Override
    public List<ArquivoRecebido> buscarArquivos() {
        List<ArquivoRecebido> arquivos = new ArrayList<>();

        // Session: guarda as configurações de conexão (protocolo, SSL, timeouts)
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.ssl.enable", "true");
        props.put("mail.imaps.connectiontimeout", "10000");
        props.put("mail.imaps.timeout", "10000");
        Session session = Session.getInstance(props);

        log.info("[INGESTAO][EMAIL] Conectando em {} como {}", config.mailHost(), config.mailUsuario());

        // Store: a conexão com o servidor IMAP
        try (Store store = session.getStore("imaps")) {
            store.connect(config.mailHost(), config.mailUsuario(), config.mailSenha());

            // Folder: a pasta da caixa de entrada; READ_WRITE para poder marcar como lido
            try (Folder pasta = store.getFolder(config.mailPasta())) {
                pasta.open(Folder.READ_WRITE);

                // Busca só mensagens NÃO lidas (flag SEEN = false)
                Message[] mensagens = pasta.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                log.info("[INGESTAO][EMAIL] {} mensagem(ns) não lida(s) em {}", mensagens.length, config.mailPasta());

                for (Message mensagem : mensagens) {
                    arquivos.addAll(extrairAnexosPlanilha(mensagem));
                    // TODO: o ideal é marcar como lido só depois de persistir com sucesso
                    mensagem.setFlag(Flags.Flag.SEEN, true);
                }
            }
        } catch (MessagingException | IOException e) {
            log.error("[INGESTAO][EMAIL] Falha ao buscar e-mails", e);
        }

        log.info("[INGESTAO][EMAIL] {} planilha(s) encontrada(s) nos anexos", arquivos.size());
        return arquivos;
    }

    /** Percorre as partes do e-mail e devolve só os anexos .xlsx/.xls. */
    private List<ArquivoRecebido> extrairAnexosPlanilha(Message mensagem) throws MessagingException, IOException {
        List<ArquivoRecebido> anexos = new ArrayList<>();
        String assunto = mensagem.getSubject();
        log.info("[INGESTAO][EMAIL] Analisando e-mail: \"{}\"", assunto);

        // E-mail com anexo é sempre multipart (texto + arquivos)
        if (!mensagem.isMimeType("multipart/*")) {
            log.info("[INGESTAO][EMAIL] E-mail sem anexos, ignorado");
            return anexos;
        }

        Multipart partes = (Multipart) mensagem.getContent();
        for (int i = 0; i < partes.getCount(); i++) {
            BodyPart parte = partes.getBodyPart(i);

            boolean ehAnexo = Part.ATTACHMENT.equalsIgnoreCase(parte.getDisposition())
                    || parte.getFileName() != null;
            if (!ehAnexo) {
                continue;
            }

            // Nomes de anexo podem vir codificados (ex.: =?UTF-8?B?...?=)
            String nome = MimeUtility.decodeText(parte.getFileName());
            if (!nome.toLowerCase().endsWith(".xlsx") && !nome.toLowerCase().endsWith(".xls")) {
                log.info("[INGESTAO][EMAIL] Anexo ignorado (não é planilha): {}", nome);
                continue;
            }

            try (InputStream in = parte.getInputStream()) {
                byte[] bytes = in.readAllBytes();
                log.info("[INGESTAO][EMAIL] Anexo de planilha baixado: {} ({} bytes)", nome, bytes.length);
                anexos.add(new ArquivoRecebido(nome, "email: " + assunto, bytes));
            }
        }
        return anexos;
    }
}
