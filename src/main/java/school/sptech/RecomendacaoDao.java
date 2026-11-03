package school.sptech;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecomendacaoDao {
    private Connection conexao;

    public RecomendacaoDao(Connection conexao) {
        this.conexao = conexao;
    }

    public List<Recomendacao> buscarTopPaises(Integer idPlanilha) throws SQLException {
        String sql = "SELECT p.idpais, p.nome, SUM(t.quantidade_turistas) AS total, "
              + "(SELECT t2.mes FROM turista_por_pais_mes t2 WHERE t2.pais_idpais = p.idpais AND t2.planilha_idplanilha = t.planilha_idplanilha "
              + " GROUP BY t2.mes ORDER BY SUM(t2.quantidade_turistas) DESC LIMIT 1) AS pico, "
              + "SUM(t.quantidade_turistas) * 100 / (SELECT SUM(quantidade_turistas) FROM turista_por_pais_mes WHERE planilha_idplanilha = t.planilha_idplanilha) AS percentual "
              + "FROM turista_por_pais_mes t JOIN pais p ON p.idpais = t.pais_idpais "
              + "WHERE t.planilha_idplanilha = ? "
              + "GROUP BY p.idpais, p.nome, t.planilha_idplanilha ORDER BY total DESC LIMIT 10";
        return buscarTop(sql, "País", idPlanilha);
    }

    public List<Recomendacao> buscarTopEstados(Integer idPlanilha) throws SQLException {
        String sql = "SELECT e.idestado, e.nome, SUM(t.quantidade_turista) AS total, "
              + "(SELECT t2.mes FROM turista_por_estado_mes t2 WHERE t2.estado_idestado = e.idestado AND t2.planilha_idplanilha = t.planilha_idplanilha "
              + " GROUP BY t2.mes ORDER BY SUM(t2.quantidade_turista) DESC LIMIT 1) AS pico, "
              + "SUM(t.quantidade_turista) * 100 / (SELECT SUM(quantidade_turista) FROM turista_por_estado_mes WHERE planilha_idplanilha = t.planilha_idplanilha) AS percentual "
              + "FROM turista_por_estado_mes t JOIN estado e ON e.idestado = t.estado_idestado "
              + "WHERE t.planilha_idplanilha = ? "
              + "GROUP BY e.idestado, e.nome, t.planilha_idplanilha ORDER BY total DESC LIMIT 10";
        return buscarTop(sql, "Estado", idPlanilha);
    }

    public void inserir(Recomendacao recomendacao, Integer idAgencia) throws SQLException {
        String sql = "INSERT INTO recomendacoes (dataGeracao, pontuacao, nivel, sazonalidade, idAgencia, idLocalDestino) "
              + "VALUES (NOW(), ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setDouble(1, recomendacao.getPontuacao());
            ps.setString(2, recomendacao.getNivel());
            ps.setString(3, recomendacao.getSazonalidade());
            ps.setInt(4, idAgencia);
            ps.setInt(5, recomendacao.getIdLocal());
            ps.executeUpdate();
            System.out.println("[BANCO] Recomendação gravada: " + recomendacao);
        }
    }

    public String buscarEmailAgencia(Integer idAgencia) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement("SELECT email FROM agencia WHERE idAgencia = ?")) {
            ps.setInt(1, idAgencia);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getString(1);
        }
    }

    private List<Recomendacao> buscarTop(String sql, String tipo, Integer idPlanilha) throws SQLException {
        List<Recomendacao> recomendacoes = new ArrayList<>();
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, idPlanilha);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int posicao = recomendacoes.size() + 1;
                recomendacoes.add(new Recomendacao(tipo, rs.getInt(1), rs.getString(2), rs.getInt(3),
                      rs.getDouble(5), nivel(posicao), rs.getString(4)));
            }
        }
        return recomendacoes;
    }

    private String nivel(int posicao) {
        if (posicao <= 3) {
            return "ALTO";
        }
        if (posicao <= 7) {
            return "MÉDIO";
        }
        return "BAIXO";
    }
}
