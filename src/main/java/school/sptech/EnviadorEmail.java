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
        enviarTexto(para, "Traveline - Recomendações", montarTexto(recomendacoes));
    }

    // Envio genérico de um e-mail de texto. Com as credenciais vazias no .env,
    // o conteúdo é só impresso no console (modo desenvolvimento).
    private void enviarTexto(String para, String assunto, String texto) throws Exception {
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
        System.out.println("[EMAIL] E-mail \"" + assunto + "\" enviado para " + para);
    }

    // Aviso de que a análise de dados de um arquivo falhou.
    public void enviarErro(String para, String nomeArquivo, String detalhe) throws Exception {
        String assunto = "Traveline - Problema ao realizar análise de dados";
        StringBuilder texto = new StringBuilder("Houve um problema ao realizar a análise de dados");
        if (nomeArquivo != null) {
            texto.append(" do arquivo ").append(nomeArquivo);
        }
        texto.append(".\n");
        if (detalhe != null) {
            texto.append("Detalhe: ").append(detalhe).append("\n");
        }
        enviarTexto(para, assunto, texto.toString());
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
