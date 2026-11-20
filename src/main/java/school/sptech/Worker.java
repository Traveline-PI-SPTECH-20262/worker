package school.sptech;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Orquestra o fluxo: percorre os arquivos do bucket S3, descobre a agência dona
// de cada um pelo id antes do "-" no nome e grava os dados ligados a essa agência.
public class Worker {
    private LeitorS3 leitorS3;
    private LeitorExcel leitorExcel;
    private EnviadorEmail enviadorEmail;
    private AgenciaDao agenciaDao;
    private PlanilhaDao planilhaDao;
    private TuristaDao turistaDao;
    private RecomendacaoDao recomendacaoDao;

    public Worker(LeitorS3 leitorS3, AgenciaDao agenciaDao, PlanilhaDao planilhaDao, TuristaDao turistaDao,
          RecomendacaoDao recomendacaoDao) {
        this.leitorS3 = leitorS3;
        this.agenciaDao = agenciaDao;
        this.planilhaDao = planilhaDao;
        this.turistaDao = turistaDao;
        this.recomendacaoDao = recomendacaoDao;
        this.leitorExcel = new LeitorExcel();
        this.enviadorEmail = new EnviadorEmail();
    }

    public void executar() throws Exception {
        // 1. Agências do banco, indexadas pelo id
        Map<Integer, Agencia> agencias = new HashMap<>();
        for (Agencia agencia : agenciaDao.listarTodas()) {
            agencias.put(agencia.getIdAgencia(), agencia);
        }

        // 2. Para cada arquivo do bucket, descobre a agência pelo id antes do "-"
        for (ArquivoS3 arquivo : leitorS3.listarArquivos()) {
            Integer idAgencia = arquivo.getIdAgencia();
            if (idAgencia == null) {
                System.out.println("[WORKER] Ignorado, nome fora do padrão <idAgencia>-<nome>.xlsx: " + arquivo);
                continue;
            }

            Agencia agencia = agencias.get(idAgencia);
            if (agencia == null) {
                System.out.println("[WORKER] Ignorado, agência " + idAgencia + " não existe no banco: " + arquivo);
                continue;
            }

            // Qualquer erro em um arquivo não impede os próximos:
            // o problema é avisado por e-mail à agência e o loop segue.
            try {
                processarArquivo(arquivo, agencia);
            } catch (Exception e) {
                System.out.println("[WORKER] Erro ao processar " + arquivo + ": " + e.getMessage());
                avisarErro(agencia, arquivo, e);
            }
        }
    }

    private void processarArquivo(ArquivoS3 arquivo, Agencia agencia) throws Exception {
        Integer idAgencia = agencia.getIdAgencia();
        System.out.println("[WORKER] Processando " + arquivo + " da agência " + idAgencia + " (" + agencia.getNome() + ")");

        // 3. Baixa a planilha do S3 e lê com Apache POI
        byte[] conteudo = leitorS3.baixar(arquivo);
        List<TuristaPorPais> turistasPorPais = leitorExcel.extrairTuristasPorPais(new ByteArrayInputStream(conteudo));
        List<TuristaPorEstado> turistasPorEstado = leitorExcel.extrairTuristasPorEstado(new ByteArrayInputStream(conteudo));

        // 4. Grava os dados ligados à agência dona do arquivo
        Integer idPlanilha = planilhaDao.inserir(arquivo.getChave(), idAgencia);
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

        // 5. Recomendações e e-mail de sucesso para a agência
        List<Recomendacao> recomendacoes = new ArrayList<>();
        recomendacoes.addAll(recomendacaoDao.buscarTopPaises(idPlanilha));
        recomendacoes.addAll(recomendacaoDao.buscarTopEstados(idPlanilha));
        for (Recomendacao recomendacao : recomendacoes) {
            recomendacaoDao.inserir(recomendacao, idAgencia);
        }

        enviadorEmail.enviar(agencia.getEmail(), recomendacoes);
    }

    // Avisa a agência por e-mail quando um arquivo falha. O e-mail nunca pode
    // derrubar o worker, então o erro de envio é só logado.
    private void avisarErro(Agencia agencia, ArquivoS3 arquivo, Exception causa) {
        try {
            enviadorEmail.enviarErro(agencia.getEmail(), arquivo.getNome(), causa.getMessage());
        } catch (Exception e) {
            System.out.println("[EMAIL] Falha ao avisar erro para " + agencia.getEmail() + ": " + e.getMessage());
        }
    }
}
