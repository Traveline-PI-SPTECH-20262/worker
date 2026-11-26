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

    // Se o arquivo já tem registro (concluído, com erro ou em processamento), não processa de novo.
    public boolean jaRegistrada(String caminho) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("SELECT 1 FROM planilha WHERE caminho = ? LIMIT 1")) {
            ps.setString(1, caminho);
            return ps.executeQuery().next();
        }
    }

    public Integer inserir(String caminho, Integer idAgencia) throws SQLException {
        String sql = "INSERT INTO planilha (caminho, nome, dataHora, situacao, dataInicio, agencia_idAgencia) "
              + "VALUES (?, ?, NOW(), 'PROCESSANDO', CURDATE(), ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, caminho);
            ps.setString(2, "planilha");
            ps.setInt(3, idAgencia);
            ps.executeUpdate();

            ResultSet chave = ps.getGeneratedKeys();
            chave.next();
            Integer id = chave.getInt(1);

            // Nome da planilha: "planilha" + id
            try (PreparedStatement psNome = conexao.prepareStatement("UPDATE planilha SET nome = ? WHERE idplanilha = ?")) {
                psNome.setString(1, "planilha" + id);
                psNome.setInt(2, id);
                psNome.executeUpdate();
            }
            Log.info(PlanilhaDao.class, "[BANCO] Planilha registrada com id " + id);
            return id;
        }
    }

    public void atualizarSituacao(Integer idPlanilha, String situacao) throws SQLException {
        String sql = "UPDATE planilha SET situacao = ?, dataConclusao = CURDATE() WHERE idplanilha = ?";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, situacao);
            ps.setInt(2, idPlanilha);
            ps.executeUpdate();
            Log.info(PlanilhaDao.class, "[BANCO] Planilha " + idPlanilha + " com situação " + situacao);
        }
    }
}
