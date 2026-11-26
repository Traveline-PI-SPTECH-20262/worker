package school.sptech;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDao {
    private Connection conexao;

    public UsuarioDao(Connection conexao) {
        this.conexao = conexao;
    }

    // Usuários da agência com a preferência de e-mail.
    // Sem linha em preferencias_usuario, vale o padrão da tabela: receber.
    public List<Usuario> listarPorAgencia(Integer idAgencia) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.idusuario, u.nome, u.email, COALESCE(p.receberEmail, 1) AS receberEmail "
              + "FROM usuario u LEFT JOIN preferencias_usuario p ON p.idUsuario = u.idusuario "
              + "WHERE u.idAgencia = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, idAgencia);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                usuarios.add(new Usuario(rs.getInt("idusuario"), rs.getString("nome"), rs.getString("email"),
                      rs.getInt("receberEmail") == 1));
            }
        }
        return usuarios;
    }
}
