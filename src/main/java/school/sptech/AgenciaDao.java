package school.sptech;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AgenciaDao {
    private Connection conexao;

    public AgenciaDao(Connection conexao) {
        this.conexao = conexao;
    }

    public List<Agencia> listarTodas() throws SQLException {
        List<Agencia> agencias = new ArrayList<>();
        try (PreparedStatement ps = conexao.prepareStatement("SELECT idAgencia, nome, email FROM agencia")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                agencias.add(new Agencia(rs.getInt("idAgencia"), rs.getString("nome"), rs.getString("email")));
            }
        }
        System.out.println("[BANCO] " + agencias.size() + " agência(s) cadastrada(s)");
        return agencias;
    }
}
