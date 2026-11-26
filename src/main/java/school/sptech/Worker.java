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
    private UsuarioDao usuarioDao;

    public Worker(LeitorS3 leitorS3, AgenciaDao agenciaDao, PlanilhaDao planilhaDao, TuristaDao turistaDao,
          RecomendacaoDao recomendacaoDao, UsuarioDao usuarioDao) {
        this.leitorS3 = leitorS3;
        this.agenciaDao = agenciaDao;
        this.planilhaDao = planilhaDao;
        this.turistaDao = turistaDao;
        this.recomendacaoDao = recomendacaoDao;
        this.usuarioDao = usuarioDao;
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
                Log.info(Worker.class, "[WORKER] Ignorado, nome fora do padrão <idAgencia>-<nome>.xlsx: " + arquivo);
                continue;
            }

            Agencia agencia = agencias.get(idAgencia);
            if (agencia == null) {
                Log.info(Worker.class, "[WORKER] Ignorado, agência " + idAgencia + " não existe no banco: " + arquivo);
                continue;
            }

            if (planilhaDao.jaRegistrada(arquivo.getChave())) {
                continue;
            }

            // Qualquer erro em um arquivo não impede os próximos:
            // o problema é avisado por e-mail à agência e o loop segue.
            try {
                processarArquivo(arquivo, agencia);
            } catch (Exception e) {
                Log.erro(Worker.class, "[WORKER] Erro ao processar " + arquivo + ": " + e.getMessage());
                avisarErro(agencia, arquivo, e);
            }
        }
    }

    private void processarArquivo(ArquivoS3 arquivo, Agencia agencia) throws Exception {
        Integer idAgencia = agencia.getIdAgencia();
        Log.info(Worker.class, "[WORKER] Processando " + arquivo + " da agência " + idAgencia + " (" + agencia.getNome() + ")");

        // 3. Registra a planilha, baixa do S3 e lê com Apache POI.
        // Registrando antes, um arquivo com erro fica como ERRO e não é reprocessado a cada execução.
        Integer idPlanilha = planilhaDao.inserir(arquivo.getChave(), idAgencia);
        try {
            byte[] conteudo = leitorS3.baixar(arquivo);
            List<TuristaPorPais> turistasPorPais = leitorExcel.extrairTuristasPorPais(new ByteArrayInputStream(conteudo));
            List<TuristaPorEstado> turistasPorEstado = leitorExcel.extrairTuristasPorEstado(new ByteArrayInputStream(conteudo));

            // 4. Grava os dados ligados à agência dona do arquivo
            for (TuristaPorPais turista : turistasPorPais) {
                turistaDao.inserirPorPais(turista, idPlanilha);
            }
            Log.info(Worker.class, "[BANCO] " + turistasPorPais.size() + " linhas em turista_por_pais_mes");

            for (TuristaPorEstado turista : turistasPorEstado) {
                turistaDao.inserirPorEstado(turista, idPlanilha);
            }
            Log.info(Worker.class, "[BANCO] " + turistasPorEstado.size() + " linhas em turista_por_estado_mes");

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

        for (Usuario usuario : usuariosParaNotificar(agencia)) {
            try {
                enviadorEmail.enviar(usuario.getEmail(), recomendacoes);
            } catch (Exception e) {
                Log.erro(Worker.class, "[EMAIL] Falha ao enviar para " + usuario.getEmail() + ": " + e.getMessage());
            }
        }
    }

    // Usuários da agência que aceitam e-mail. Quem desativou é pulado.
    private List<Usuario> usuariosParaNotificar(Agencia agencia) throws Exception {
        List<Usuario> destinatarios = new ArrayList<>();
        for (Usuario usuario : usuarioDao.listarPorAgencia(agencia.getIdAgencia())) {
            if (usuario.isReceberEmail()) {
                destinatarios.add(usuario);
            } else {
                Log.info(Worker.class, "[EMAIL] " + usuario.getEmail() + " não recebe e-mails (preferência), pulando");
            }
        }
        if (destinatarios.isEmpty()) {
            Log.info(Worker.class, "[EMAIL] Nenhum usuário da agência " + agencia.getIdAgencia() + " para notificar");
        }
        return destinatarios;
    }

    // Avisa os usuários da agência por e-mail quando um arquivo falha. O e-mail nunca pode
    // derrubar o worker, então o erro de envio é só logado.
    private void avisarErro(Agencia agencia, ArquivoS3 arquivo, Exception causa) {
        List<Usuario> destinatarios;
        try {
            destinatarios = usuariosParaNotificar(agencia);
        } catch (Exception e) {
            Log.erro(Worker.class, "[EMAIL] Falha ao buscar usuários da agência " + agencia.getIdAgencia() + ": " + e.getMessage());
            return;
        }
        for (Usuario usuario : destinatarios) {
            try {
                enviadorEmail.enviarErro(usuario.getEmail(), arquivo.getNome(), causa.getMessage());
            } catch (Exception e) {
                Log.erro(Worker.class, "[EMAIL] Falha ao avisar erro para " + usuario.getEmail() + ": " + e.getMessage());
            }
        }
    }
}
