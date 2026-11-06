
package school.sptech;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

    public static Connection getConexao() throws SQLException {

        String url = "jdbc:mysql://100.54.110.112:3306/traveline";
        String usuario = "root";
        String senha = "urubu100";

        Connection conexao = DriverManager.getConnection(url, usuario, senha);

        System.out.println("[BANCO] Conectado em " + url);

        return conexao;
    }
}
