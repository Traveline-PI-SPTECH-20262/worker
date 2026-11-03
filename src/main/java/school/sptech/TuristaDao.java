package school.sptech;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class TuristaDao {
    private Connection conexao;

    private Map<String, Integer> idsMeio = new HashMap<>();
    private Map<String, Integer> idsPais = new HashMap<>();
    private Map<String, Integer> idsEstado = new HashMap<>();

    public TuristaDao(Connection conexao) {
        this.conexao = conexao;
    }

    public void inserirPorPais(TuristaPorPais turista, Integer idPlanilha) throws SQLException {
        String sql = "INSERT INTO turista_por_pais_mes (ano, mes, quantidade_turistas, meio_idmodal, pais_idpais, planilha_idplanilha) "
              + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(turista.getAno()));
            ps.setString(2, turista.getMes());
            ps.setInt(3, turista.getQuantidade());
            ps.setInt(4, idMeio(turista.getMeio()));
            ps.setInt(5, idPais(turista.getPais(), turista.getContinente()));
            ps.setInt(6, idPlanilha);
            ps.executeUpdate();
        }
    }

    public void inserirPorEstado(TuristaPorEstado turista, Integer idPlanilha) throws SQLException {
        String sql = "INSERT INTO turista_por_estado_mes (ano, mes, quantidade_turista, planilha_idplanilha, estado_idestado, meio_idmodal) "
              + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(turista.getAno()));
            ps.setString(2, turista.getMes());
            ps.setInt(3, turista.getQuantidade());
            ps.setInt(4, idPlanilha);
            ps.setInt(5, idEstado(turista.getEstado(), turista.getSigla()));
            ps.setInt(6, idMeio(turista.getMeio()));
            ps.executeUpdate();
        }
    }

    private Integer idMeio(String tipo) throws SQLException {
        if (!idsMeio.containsKey(tipo)) {
            Integer id = buscarId("SELECT idmodal FROM meio WHERE tipo = ?", tipo);
            if (id == null) {
                id = inserir("INSERT INTO meio (tipo) VALUES (?)", tipo);
                System.out.println("[BANCO] Meio cadastrado: " + tipo);
            }
            idsMeio.put(tipo, id);
        }
        return idsMeio.get(tipo);
    }

    private Integer idPais(String nome, String continente) throws SQLException {
        if (!idsPais.containsKey(nome)) {
            Integer id = buscarId("SELECT idpais FROM pais WHERE nome = ?", nome);
            if (id == null) {
                id = inserir("INSERT INTO pais (nome, continente) VALUES (?, ?)", nome, continente);
                System.out.println("[BANCO] País cadastrado: " + nome + " (" + continente + ")");
            }
            idsPais.put(nome, id);
        }
        return idsPais.get(nome);
    }

    private Integer idEstado(String nome, String sigla) throws SQLException {
        if (!idsEstado.containsKey(sigla)) {
            Integer id = buscarId("SELECT idestado FROM estado WHERE sigla = ?", sigla);
            if (id == null) {
                id = inserir("INSERT INTO estado (sigla, nome) VALUES (?, ?)", sigla, nome);
                System.out.println("[BANCO] Estado cadastrado: " + sigla + " - " + nome);
            }
            idsEstado.put(sigla, id);
        }
        return idsEstado.get(sigla);
    }

    private Integer buscarId(String sql, String valor) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, valor);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return null;
        }
    }

    private Integer inserir(String sql, String... valores) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < valores.length; i++) {
                ps.setString(i + 1, valores[i]);
            }
            ps.executeUpdate();
            ResultSet chave = ps.getGeneratedKeys();
            chave.next();
            return chave.getInt(1);
        }
    }
}
