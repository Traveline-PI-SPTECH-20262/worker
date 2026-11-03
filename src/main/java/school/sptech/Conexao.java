package school.sptech;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    public static Connection getConexao() throws SQLException {
        String url = Config.get("DB_URL");
        Connection conexao = DriverManager.getConnection(url, Config.get("DB_USUARIO"), Config.get("DB_SENHA"));
        System.out.println("[BANCO] Conectado em " + url);
        return conexao;
    }
}
