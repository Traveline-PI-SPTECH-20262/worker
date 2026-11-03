package school.sptech;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Integer ID_AGENCIA = 1;

    public static void main(String[] args) throws Exception {
        String nomeArquivo = "01. a 03. Chegadas de turistas internacionais ao Brasil__A2022-2021-V49-1ed.xlsx";
        System.out.println("[MAIN] Iniciando worker para a agência " + ID_AGENCIA);

        LeitorExcel leitorExcel = new LeitorExcel();
        List<TuristaPorPais> turistasPorPais = leitorExcel.extrairTuristasPorPais(nomeArquivo);
        List<TuristaPorEstado> turistasPorEstado = leitorExcel.extrairTuristasPorEstado(nomeArquivo);

        try (Connection conexao = Conexao.getConexao()) {
            PlanilhaDao planilhaDao = new PlanilhaDao(conexao);
            TuristaDao turistaDao = new TuristaDao(conexao);

            Integer idPlanilha = planilhaDao.inserir(nomeArquivo, ID_AGENCIA);
            try {
                for (TuristaPorPais turista : turistasPorPais) {
                    turistaDao.inserirPorPais(turista, idPlanilha);
                }
                System.out.println("[BANCO] " + turistasPorPais.size() + " linhas em turista_por_pais_mes");

                for (TuristaPorEstado turista : turistasPorEstado) {
                    turistaDao.inserirPorEstado(turista, idPlanilha);
                }
                System.out.println("[BANCO] " + turistasPorEstado.size() + " linhas em turista_por_estado_mes");

                planilhaDao.atualizarSituacao(idPlanilha, "CONCLUIDA");
            } catch (Exception e) {
                planilhaDao.atualizarSituacao(idPlanilha, "ERRO");
                throw e;
            }

            RecomendacaoDao recomendacaoDao = new RecomendacaoDao(conexao);
            List<Recomendacao> recomendacoes = new ArrayList<>();
            recomendacoes.addAll(recomendacaoDao.buscarTopPaises(idPlanilha));
            recomendacoes.addAll(recomendacaoDao.buscarTopEstados(idPlanilha));
            for (Recomendacao recomendacao : recomendacoes) {
                recomendacaoDao.inserir(recomendacao, ID_AGENCIA);
            }

            new EnviadorEmail().enviar(recomendacaoDao.buscarEmailAgencia(ID_AGENCIA), recomendacoes);
        }

        System.out.println("[MAIN] Worker finalizado");
    }
}
