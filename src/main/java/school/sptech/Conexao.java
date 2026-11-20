package school.sptech;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    public static Connection getConexao() throws SQLException {

        // Credenciais sempre no .env (DB_URL, DB_USUARIO, DB_SENHA).
        // Se faltarem, cai nos valores padrão de desenvolvimento.
        String url = ou(Config.get("DB_URL"), "jdbc:mysql://54.234.69.177:3306/traveline");
        String usuario = ou(Config.get("DB_USUARIO"), "root");
        String senha = ou(Config.get("DB_SENHA"), "urubu100");

        Connection conexao = DriverManager.getConnection(url, usuario, senha);

        System.out.println("[BANCO] Conectado em " + url);

        return conexao;
    }

    private static String ou(String valor, String padrao) {
        return valor == null || valor.isEmpty() ? padrao : valor;
    }
}
