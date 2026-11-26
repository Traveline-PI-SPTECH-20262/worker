package school.sptech;

import java.sql.Connection;
import java.sql.PreparedStatement;


public class Log {
    private static Connection conexao;

    public static void setConexao(Connection novaConexao) {
        conexao = novaConexao;
    }

    public static void info(Class<?> classe, String mensagem) {
        registrar("INFO", classe, mensagem);
    }

    public static void erro(Class<?> classe, String mensagem) {
        registrar("ERRO", classe, mensagem);
    }

    private static void registrar(String nivel, Class<?> classe, String mensagem) {
        System.out.println(mensagem);
        try {
            if (conexao == null || conexao.isClosed()) {
                return;
            }
            try (PreparedStatement ps = conexao.prepareStatement(
                  "INSERT INTO log_java (nivel, mensagem, classe) VALUES (?, ?, ?)")) {
                ps.setString(1, nivel);
                ps.setString(2, mensagem);
                ps.setString(3, classe.getSimpleName());
                ps.executeUpdate();
            }
        } catch (Exception e) {

            System.out.println("[LOG] Falha ao gravar log no banco: " + e.getMessage());
        }
    }
}
