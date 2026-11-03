package school.sptech;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PlanilhaDao {
    private Connection conexao;

    public PlanilhaDao(Connection conexao) {
        this.conexao = conexao;
    }

    public Integer inserir(String caminho, Integer idAgencia) throws SQLException {
        String sql = "INSERT INTO planilha (caminho, dataHora, situacao, dataInicio, agencia_idAgencia) "
              + "VALUES (?, NOW(), 'PROCESSANDO', CURDATE(), ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, caminho);
            ps.setInt(2, idAgencia);
            ps.executeUpdate();

            ResultSet chave = ps.getGeneratedKeys();
            chave.next();
            Integer id = chave.getInt(1);
            System.out.println("[BANCO] Planilha registrada com id " + id);
            return id;
        }
    }

    public void atualizarSituacao(Integer idPlanilha, String situacao) throws SQLException {
        String sql = "UPDATE planilha SET situacao = ?, dataConclusao = CURDATE() WHERE idplanilha = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, situacao);
            ps.setInt(2, idPlanilha);
            ps.executeUpdate();
            System.out.println("[BANCO] Planilha " + idPlanilha + " com situação " + situacao);
        }
    }
}
