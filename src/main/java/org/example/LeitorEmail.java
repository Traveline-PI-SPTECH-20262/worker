package org.example;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;

import java.io.InputStream;
import java.util.Properties;

// Busca a planilha anexada no e-mail usando Jakarta Mail (JavaMail)
public class LeitorEmail {

    String host = "imap.gmail.com";
    String usuario = "seu-email@gmail.com";
    String senha = "sua-senha-de-app";

    public InputStream buscarPlanilha() throws Exception {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        Session session = Session.getInstance(props);


        Store store = session.getStore("imaps");
        store.connect(host, usuario, senha);
        System.out.println("[EMAIL] Conectado em " + host);


        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Message[] mensagens = inbox.getMessages();
        System.out.println("[EMAIL] " + mensagens.length + " e-mails na caixa");


        for (int i = mensagens.length - 1; i >= 0; i--) {
            Message mensagem = mensagens[i];


            if (!mensagem.isMimeType("multipart/*")) {
                continue;
            }

            Multipart partes = (Multipart) mensagem.getContent();
            for (int j = 0; j < partes.getCount(); j++) {
                Part parte = partes.getBodyPart(j);
                String nome = parte.getFileName();

                if (nome != null && nome.endsWith(".xlsx")) {
                    System.out.println("[EMAIL] Planilha encontrada: " + nome + " (e-mail: " + mensagem.getSubject() + ")");
                    return parte.getInputStream();
                }
            }
        }

        System.out.println("[EMAIL] Nenhuma planilha encontrada");
        return null;
    }
}
