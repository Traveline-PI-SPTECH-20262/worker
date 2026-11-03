package school.sptech;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class EnviadorEmail {
    public void enviar(String para, List<Recomendacao> recomendacoes) throws Exception {
        String assunto = "Traveline - Recomendações";
        String texto = montarTexto(recomendacoes);

        String host = Config.get("EMAIL_HOST");
        String usuario = Config.get("EMAIL_USUARIO");
        String senha = Config.get("EMAIL_SENHA");

        if (host.isEmpty() || usuario.isEmpty() || senha.isEmpty()) {
            System.out.println("[EMAIL] Credenciais vazias no .env, e-mail não enviado. Conteúdo:");
            System.out.println("Para: " + para);
            System.out.println("Assunto: " + assunto);
            System.out.println(texto);
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", Config.get("EMAIL_PORTA"));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(props);

        Message mensagem = new MimeMessage(session);
        mensagem.setFrom(new InternetAddress(usuario));
        mensagem.setRecipients(Message.RecipientType.TO, InternetAddress.parse(para));
        mensagem.setSubject(assunto);
        mensagem.setText(texto);

        Transport.send(mensagem, usuario, senha);
        System.out.println("[EMAIL] Recomendações enviadas para " + para);
    }

    private String montarTexto(List<Recomendacao> recomendacoes) {
        StringBuilder texto = new StringBuilder("Olá! Estas são as recomendações geradas a partir da última planilha.\n");
        String tipoAtual = "";
        int posicao = 0;
        for (Recomendacao r : recomendacoes) {
            if (!r.getTipo().equals(tipoAtual)) {
                tipoAtual = r.getTipo();
                posicao = 0;
                texto.append(tipoAtual.equals("País") ? "\nTop 10 países que mais enviam turistas:\n"
                      : "\nTop 10 estados mais visitados:\n");
            }
            posicao++;
            texto.append(String.format(Locale.of("pt", "BR"), "%2d. %s - %,d turistas (%.2f%%), nível %s, pico em %s%n",
                  posicao, r.getNome(), r.getTotalTuristas(), r.getPontuacao(), r.getNivel(), r.getSazonalidade()));
        }
        return texto.toString();
    }
}
